package com.yozora.aichat.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class WaifuImageRepository {
    suspend fun randomPortraitImageUrl(
        token: String,
        tag: String
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            val trimmedToken = token.trim()
            val trimmedTag = tag.trim()
            if (trimmedToken.isEmpty() || trimmedTag.isEmpty()) return@runCatching null

            val encodedTag = URLEncoder.encode(trimmedTag, "UTF-8")
            val url = URL(
                "https://api.waifu.im/images" +
                    "?includedTags=$encodedTag" +
                    "&isNsfw=true" +
                    "&orientation=PORTRAIT"
            )
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 20_000
                readTimeout = 45_000
                setRequestProperty("Authorization", "Bearer $trimmedToken")
                setRequestProperty("Accept", "application/json")
            }
            try {
                if (connection.responseCode !in 200..299) {
                    return@runCatching null
                }
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val items = JSONObject(responseText).optJSONArray("images")
                    ?: JSONObject(responseText).optJSONArray("items")
                    ?: return@runCatching null
                items.optJSONObject(0)?.optString("url")?.takeIf { it.isNotBlank() }
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
    }
}
