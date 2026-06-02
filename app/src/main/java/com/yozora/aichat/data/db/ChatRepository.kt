package com.yozora.aichat.data.db

import android.content.Context
import android.net.Uri
import com.yozora.aichat.ui.chat.ApiVendor
import com.yozora.aichat.ui.chat.ChatBackground
import com.yozora.aichat.ui.chat.ChatMessage
import com.yozora.aichat.ui.chat.ChatSession
import com.yozora.aichat.ui.chat.GroupMember
import com.yozora.aichat.ui.chat.InstructionMode
import com.yozora.aichat.ui.chat.PersonaUiState
import com.yozora.aichat.ui.chat.SafetyLevel
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class ChatRepository private constructor(
    private val dao: ChatDao
) {
    suspend fun hasSessions(): Boolean = dao.sessionCount() > 0

    suspend fun loadSessions(): List<ChatSession> {
        return dao.sessions().map { sessionEntity ->
            val members = dao.membersForSession(sessionEntity.id)
                .map { it.toGroupMember() }
                .ifEmpty {
                    listOf(GroupMember(persona = sessionEntity.personaJson.toPersonaUiState()))
                }
            val activeId = sessionEntity.activeMemberId
                .takeIf { id -> members.any { it.id == id } }
                ?: members.first().id
            val activePersona = members.firstOrNull { it.id == activeId }?.persona
                ?: members.first().persona

            ChatSession(
                id = sessionEntity.id,
                persona = activePersona,
                members = members,
                activeMemberId = activeId,
                responseRounds = sessionEntity.responseRounds.coerceIn(1, 3),
                memoryEnabled = sessionEntity.memoryEnabled,
                background = sessionEntity.backgroundJson.toChatBackground(),
                preview = sessionEntity.preview.ifBlank { "No messages yet" },
                updatedAt = sessionEntity.updatedAt.ifBlank { "Now" },
                messages = dao.messagesForSession(sessionEntity.id).map { it.toChatMessage() }
            )
        }
    }

    suspend fun replaceAll(sessions: List<ChatSession>) {
        val sessionEntities = sessions.mapIndexed { index, session ->
            ChatSessionEntity(
                id = session.id,
                personaJson = session.persona.toJsonString(),
                activeMemberId = session.activeMemberId,
                responseRounds = session.responseRounds.coerceIn(1, 3),
                memoryEnabled = session.memoryEnabled,
                backgroundJson = session.background.toJsonString(),
                preview = session.preview,
                updatedAt = session.updatedAt,
                sortOrder = index
            )
        }
        val memberEntities = sessions.flatMap { session ->
            session.normalizedMembersForDb().mapIndexed { index, member ->
                GroupMemberEntity(
                    id = member.id,
                    sessionId = session.id,
                    personaJson = member.persona.toJsonString(),
                    position = index
                )
            }
        }
        val now = System.currentTimeMillis()
        val messageEntities = sessions.flatMap { session ->
            session.messages
                .filterNot { it.isImageLoading }
                .mapIndexed { index, message ->
                    MessageEntity(
                        id = message.id,
                        chatId = session.id,
                        role = message.role,
                        content = message.content,
                        timestamp = now + index,
                        speakerId = message.speakerId,
                        speakerName = message.speakerName,
                        imageUrisJson = JSONArray().apply {
                            message.imageUris.forEach { uri -> put(uri.toString()) }
                        }.toString(),
                        remoteImageUrl = message.remoteImageUrl,
                        time = message.time,
                        position = index
                    )
                }
        }
        dao.replaceAll(sessionEntities, memberEntities, messageEntities)
    }

    suspend fun latestTtsAudioCache(
        messageId: String,
        sourceHash: String,
        provider: String,
        voiceId: String,
        modelId: String
    ): TtsAudioCacheEntity? {
        return dao.latestTtsAudioCache(
            messageId = messageId,
            sourceHash = sourceHash,
            provider = provider,
            voiceId = voiceId,
            modelId = modelId
        )
    }

    suspend fun exactTtsAudioCache(
        messageId: String,
        sourceHash: String,
        preparedTextHash: String,
        provider: String,
        voiceId: String,
        modelId: String
    ): TtsAudioCacheEntity? {
        return dao.exactTtsAudioCache(
            messageId = messageId,
            sourceHash = sourceHash,
            preparedTextHash = preparedTextHash,
            provider = provider,
            voiceId = voiceId,
            modelId = modelId
        )
    }

    suspend fun saveTtsAudioCache(cache: TtsAudioCacheEntity) {
        dao.insertTtsAudioCache(cache)
    }

    suspend fun deleteTtsAudioCache(id: String) {
        dao.deleteTtsAudioCache(id)
    }

    companion object {
        @Volatile
        private var instance: ChatRepository? = null

        fun get(context: Context): ChatRepository {
            return instance ?: synchronized(this) {
                instance ?: ChatRepository(AppDatabase.get(context).chatDao())
                    .also { instance = it }
            }
        }
    }
}

private fun ChatSession.normalizedMembersForDb(): List<GroupMember> {
    return members.ifEmpty { listOf(GroupMember(persona = persona)) }
}

private fun GroupMemberEntity.toGroupMember(): GroupMember {
    return GroupMember(
        id = id,
        persona = personaJson.toPersonaUiState()
    )
}

private fun MessageEntity.toChatMessage(): ChatMessage {
    val imageUris = runCatching {
        val raw = JSONArray(imageUrisJson)
        buildList {
            for (index in 0 until raw.length()) {
                raw.optString(index).takeIf { it.isNotBlank() }?.let { add(Uri.parse(it)) }
            }
        }
    }.getOrDefault(emptyList())

    return ChatMessage(
        id = id,
        role = role,
        content = content,
        speakerId = speakerId,
        speakerName = speakerName,
        imageUris = imageUris.take(6),
        remoteImageUrl = remoteImageUrl,
        isImageLoading = false,
        time = time.ifBlank { "" }
    )
}

private fun PersonaUiState.toJsonString(): String {
    return JSONObject()
        .put("displayName", displayName)
        .put("tagline", tagline)
        .put("instructionMode", instructionMode.name)
        .put("beginnerRole", beginnerRole)
        .put("beginnerStyle", beginnerStyle)
        .put("beginnerLimits", beginnerLimits)
        .put("instructionPrompt", instructionPrompt)
        .put("vendor", vendor.id)
        .put("model", model)
        .put("safetyLevel", safetyLevel.name)
        .put("temperature", temperature.toDouble())
        .put("avatarUri", avatarUri?.toString() ?: JSONObject.NULL)
        .put("avatarScale", avatarScale.toDouble())
        .put("avatarOffsetX", avatarOffsetX.toDouble())
        .put("avatarOffsetY", avatarOffsetY.toDouble())
        .put("traits", JSONArray().apply { traits.forEach { put(it) } })
        .toString()
}

private fun String.toPersonaUiState(): PersonaUiState {
    val json = safeJsonObject(this)
    val vendor = ApiVendor.entries.firstOrNull { it.id == json.optString("vendor") } ?: ApiVendor.Google
    val restoredPrompt = json.optString("instructionPrompt")
    val restoredInstructionMode = InstructionMode.entries.firstOrNull { it.name == json.optString("instructionMode") }
        ?: if (restoredPrompt.isNotBlank()) InstructionMode.Advanced else InstructionMode.Beginner
    val traitsJson = json.optJSONArray("traits")
    val traits = buildList {
        if (traitsJson != null) {
            for (index in 0 until traitsJson.length()) {
                traitsJson.optString(index).takeIf { it.isNotBlank() }?.let { add(it) }
            }
        }
    }

    return PersonaUiState(
        displayName = json.optString("displayName").ifBlank { "New Persona" },
        tagline = json.optString("tagline").ifBlank { "Custom roleplay companion" },
        instructionMode = restoredInstructionMode,
        beginnerRole = json.optString("beginnerRole"),
        beginnerStyle = json.optString("beginnerStyle"),
        beginnerLimits = json.optString("beginnerLimits"),
        instructionPrompt = restoredPrompt,
        vendor = vendor,
        model = json.optString("model").ifBlank { vendor.defaultModel },
        safetyLevel = SafetyLevel.entries.firstOrNull { it.name == json.optString("safetyLevel") } ?: SafetyLevel.None,
        temperature = json.optDouble("temperature", 1.0).toFloat().coerceIn(0f, 2f),
        avatarUri = json.optNullableString("avatarUri")?.let(Uri::parse),
        avatarScale = json.optDouble("avatarScale", 1.0).toFloat().coerceIn(1f, 4f),
        avatarOffsetX = json.optDouble("avatarOffsetX", 0.0).toFloat().coerceIn(-180f, 180f),
        avatarOffsetY = json.optDouble("avatarOffsetY", 0.0).toFloat().coerceIn(-180f, 180f),
        traits = traits.ifEmpty { listOf("Empathetic", "Encouraging", "Curious", "Calm") }
    )
}

private fun ChatBackground.toJsonString(): String {
    return when (this) {
        ChatBackground.DarkMode -> JSONObject().put("type", "dark")
        ChatBackground.LightMode -> JSONObject().put("type", "light")
        ChatBackground.GreyMode -> JSONObject().put("type", "grey")
        ChatBackground.PureWhite -> JSONObject().put("type", "white")
        ChatBackground.PureBlack -> JSONObject().put("type", "black")
        ChatBackground.PresetBlack -> JSONObject().put("type", "preset_black")
        ChatBackground.PresetWhite -> JSONObject().put("type", "preset_white")
        is ChatBackground.CustomImage -> JSONObject()
            .put("type", "custom")
            .put("uri", uri.toString())
    }.toString()
}

private fun String.toChatBackground(): ChatBackground {
    val json = safeJsonObject(this)
    return when (json.optString("type")) {
        "light" -> ChatBackground.LightMode
        "grey" -> ChatBackground.GreyMode
        "white" -> ChatBackground.PureWhite
        "black" -> ChatBackground.PureBlack
        "preset_black" -> ChatBackground.PresetBlack
        "preset_white" -> ChatBackground.PresetWhite
        "custom" -> json.optNullableString("uri")?.let { ChatBackground.CustomImage(Uri.parse(it)) } ?: ChatBackground.DarkMode
        else -> ChatBackground.DarkMode
    }
}

private fun safeJsonObject(raw: String): JSONObject {
    return runCatching { JSONObject(raw.ifBlank { "{}" }) }.getOrElse { JSONObject() }
}

private fun JSONObject.optNullableString(name: String): String? {
    if (!has(name) || isNull(name)) return null
    return optString(name).takeIf { it.isNotBlank() }
}
