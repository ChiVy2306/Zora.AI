package com.yozora.aichat.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import com.yozora.aichat.data.SkillRepository
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

data class GeminiToolCall(
    val name: String,
    val args: JSONObject
)

data class GeminiToolPlan(
    val text: String,
    val functionCall: GeminiToolCall?,
    val promptTokenCount: Int,
    val responseTokenCount: Int,
    val totalTokenCount: Int
) {
    fun toReply(): GeminiChatReply {
        return GeminiChatReply(
            text = text,
            promptTokenCount = promptTokenCount,
            responseTokenCount = responseTokenCount,
            totalTokenCount = totalTokenCount
        )
    }
}

private const val FALLBACK_MASTER_PROMPT = """
You are a customizable AI companion. Follow the persona instruction prompt for the active session, keep replies immersive and useful, and ask for clarification when the user request is unclear.
"""

class GeminiChatService(
    private val skillRepository: SkillRepository? = null
) {
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
        val finalSystemPrompt = finalSystemPrompt(masterPrompt, persona)

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

    suspend fun planGeminiToolUse(
        apiKey: String,
        persona: PersonaEntity,
        history: List<MessageEntity>,
        userInput: String,
        safetyLevel: SafetyLevel,
        images: List<Bitmap>,
        enabledTools: Set<String>,
        masterPrompt: String? = null
    ): Result<GeminiToolPlan> = runCatching {
        planToolUse(
            apiKey = apiKey,
            vendor = ApiVendor.Google,
            persona = persona,
            history = history,
            userInput = userInput,
            safetyLevel = safetyLevel,
            images = images,
            enabledTools = enabledTools,
            masterPrompt = masterPrompt
        ).getOrThrow()
    }

    suspend fun planToolUse(
        apiKey: String,
        vendor: ApiVendor,
        persona: PersonaEntity,
        history: List<MessageEntity>,
        userInput: String,
        safetyLevel: SafetyLevel,
        images: List<Bitmap>,
        enabledTools: Set<String>,
        masterPrompt: String? = null
    ): Result<GeminiToolPlan> = runCatching {
        require(enabledTools.isNotEmpty()) { "No tools enabled." }
        val promptedSystem = finalSystemPrompt(masterPrompt, persona) + "\n\n" + toolUseInstruction(enabledTools)
        when (vendor) {
            ApiVendor.Google -> sendGeminiToolPlanning(
                apiKey = apiKey,
                persona = persona,
                history = history,
                userInput = userInput,
                finalSystemPrompt = promptedSystem,
                safetyLevel = safetyLevel,
                images = images,
                enabledTools = enabledTools
            )

            ApiVendor.Claude -> sendClaudeToolPlanning(
                apiKey = apiKey,
                persona = persona,
                history = history,
                userInput = userInput,
                finalSystemPrompt = promptedSystem,
                images = images,
                enabledTools = enabledTools
            )

            ApiVendor.GPT,
            ApiVendor.Grok,
            ApiVendor.Mixtral -> sendOpenAiToolPlanning(
                apiKey = apiKey,
                vendor = vendor,
                persona = persona,
                history = history,
                userInput = userInput,
                finalSystemPrompt = promptedSystem,
                images = images,
                enabledTools = enabledTools
            )
        }
    }

    suspend fun pickRule34Tags(
        apiKey: String,
        userRequest: String,
        presetHint: String = ""
    ): Result<String> = runCatching {
        val model = GenerativeModel(
            modelName = "gemini-3.1-flash-lite",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.1f
                maxOutputTokens = 32
            },
            safetySettings = safetySettings(SafetyLevel.None)
        )
        val prompt = """
            Convert this image request into Rule34/Danbooru search tags.
            Preset guidance: $presetHint
            Rules:
            - Output ONLY the tags, space-separated
            - Use underscores for spaces within a tag (e.g. long_hair, serious_face)
            - Output 2 to 4 tags
            - Put the most important request-specific tag first
            - Put generic support tags like 1girl, 1boy, solo, looking_at_viewer last
            - Prefer common Booru tags over invented mood or prose words
            - Do NOT include rating tags
            - Example output: maid black_hair large_breasts 1girl
            Request: "$userRequest"
        """.trimIndent()
        val response = model.generateContent(prompt)
        parseRule34Tags(response.text.orEmpty()).ifBlank {
            fallbackRule34Tags(userRequest)
        }
    }

    suspend fun prepareJapaneseSpeechText(
        apiKey: String,
        cleanedText: String
    ): Result<String> = runCatching {
        val model = GenerativeModel(
            modelName = "gemini-3.1-flash-lite",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.2f
                maxOutputTokens = 2048
            },
            safetySettings = safetySettings(SafetyLevel.None)
        )
        val prompt = """
            Convert the following roleplay dialogue into natural spoken Japanese for text-to-speech.

            Rules:
            - Output only the final Japanese speech text.
            - Keep only dialogue that should be spoken aloud.
            - Do not include narration, actions, thoughts, role labels, markdown, or explanations.
            - Preserve emotional tone and intimacy.
            - Make the Japanese sound natural when spoken.
            - Do not add new story content.

            Text:
            $cleanedText
        """.trimIndent()
        cleanSpeechPreparation(model.generateContent(prompt).text.orEmpty())
            .ifBlank { cleanedText.trim() }
    }

    private suspend fun sendGemini(
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
            val role = when (message.role) {
                "model" -> "model"
                "user" -> "user"
                else -> return@forEach
            }
            contents.put(
                JSONObject()
                    .put("role", role)
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
            .put("generationConfig", geminiGenerationConfig(persona, 8192))
            .put("safetySettings", geminiRestSafetySettings(safetyLevel))

        val json = postJson(
            url = "https://generativelanguage.googleapis.com/v1beta/models/${persona.model}:generateContent",
            headers = mapOf("x-goog-api-key" to apiKey),
            body = body
        )
        val candidate = json.optJSONArray("candidates")?.optJSONObject(0)
            ?: error("No Gemini response candidate returned.")
        val usage = json.optJSONObject("usageMetadata")
        GeminiChatReply(
            text = extractGeminiRestText(candidate),
            promptTokenCount = usage?.optInt("promptTokenCount") ?: 0,
            responseTokenCount = usage?.optInt("candidatesTokenCount") ?: 0,
            totalTokenCount = usage?.optInt("totalTokenCount") ?: 0
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
            .put("generationConfig", geminiGenerationConfig(persona, 8192))
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

    private suspend fun sendGeminiToolPlanning(
        apiKey: String,
        persona: PersonaEntity,
        history: List<MessageEntity>,
        userInput: String,
        finalSystemPrompt: String,
        safetyLevel: SafetyLevel,
        images: List<Bitmap>,
        enabledTools: Set<String>
    ): GeminiToolPlan = withContext(Dispatchers.IO) {
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
            .put("generationConfig", geminiGenerationConfig(persona, 8192))
            .put("safetySettings", geminiRestSafetySettings(safetyLevel))
            .put(
                "tools",
                JSONArray().put(
                    JSONObject().put("functionDeclarations", geminiFunctionDeclarations(enabledTools))
                )
            )

        val json = postJson(
            url = "https://generativelanguage.googleapis.com/v1beta/models/${persona.model}:generateContent",
            headers = mapOf("x-goog-api-key" to apiKey),
            body = body
        )
        val candidate = json.getJSONArray("candidates").getJSONObject(0)
        val parts = candidate.optJSONObject("content")?.optJSONArray("parts") ?: JSONArray()
        var functionCall: GeminiToolCall? = null
        val responseText = buildString {
            for (index in 0 until parts.length()) {
                val part = parts.optJSONObject(index) ?: continue
                part.optString("text").takeIf { it.isNotBlank() }?.let { append(it) }
                val call = part.optJSONObject("functionCall") ?: continue
                functionCall = GeminiToolCall(
                    name = call.optString("name"),
                    args = call.optJSONObject("args") ?: JSONObject()
                )
            }
        }
        val usage = json.optJSONObject("usageMetadata")
        GeminiToolPlan(
            text = responseText,
            functionCall = functionCall?.takeIf { it.name.isNotBlank() },
            promptTokenCount = usage?.optInt("promptTokenCount") ?: 0,
            responseTokenCount = usage?.optInt("candidatesTokenCount") ?: 0,
            totalTokenCount = usage?.optInt("totalTokenCount") ?: 0
        )
    }

    private suspend fun sendOpenAiToolPlanning(
        apiKey: String,
        vendor: ApiVendor,
        persona: PersonaEntity,
        history: List<MessageEntity>,
        userInput: String,
        finalSystemPrompt: String,
        images: List<Bitmap>,
        enabledTools: Set<String>
    ): GeminiToolPlan = withContext(Dispatchers.IO) {
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
            .put("tools", openAiTools(enabledTools))
            .put("tool_choice", "auto")

        val json = postJson(
            url = url,
            headers = mapOf("Authorization" to "Bearer $apiKey"),
            body = body
        )
        val message = json.getJSONArray("choices").getJSONObject(0).getJSONObject("message")
        val toolCall = message.optJSONArray("tool_calls")
            ?.optJSONObject(0)
            ?.optJSONObject("function")
            ?.let { function ->
                GeminiToolCall(
                    name = function.optString("name"),
                    args = parseJsonObject(function.optString("arguments"))
                )
            }
        val usage = json.optJSONObject("usage")
        GeminiToolPlan(
            text = extractOpenAiContent(message),
            functionCall = toolCall?.takeIf { it.name.isNotBlank() },
            promptTokenCount = usage?.optInt("prompt_tokens") ?: 0,
            responseTokenCount = usage?.optInt("completion_tokens") ?: 0,
            totalTokenCount = usage?.optInt("total_tokens") ?: 0
        )
    }

    private suspend fun sendClaudeToolPlanning(
        apiKey: String,
        persona: PersonaEntity,
        history: List<MessageEntity>,
        userInput: String,
        finalSystemPrompt: String,
        images: List<Bitmap>,
        enabledTools: Set<String>
    ): GeminiToolPlan = withContext(Dispatchers.IO) {
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
            .put("tools", claudeTools(enabledTools))
            .put("tool_choice", JSONObject().put("type", "auto"))

        val json = postJson(
            url = "https://api.anthropic.com/v1/messages",
            headers = mapOf(
                "x-api-key" to apiKey,
                "anthropic-version" to "2023-06-01"
            ),
            body = body
        )
        val content = json.getJSONArray("content")
        var toolCall: GeminiToolCall? = null
        val text = buildString {
            for (index in 0 until content.length()) {
                val part = content.optJSONObject(index) ?: continue
                when (part.optString("type")) {
                    "text" -> append(part.optString("text"))
                    "tool_use" -> toolCall = GeminiToolCall(
                        name = part.optString("name"),
                        args = part.optJSONObject("input") ?: JSONObject()
                    )
                }
            }
        }
        val usage = json.optJSONObject("usage")
        val inputTokens = usage?.optInt("input_tokens") ?: 0
        val outputTokens = usage?.optInt("output_tokens") ?: 0
        GeminiToolPlan(
            text = text,
            functionCall = toolCall?.takeIf { it.name.isNotBlank() },
            promptTokenCount = inputTokens,
            responseTokenCount = outputTokens,
            totalTokenCount = inputTokens + outputTokens
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

    private fun finalSystemPrompt(masterPrompt: String?, persona: PersonaEntity): String {
        val baseMasterPrompt = masterPrompt?.takeIf { it.isNotBlank() } ?: FALLBACK_MASTER_PROMPT
        return listOfNotNull(
            baseMasterPrompt.trimIndent(),
            skillRepository?.combinedContent?.takeIf { it.isNotBlank() },
            persona.systemPrompt.trim()
        ).joinToString("\n\n---\n\n")
    }

    private fun toolUseInstruction(enabledTools: Set<String>): String {
        return buildString {
            appendLine("App tool policy:")
            appendLine("- The following function tools are enabled by the user in the app UI and relevant to the current message: ${enabledTools.joinToString()}.")
            appendLine("- If a tool is listed here, that tool is ON. If a tool is not listed here, treat it as OFF or unavailable.")
            appendLine("- Decide tool use only from the current user message, not from an older request in chat history.")
            appendLine("- If the user is only chatting, answer normally without calling a tool.")
            if ("web_search" in enabledTools) {
                appendLine("- Call web_search only when the current message asks for current, recent, factual, source-backed, or explicitly searched information.")
            }
            if ("anime_image_search" in enabledTools) {
                appendLine("- Call anime_image_search only when the current message asks you to show, find, fetch, get, or display an anime-style image.")
            }
            appendLine("- Do not claim a tool was used unless you call it.")
        }
    }

    private fun geminiFunctionDeclarations(enabledTools: Set<String>): JSONArray {
        return JSONArray().apply {
            if ("web_search" in enabledTools) {
                put(
                    JSONObject()
                        .put("name", "web_search")
                        .put("description", "Search the web for current or source-backed information.")
                        .put(
                            "parameters",
                            JSONObject()
                                .put("type", "OBJECT")
                                .put(
                                    "properties",
                                    JSONObject().put(
                                        "query",
                                        JSONObject()
                                            .put("type", "STRING")
                                            .put("description", "The search query to retrieve web context for.")
                                    )
                                )
                                .put("required", JSONArray().put("query"))
                        )
                )
            }
            if ("anime_image_search" in enabledTools) {
                put(
                    JSONObject()
                        .put("name", "anime_image_search")
                        .put("description", "Find an anime-style image matching the user's request.")
                        .put(
                            "parameters",
                            JSONObject()
                                .put("type", "OBJECT")
                                .put(
                                    "properties",
                                    JSONObject()
                                        .put(
                                            "request",
                                            JSONObject()
                                                .put("type", "STRING")
                                                .put("description", "The user's image request in natural language.")
                                        )
                                        .put(
                                            "preset",
                                            JSONObject()
                                                .put("type", "STRING")
                                                .put("description", "Optional image preset: auto, portrait, character, or explicit.")
                                        )
                                )
                                .put("required", JSONArray().put("request"))
                        )
                )
            }
        }
    }

    private fun openAiTools(enabledTools: Set<String>): JSONArray {
        return JSONArray().apply {
            geminiFunctionDeclarations(enabledTools).forEachObject { declaration ->
                put(
                    JSONObject()
                        .put("type", "function")
                        .put("function", declaration)
                )
            }
        }
    }

    private fun claudeTools(enabledTools: Set<String>): JSONArray {
        return JSONArray().apply {
            geminiFunctionDeclarations(enabledTools).forEachObject { declaration ->
                put(
                    JSONObject()
                        .put("name", declaration.optString("name"))
                        .put("description", declaration.optString("description"))
                        .put("input_schema", declaration.optJSONObject("parameters") ?: JSONObject())
                )
            }
        }
    }

    private fun parseRule34Tags(raw: String): String {
        return raw
            .trim()
            .substringBefore('\n')
            .lowercase(Locale.US)
            .replace(',', ' ')
            .split(Regex("\\s+"))
            .map { tag ->
                tag.trim('"', '\'', '`', ':', '.', ';')
                    .replace('-', '_')
                    .replace(Regex("[^a-z0-9_]+"), "_")
                    .trim('_')
            }
            .filter { tag -> tag.isNotBlank() && !tag.startsWith("rating") }
            .take(4)
            .joinToString(" ")
    }

    private fun cleanSpeechPreparation(raw: String): String {
        return raw
            .replace(Regex("(?s)```.*?```"), "")
            .replace(Regex("^\\s*日本語\\s*[:：]\\s*", RegexOption.IGNORE_CASE), "")
            .trim()
            .trim('"', '\'', '`')
            .lines()
            .map { line -> line.trim() }
            .filter { it.isNotBlank() }
            .joinToString("\n")
    }

    fun fallbackRule34Tags(userRequest: String): String {
        val request = userRequest.lowercase(Locale.US)
        val mappedTags = linkedSetOf<String>()
        val phraseMap = listOf(
            "school uniform" to "school_uniform",
            "bunny girl" to "bunny_girl",
            "cat girl" to "cat_girl",
            "catgirl" to "cat_girl",
            "maid" to "maid",
            "nurse" to "nurse",
            "teacher" to "teacher",
            "secretary" to "secretary",
            "uniform" to "uniform",
            "kimono" to "kimono",
            "swimsuit" to "swimsuit",
            "bikini" to "bikini",
            "stockings" to "stockings",
            "thighhighs" to "thighhighs",
            "glasses" to "glasses",
            "white hair" to "white_hair",
            "black hair" to "black_hair",
            "blonde" to "blonde_hair",
            "red hair" to "red_hair",
            "blue hair" to "blue_hair",
            "pink hair" to "pink_hair",
            "long hair" to "long_hair",
            "short hair" to "short_hair",
            "large breasts" to "large_breasts",
            "big boobs" to "large_breasts",
            "oppai" to "large_breasts",
            "ass" to "ass",
            "feet" to "feet",
            "oral" to "fellatio",
            "blowjob" to "fellatio",
            "genshin" to "genshin_impact",
            "raiden" to "raiden_shogun",
            "ayaka" to "kamisato_ayaka",
            "marin" to "kitagawa_marin",
            "rem" to "rem_(re:zero)"
        )
        phraseMap.forEach { (needle, tag) ->
            if (request.contains(needle)) mappedTags += tag
        }
        request
            .replace(Regex("[^a-z0-9_\\s-]+"), " ")
            .split(Regex("\\s+"))
            .map { token -> token.replace('-', '_').trim('_') }
            .filter { token ->
                token.length > 2 &&
                    token !in setOf("show", "give", "want", "with", "that", "this", "girl", "anime", "image", "picture", "photo", "please")
            }
            .forEach { token -> mappedTags += token }
        return mappedTags.take(4).joinToString(" ").ifBlank { "1girl" }
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

    private fun geminiGenerationConfig(persona: PersonaEntity, maxOutputTokens: Int): JSONObject {
        return JSONObject()
            .put("temperature", persona.temperature.toDouble())
            .put("maxOutputTokens", maxOutputTokens)
            .apply {
                persona.thinkingBudget
                    ?.takeIf { budget -> budget > 0 }
                    ?.let { budget ->
                        put("thinkingConfig", JSONObject().put("thinkingBudget", budget))
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
            val apiMessage = apiErrorMessage(responseText).ifBlank { responseText.take(160) }
            error("HTTP ${connection.responseCode}: $apiMessage")
        }
        return JSONObject(responseText)
    }

    private fun apiErrorMessage(responseText: String): String {
        return runCatching {
            JSONObject(responseText)
                .optJSONObject("error")
                ?.optString("message")
                .orEmpty()
        }.getOrDefault("")
    }

    private fun parseJsonObject(raw: String): JSONObject {
        return runCatching { JSONObject(raw.ifBlank { "{}" }) }.getOrElse { JSONObject() }
    }

    private inline fun JSONArray.forEachObject(block: (JSONObject) -> Unit) {
        for (index in 0 until length()) {
            optJSONObject(index)?.let(block)
        }
    }
}
