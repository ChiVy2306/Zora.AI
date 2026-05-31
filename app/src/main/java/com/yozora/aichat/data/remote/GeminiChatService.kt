package com.yozora.aichat.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.yozora.aichat.data.db.MessageEntity
import com.yozora.aichat.data.db.PersonaEntity
import com.yozora.aichat.ui.chat.ApiVendor
import com.yozora.aichat.ui.chat.SafetyLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class GeminiChatReply(
    val text: String,
    val promptTokenCount: Int,
    val responseTokenCount: Int,
    val totalTokenCount: Int
)

private const val FALLBACK_MASTER_PROMPT = """
You are a customizable AI companion. Follow the persona instruction prompt for the active session, keep replies immersive and useful, and ask for clarification when the user request is unclear.
"""

class GeminiChatService {
    fun supportsImageInput(vendor: ApiVendor, model: String): Boolean {
        val normalized = model.lowercase(Locale.US)
        return when (vendor) {
            ApiVendor.Google,
            ApiVendor.GPT,
            ApiVendor.Claude -> true

            ApiVendor.Grok -> listOf("build", "multi-agent", "reasoning", "fast")
                .none { marker -> normalized.contains(marker) }

            ApiVendor.Mixtral -> !normalized.startsWith("ministral")
        }
    }

    suspend fun sendMessage(
        apiKey: String,
        persona: PersonaEntity,
        history: List<MessageEntity>,
        userInput: String,
        vendor: ApiVendor,
        safetyLevel: SafetyLevel,
        images: List<Bitmap>,
        webSearchEnabled: Boolean,
        masterPrompt: String? = null
    ): Result<GeminiChatReply> = runCatching {
        val baseMasterPrompt = masterPrompt?.takeIf { it.isNotBlank() } ?: FALLBACK_MASTER_PROMPT
        val finalSystemPrompt = baseMasterPrompt.trimIndent() +
            "\n\n---\n\n" +
            persona.systemPrompt.trim()

        when (vendor) {
            ApiVendor.Google -> if (webSearchEnabled) {
                sendGroundedGemini(
                    apiKey = apiKey,
                    persona = persona,
                    history = history,
                    userInput = userInput,
                    finalSystemPrompt = finalSystemPrompt,
                    safetyLevel = safetyLevel,
                    images = images
                )
            } else {
                sendGemini(
                    apiKey = apiKey,
                    persona = persona,
                    history = history,
                    userInput = userInput,
                    finalSystemPrompt = finalSystemPrompt,
                    safetyLevel = safetyLevel,
                    images = images
                )
            }

            ApiVendor.Claude -> sendClaude(
                apiKey = apiKey,
                persona = persona,
                history = history,
                userInput = userInput,
                finalSystemPrompt = finalSystemPrompt,
                images = images
            )

            ApiVendor.GPT,
            ApiVendor.Grok,
            ApiVendor.Mixtral -> sendOpenAiCompatible(
                apiKey = apiKey,
                vendor = vendor,
                persona = persona,
                history = history,
                userInput = userInput,
                finalSystemPrompt = finalSystemPrompt,
                images = images
            )
        }
    }

    private suspend fun sendGemini(
        apiKey: String,
        persona: PersonaEntity,
        history: List<MessageEntity>,
        userInput: String,
        finalSystemPrompt: String,
        safetyLevel: SafetyLevel,
        images: List<Bitmap>
    ): GeminiChatReply {
        val model = GenerativeModel(
            modelName = persona.model,
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = persona.temperature
                maxOutputTokens = 8192
            },
            safetySettings = safetySettings(safetyLevel),
            systemInstruction = content { text(finalSystemPrompt) }
        )

        val chatHistory = history.mapNotNull { message ->
            when (message.role) {
                "user" -> content(role = "user") { text(message.content) }
                "model" -> content(role = "model") { text(message.content) }
                else -> null
            }
        }

        val chat = model.startChat(chatHistory)
        val response = if (images.isEmpty()) {
            chat.sendMessage(userInput)
        } else {
            chat.sendMessage(
                content(role = "user") {
                    text(userInput)
                    images.forEach { image(it) }
                }
            )
        }
        val usage = response.usageMetadata
        return GeminiChatReply(
            text = response.text.orEmpty(),
            promptTokenCount = usage?.promptTokenCount ?: 0,
            responseTokenCount = usage?.candidatesTokenCount ?: 0,
            totalTokenCount = usage?.totalTokenCount ?: 0
        )
    }

    private suspend fun sendGroundedGemini(
        apiKey: String,
        persona: PersonaEntity,
        history: List<MessageEntity>,
        userInput: String,
        finalSystemPrompt: String,
        safetyLevel: SafetyLevel,
        images: List<Bitmap>
    ): GeminiChatReply = withContext(Dispatchers.IO) {
        val contents = JSONArray()
        history.forEach { message ->
            contents.put(
                JSONObject()
                    .put("role", if (message.role == "model") "model" else "user")
                    .put("parts", JSONArray().put(JSONObject().put("text", message.content)))
            )
        }
        contents.put(
            JSONObject()
                .put("role", "user")
                .put("parts", geminiParts(userInput, images))
        )

        val body = JSONObject()
            .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", finalSystemPrompt))))
            .put("contents", contents)
            .put(
                "generationConfig",
                JSONObject()
                    .put("temperature", persona.temperature.toDouble())
                    .put("maxOutputTokens", 8192)
            )
            .put("safetySettings", geminiRestSafetySettings(safetyLevel))
            .put("tools", JSONArray().put(JSONObject().put("google_search", JSONObject())))

        val json = postJson(
            url = "https://generativelanguage.googleapis.com/v1beta/models/${persona.model}:generateContent",
            headers = mapOf("x-goog-api-key" to apiKey),
            body = body
        )
        val candidate = json.getJSONArray("candidates").getJSONObject(0)
        val responseText = extractGeminiRestText(candidate)
        val groundedText = appendGroundingSources(responseText, candidate.optJSONObject("groundingMetadata"))
        val usage = json.optJSONObject("usageMetadata")
        GeminiChatReply(
            text = groundedText,
            promptTokenCount = usage?.optInt("promptTokenCount") ?: 0,
            responseTokenCount = usage?.optInt("candidatesTokenCount") ?: 0,
            totalTokenCount = usage?.optInt("totalTokenCount") ?: 0
        )
    }

    private suspend fun sendOpenAiCompatible(
        apiKey: String,
        vendor: ApiVendor,
        persona: PersonaEntity,
        history: List<MessageEntity>,
        userInput: String,
        finalSystemPrompt: String,
        images: List<Bitmap>
    ): GeminiChatReply = withContext(Dispatchers.IO) {
        val url = when (vendor) {
            ApiVendor.GPT -> "https://api.openai.com/v1/chat/completions"
            ApiVendor.Grok -> "https://api.x.ai/v1/chat/completions"
            ApiVendor.Mixtral -> "https://api.mistral.ai/v1/chat/completions"
            else -> error("Unsupported OpenAI-compatible vendor")
        }
        val messages = JSONArray()
            .put(JSONObject().put("role", "system").put("content", finalSystemPrompt))
        history.forEach { message ->
            val role = if (message.role == "model") "assistant" else message.role
            messages.put(JSONObject().put("role", role).put("content", message.content))
        }
        messages.put(JSONObject().put("role", "user").put("content", openAiUserContent(userInput, images)))

        val body = JSONObject()
            .put("model", persona.model)
            .put("messages", messages)
            .put("temperature", persona.temperature.toDouble())
            .put("max_tokens", 4096)

        val json = postJson(
            url = url,
            headers = mapOf("Authorization" to "Bearer $apiKey"),
            body = body
        )
        val content = extractOpenAiContent(json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message"))
        val usage = json.optJSONObject("usage")
        GeminiChatReply(
            text = content,
            promptTokenCount = usage?.optInt("prompt_tokens") ?: 0,
            responseTokenCount = usage?.optInt("completion_tokens") ?: 0,
            totalTokenCount = usage?.optInt("total_tokens") ?: 0
        )
    }

    private suspend fun sendClaude(
        apiKey: String,
        persona: PersonaEntity,
        history: List<MessageEntity>,
        userInput: String,
        finalSystemPrompt: String,
        images: List<Bitmap>
    ): GeminiChatReply = withContext(Dispatchers.IO) {
        val messages = JSONArray()
        history.forEach { message ->
            val role = if (message.role == "model") "assistant" else "user"
            messages.put(JSONObject().put("role", role).put("content", message.content))
        }
        messages.put(JSONObject().put("role", "user").put("content", claudeUserContent(userInput, images)))

        val body = JSONObject()
            .put("model", persona.model)
            .put("system", finalSystemPrompt)
            .put("messages", messages)
            .put("temperature", persona.temperature.toDouble())
            .put("max_tokens", 4096)

        val json = postJson(
            url = "https://api.anthropic.com/v1/messages",
            headers = mapOf(
                "x-api-key" to apiKey,
                "anthropic-version" to "2023-06-01"
            ),
            body = body
        )
        val content = extractClaudeText(json.getJSONArray("content"))
        val usage = json.optJSONObject("usage")
        val inputTokens = usage?.optInt("input_tokens") ?: 0
        val outputTokens = usage?.optInt("output_tokens") ?: 0
        GeminiChatReply(
            text = content,
            promptTokenCount = inputTokens,
            responseTokenCount = outputTokens,
            totalTokenCount = inputTokens + outputTokens
        )
    }

    private fun safetySettings(level: SafetyLevel): List<SafetySetting> {
        val threshold = when (level) {
            SafetyLevel.None -> BlockThreshold.NONE
            SafetyLevel.Low -> BlockThreshold.ONLY_HIGH
            SafetyLevel.Medium -> BlockThreshold.MEDIUM_AND_ABOVE
            SafetyLevel.High -> BlockThreshold.LOW_AND_ABOVE
        }
        return listOf(
            SafetySetting(HarmCategory.HARASSMENT, threshold),
            SafetySetting(HarmCategory.HATE_SPEECH, threshold),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, threshold),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, threshold)
        )
    }

    private fun openAiUserContent(userInput: String, images: List<Bitmap>): Any {
        if (images.isEmpty()) return userInput
        return JSONArray().apply {
            put(JSONObject().put("type", "text").put("text", userInput))
            images.forEach { image ->
                put(
                    JSONObject()
                        .put("type", "image_url")
                        .put(
                            "image_url",
                            JSONObject().put("url", "data:image/jpeg;base64,${image.toBase64Jpeg()}")
                        )
                )
            }
        }
    }

    private fun claudeUserContent(userInput: String, images: List<Bitmap>): Any {
        if (images.isEmpty()) return userInput
        return JSONArray().apply {
            put(JSONObject().put("type", "text").put("text", userInput))
            images.forEach { image ->
                put(
                    JSONObject()
                        .put("type", "image")
                        .put(
                            "source",
                            JSONObject()
                                .put("type", "base64")
                                .put("media_type", "image/jpeg")
                                .put("data", image.toBase64Jpeg())
                        )
                )
            }
        }
    }

    private fun geminiParts(userInput: String, images: List<Bitmap>): JSONArray {
        return JSONArray().apply {
            put(JSONObject().put("text", userInput))
            images.forEach { image ->
                put(
                    JSONObject().put(
                        "inlineData",
                        JSONObject()
                            .put("mimeType", "image/jpeg")
                            .put("data", image.toBase64Jpeg())
                    )
                )
            }
        }
    }

    private fun geminiRestSafetySettings(level: SafetyLevel): JSONArray {
        val threshold = when (level) {
            SafetyLevel.None -> "BLOCK_NONE"
            SafetyLevel.Low -> "BLOCK_ONLY_HIGH"
            SafetyLevel.Medium -> "BLOCK_MEDIUM_AND_ABOVE"
            SafetyLevel.High -> "BLOCK_LOW_AND_ABOVE"
        }
        return JSONArray().apply {
            listOf(
                "HARM_CATEGORY_HARASSMENT",
                "HARM_CATEGORY_HATE_SPEECH",
                "HARM_CATEGORY_SEXUALLY_EXPLICIT",
                "HARM_CATEGORY_DANGEROUS_CONTENT"
            ).forEach { category ->
                put(JSONObject().put("category", category).put("threshold", threshold))
            }
        }
    }

    private fun Bitmap.toBase64Jpeg(): String {
        val output = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 90, output)
        return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    }

    private fun extractGeminiRestText(candidate: JSONObject): String {
        val parts = candidate.optJSONObject("content")?.optJSONArray("parts") ?: return ""
        return buildString {
            for (index in 0 until parts.length()) {
                append(parts.optJSONObject(index)?.optString("text").orEmpty())
            }
        }
    }

    private fun appendGroundingSources(text: String, metadata: JSONObject?): String {
        val chunks = metadata?.optJSONArray("groundingChunks") ?: return text
        val sources = mutableListOf<Pair<String, String>>()
        for (index in 0 until chunks.length()) {
            val web = chunks.optJSONObject(index)?.optJSONObject("web") ?: continue
            val uri = web.optString("uri")
            if (uri.isBlank()) continue
            val title = web.optString("title").ifBlank { uri }
            sources += title to uri
        }
        if (sources.isEmpty()) return text
        val uniqueSources = sources.distinctBy { it.second }.take(5)
        return buildString {
            append(text.trimEnd())
            append("\n\nSources:")
            uniqueSources.forEachIndexed { index, (title, uri) ->
                append("\n[${index + 1}] $title - $uri")
            }
        }
    }

    private fun extractOpenAiContent(message: JSONObject): String {
        return when (val content = message.opt("content")) {
            is String -> content
            is JSONArray -> buildString {
                for (index in 0 until content.length()) {
                    val part = content.optJSONObject(index) ?: continue
                    if (part.optString("type") == "text") {
                        append(part.optString("text"))
                    }
                }
            }
            else -> ""
        }
    }

    private fun extractClaudeText(content: JSONArray): String {
        return buildString {
            for (index in 0 until content.length()) {
                val part = content.optJSONObject(index) ?: continue
                if (part.optString("type") == "text") {
                    append(part.optString("text"))
                }
            }
        }
    }

    private fun postJson(
        url: String,
        headers: Map<String, String>,
        body: JSONObject
    ): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 30_000
            readTimeout = 120_000
            setRequestProperty("Content-Type", "application/json")
            headers.forEach { (key, value) -> setRequestProperty(key, value) }
        }
        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(body.toString())
        }
        val stream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }
        val responseText = stream.bufferedReader().use { it.readText() }
        if (connection.responseCode !in 200..299) {
            error(responseText.take(300))
        }
        return JSONObject(responseText)
    }
}
