package com.yozora.aichat.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

data class PersonaEntity(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val avatarUri: String?,
    val systemPrompt: String,
    val model: String,
    val temperature: Float = 1.0f,
    val thinkingBudget: Int? = null
)

@Entity(tableName = "sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val title: String = "",
    val headerAvatarUri: String? = null,
    val headerAvatarScale: Float = 1.0f,
    val headerAvatarOffsetX: Float = 0f,
    val headerAvatarOffsetY: Float = 0f,
    val personaJson: String,
    val activeMemberId: String,
    val responseRounds: Int,
    val memoryEnabled: Boolean = true,
    val storyLore: String = "",
    val levelSystemEnabled: Boolean = false,
    val levelXp: Int = 0,
    val projectId: String? = null,
    val backgroundJson: String,
    val preview: String,
    val updatedAt: String,
    val sortOrder: Int
)

@Entity(
    tableName = "group_members",
    foreignKeys = [
        ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class GroupMemberEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val personaJson: String,
    val position: Int
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("chatId")]
)
data class MessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val chatId: String,
    val role: String,
    val content: String,
    val timestamp: Long,
    val speakerId: String? = null,
    val speakerName: String? = null,
    val imageUrisJson: String = "[]",
    val remoteImageUrl: String? = null,
    val time: String = "",
    val position: Int = 0
)

@Entity(
    tableName = "tts_audio_cache",
    indices = [
        Index(
            value = [
                "messageId",
                "sourceHash",
                "preparedTextHash",
                "provider",
                "voiceId",
                "modelId"
            ],
            unique = true
        ),
        Index("messageId", "sourceHash", "provider", "voiceId", "modelId")
    ]
)
data class TtsAudioCacheEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val messageId: String,
    val sessionId: String,
    val sourceHash: String,
    val cleanedTextHash: String,
    val preparedTextHash: String,
    val provider: String,
    val voiceId: String,
    val modelId: String,
    val language: String,
    val audioFilePath: String,
    val characterCount: Int,
    val durationMs: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
