package com.yozora.aichat.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "personas")
data class PersonaEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val avatarUri: String?,
    val systemPrompt: String,
    val model: String,
    val temperature: Float = 1.0f
)

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val personaId: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val chatId: String,
    val role: String,
    val content: String,
    val timestamp: Long
)
