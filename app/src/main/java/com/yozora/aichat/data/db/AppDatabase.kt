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
    version = 8,
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
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8
                    )
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

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `sessions` ADD COLUMN `projectId` TEXT"
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `sessions` ADD COLUMN `title` TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `sessions` ADD COLUMN `headerAvatarUri` TEXT")
                db.execSQL("ALTER TABLE `sessions` ADD COLUMN `headerAvatarScale` REAL NOT NULL DEFAULT 1.0")
                db.execSQL("ALTER TABLE `sessions` ADD COLUMN `headerAvatarOffsetX` REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE `sessions` ADD COLUMN `headerAvatarOffsetY` REAL NOT NULL DEFAULT 0.0")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `sessions` ADD COLUMN `levelSystemEnabled` INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE `sessions` ADD COLUMN `levelXp` INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `sessions` ADD COLUMN `storyLore` TEXT NOT NULL DEFAULT ''"
                )
            }
        }
    }
}
