package com.yozora.aichat.data

import android.content.Context

data class SkillDocument(
    val fileName: String,
    val content: String
)

class SkillRepository private constructor(context: Context) {
    val skills: List<SkillDocument> = loadSkills(context.applicationContext)

    val combinedContent: String = skills.joinToString("\n\n---\n\n") { skill ->
        "[Skill: ${skill.fileName}]\n${skill.content.trim()}"
    }

    private fun loadSkills(context: Context): List<SkillDocument> {
        return context.assets.list(SKILLS_ASSET_DIRECTORY)
            .orEmpty()
            .asSequence()
            .filter { fileName -> fileName.endsWith(".md", ignoreCase = true) }
            .sorted()
            .mapNotNull { fileName ->
                runCatching {
                    val content = context.assets
                        .open("$SKILLS_ASSET_DIRECTORY/$fileName")
                        .bufferedReader()
                        .use { reader -> reader.readText() }
                    SkillDocument(fileName = fileName, content = content)
                }.getOrNull()
            }
            .filter { skill -> skill.content.isNotBlank() }
            .toList()
    }

    companion object {
        private const val SKILLS_ASSET_DIRECTORY = "skills"

        @Volatile
        private var instance: SkillRepository? = null

        fun get(context: Context): SkillRepository {
            return instance ?: synchronized(this) {
                instance ?: SkillRepository(context).also { instance = it }
            }
        }
    }
}
