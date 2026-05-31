package com.yozora.aichat.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

class TavilyRepository {
    suspend fun searchContext(
        apiKey: String,
        query: String
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            val trimmedKey = apiKey.trim()
            val trimmedQuery = query.trim()
            if (trimmedKey.isEmpty() || trimmedQuery.isEmpty()) return@runCatching null

            val body = JSONObject()
                .put("api_key", trimmedKey)
                .put("query", trimmedQuery)
                .put("search_depth", "basic")
                .put("include_answer", false)
                .put("max_results", 4)

            val json = postSearch(body) ?: return@runCatching null
            val results = json.optJSONArray("results") ?: return@runCatching null
            if (results.length() == 0) return@runCatching null

            buildString {
                append("[Web Search Results - Today: ${LocalDate.now()}]\n")
                var sourceNumber = 1
                for (index in 0 until results.length()) {
                    val item = results.optJSONObject(index) ?: continue
                    val title = item.optString("title").ifBlank { "Untitled source" }
                    val content = item.optString("content").cleanSnippet()
                    val url = item.optString("url")
                    if (content.isBlank() && url.isBlank()) continue

                    append("[Source $sourceNumber: $title]\n")
                    if (content.isNotBlank()) {
                        append(content)
                        append('\n')
                    }
                    if (url.isNotBlank()) {
                        append("URL: ")
                        append(url)
                        append('\n')
                    }
                    sourceNumber++
                }
                append("[End of search results]\n")
                append("User question: ")
                append(trimmedQuery)
            }.takeIf { it.contains("[Source ") }
        }.getOrNull()
    }

    private fun postSearch(body: JSONObject): JSONObject? {
        val connection = (URL("https://api.tavily.com/search").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 20_000
            readTimeout = 45_000
            setRequestProperty("Content-Type", "application/json")
        }
        return try {
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(body.toString())
            }
            if (connection.responseCode !in 200..299) {
                return null
            }
            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            JSONObject(responseText)
        } finally {
            connection.disconnect()
        }
    }

    private fun String.cleanSnippet(): String {
        return trim()
            .replace(Regex("\\s+"), " ")
            .take(1_200)
    }
}
