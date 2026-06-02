package com.yozora.aichat.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ChatSessionEntity::class,
        GroupMemberEntity::class,
        MessageEntity::class,
        TtsAudioCacheEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zora.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { instance = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `tts_audio_cache` (
                        `id` TEXT NOT NULL,
                        `messageId` TEXT NOT NULL,
                        `sessionId` TEXT NOT NULL,
                        `sourceHash` TEXT NOT NULL,
                        `cleanedTextHash` TEXT NOT NULL,
                        `preparedTextHash` TEXT NOT NULL,
                        `provider` TEXT NOT NULL,
                        `voiceId` TEXT NOT NULL,
                        `modelId` TEXT NOT NULL,
                        `language` TEXT NOT NULL,
                        `audioFilePath` TEXT NOT NULL,
                        `characterCount` INTEGER NOT NULL,
                        `durationMs` INTEGER,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    `index_tts_audio_cache_messageId_sourceHash_preparedTextHash_provider_voiceId_modelId`
                    ON `tts_audio_cache` (`messageId`, `sourceHash`, `preparedTextHash`, `provider`, `voiceId`, `modelId`)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    `index_tts_audio_cache_messageId_sourceHash_provider_voiceId_modelId`
                    ON `tts_audio_cache` (`messageId`, `sourceHash`, `provider`, `voiceId`, `modelId`)
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `sessions` ADD COLUMN `memoryEnabled` INTEGER NOT NULL DEFAULT 1"
                )
            }
        }
    }
}
