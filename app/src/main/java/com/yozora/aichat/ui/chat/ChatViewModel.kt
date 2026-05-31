package com.yozora.aichat.ui.chat

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yozora.aichat.data.datastore.ApiKeyManager
import com.yozora.aichat.data.datastore.settingsDataStore
import com.yozora.aichat.data.db.MessageEntity
import com.yozora.aichat.data.db.PersonaEntity
import com.yozora.aichat.data.remote.GeminiChatReply
import com.yozora.aichat.data.remote.GeminiChatService
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String,
    val content: String,
    val imageUris: List<Uri> = emptyList(),
    val time: String = currentTime()
) {
    val imageUri: Uri?
        get() = imageUris.firstOrNull()
}

enum class ApiVendor(
    val id: String,
    val label: String,
    val defaultModel: String,
    val modelOptions: List<String>
) {
    Google(
        "google",
        "Google",
        "gemini-3.1-flash-lite",
        listOf("gemini-3.5-flash", "gemini-3.1-pro", "gemini-3.1-flash-lite", "gemini-3-flash")
    ),
    GPT(
        "gpt",
        "GPT",
        "gpt-5.5-instant",
        listOf("gpt-5.5", "gpt-5.5-instant", "gpt-5.4-pro", "gpt-5.4-mini", "gpt-5.4")
    ),
    Claude(
        "claude",
        "Claude",
        "claude-4.6-sonnet",
        listOf("claude-4.8-opus", "claude-4.7-opus", "claude-4.6-sonnet", "claude-4.5-haiku")
    ),
    Grok(
        "grok",
        "Grok",
        "grok-4.1-fast",
        listOf("grok-4.3", "grok-build-0.1", "grok-4.20-multi-agent-0309", "grok-4.20-0309-reasoning", "grok-4.1-fast")
    ),
    Mixtral(
        "mixtral",
        "Mixtral",
        "mistral-medium-3.5",
        listOf("mistral-medium-3.5", "mistral-small-4", "mistral-large-3", "ministral-14b", "ministral-8b", "ministral-3b")
    )
}

enum class SafetyLevel(
    val label: String
) {
    None("None"),
    Low("Low"),
    Medium("Medium"),
    High("High")
}

enum class AppIconChoice(
    val id: String,
    val label: String
) {
    Minimalist("minimalist", "Minimalist"),
    Waifu("waifu", "Waifu")
}

enum class AppNameChoice(
    val id: String,
    val label: String
) {
    Zora("zora", "Zora.AI"),
    SanLoVerse("sanloverse", "SanLoVerse (SLV)")
}

data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    val persona: PersonaUiState = PersonaUiState(),
    val background: ChatBackground = ChatBackground.DarkMode,
    val preview: String = "No messages yet",
    val updatedAt: String = "Now",
    val messages: List<ChatMessage> = emptyList()
)

sealed class ChatBackground {
    data object DarkMode : ChatBackground()
    data object LightMode : ChatBackground()
    data object GreyMode : ChatBackground()
    data object PureWhite : ChatBackground()
    data object PureBlack : ChatBackground()
    data object PresetBlack : ChatBackground()
    data object PresetWhite : ChatBackground()
    data class CustomImage(val uri: Uri) : ChatBackground()
}

data class PersonaUiState(
    val displayName: String = "New Persona",
    val tagline: String = "Custom roleplay companion",
    val instructionPrompt: String = "",
    val vendor: ApiVendor = ApiVendor.Google,
    val model: String = "gemini-3.1-flash-lite",
    val safetyLevel: SafetyLevel = SafetyLevel.None,
    val temperature: Float = 1.0f,
    val avatarUri: Uri? = null,
    val avatarScale: Float = 1.0f,
    val avatarOffsetX: Float = 0f,
    val avatarOffsetY: Float = 0f,
    val traits: List<String> = listOf("Empathetic", "Encouraging", "Curious", "Calm")
)

data class QuotaUsageState(
    val day: String = currentDay(),
    val requestsToday: Int = 0,
    val totalTokensToday: Int = 0,
    val lastPromptTokens: Int = 0,
    val lastResponseTokens: Int = 0,
    val lastTotalTokens: Int = 0
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsDataStore = application.settingsDataStore
    private val apiKeyManager = ApiKeyManager(settingsDataStore)
    private val geminiChatService = GeminiChatService()
    private val masterSystemPrompt = loadMasterPrompt(application)
    private val chatStateKey = stringPreferencesKey("chat_state_v1")
    private val appIconChoiceKey = stringPreferencesKey("app_icon_choice")
    private val appNameChoiceKey = stringPreferencesKey("app_name_choice")
    private var restoringState = false
    private val maxAttachedImages = 6

    var draft by mutableStateOf("")
        private set

    var personaSheetVisible by mutableStateOf(false)
        private set

    var sessionDrawerVisible by mutableStateOf(false)
        private set

    var morePersonaOptions by mutableStateOf(false)
        private set

    var apiKeyDialogVisible by mutableStateOf(false)
        private set

    var appSettingsVisible by mutableStateOf(false)
        private set

    var apiKeyDraft by mutableStateOf("")
        private set

    var savedApiKeys by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    var chatError by mutableStateOf<String?>(null)
        private set

    var sendingSessionId by mutableStateOf<String?>(null)
        private set

    var attachedImageUris by mutableStateOf<List<Uri>>(emptyList())
        private set

    var webSearchEnabled by mutableStateOf(false)
        private set

    var appIconChoice by mutableStateOf(AppIconChoice.Minimalist)
        private set

    var appNameChoice by mutableStateOf(AppNameChoice.Zora)
        private set

    var quotaUsage by mutableStateOf(QuotaUsageState())
        private set

    val sessions = mutableStateListOf(
        ChatSession()
    )

    var activeSessionId by mutableStateOf(sessions.first().id)
        private set

    val persona: PersonaUiState
        get() = activeSession.persona

    val messages: List<ChatMessage>
        get() = activeSession.messages

    val background: ChatBackground
        get() = activeSession.background

    val isSending: Boolean
        get() = sendingSessionId == activeSessionId

    val activeApiKeyLabel: String?
        get() = savedApiKeys[persona.vendor.id]?.let(apiKeyManager::mask)

    val dailyRequestLimit: Int?
        get() = when (persona.model) {
            "gemini-3.1-flash-lite" -> 500
            "gemini-3.5-flash" -> 500
            "gemini-3.1-pro" -> 100
            "gemini-3-flash" -> 500
            else -> null
        }

    init {
        viewModelScope.launch {
            restoreChatState()
        }
        viewModelScope.launch {
            restoreLauncherChoice()
        }
        viewModelScope.launch {
            apiKeyManager.providerKeys(ApiVendor.entries.map { it.id }).collectLatest { keys ->
                savedApiKeys = keys
            }
        }
    }

    fun updateDraft(value: String) {
        draft = value
    }

    fun sendDraft() {
        val message = draft.trim()
        val imageUris = attachedImageUris
        if (message.isEmpty() && imageUris.isEmpty()) return
        viewModelScope.launch {
            val provider = persona.vendor
            val apiKey = apiKeyManager.keyForProvider(provider.id)
            if (apiKey == null) {
                apiKeyDialogVisible = true
                chatError = "Add a ${provider.label} API key first."
                return@launch
            }
            if (imageUris.isNotEmpty() && !geminiChatService.supportsImageInput(provider, persona.model)) {
                chatError = "${persona.model} does not support image input. Pick a vision model or remove the image."
                return@launch
            }
            if (webSearchEnabled && provider != ApiVendor.Google) {
                chatError = "Web search uses Gemini grounding. Switch API vendor to Google or turn it off."
                return@launch
            }

            val sessionId = activeSessionId
            val session = activeSession
            val content = message.ifBlank { "Please respond to this image." }
            val loadedImages = imageUris.mapNotNull { loadBitmap(it) }
            if (imageUris.isNotEmpty() && loadedImages.isEmpty()) {
                chatError = "Could not read the selected images. Pick them again."
                return@launch
            }
            val userMessage = ChatMessage(role = "user", content = content, imageUris = imageUris)
            val history = session.messages
            draft = ""
            attachedImageUris = emptyList()
            chatError = null
            sendingSessionId = sessionId
            appendMessage(
                sessionId = sessionId,
                message = userMessage,
                preview = if (imageUris.isEmpty()) content.take(72) else "[${imageUris.size} image] ${content.take(58)}"
            )

            val result = geminiChatService.sendMessage(
                apiKey = apiKey,
                persona = session.persona.toEntity(sessionId),
                history = history.toEntities(sessionId),
                userInput = content,
                vendor = provider,
                safetyLevel = session.persona.safetyLevel,
                images = loadedImages,
                webSearchEnabled = webSearchEnabled,
                masterPrompt = masterSystemPrompt
            )

            sendingSessionId = null
            result
                .onSuccess { reply ->
                    recordUsage(reply)
                    val cleanResponse = reply.text.ifBlank { "(empty response)" }
                    appendMessage(
                        sessionId = sessionId,
                        message = ChatMessage(role = "model", content = cleanResponse),
                        preview = cleanResponse.take(72)
                    )
                }
                .onFailure { throwable ->
                    chatError = throwable.message?.take(160) ?: "Gemini request failed."
                }
        }
    }

    fun beginEditMessage(messageId: String) {
        val session = activeSession
        val index = session.messages.indexOfFirst { it.id == messageId && it.role == "user" }
        if (index < 0) return

        val message = session.messages[index]
        val keptMessages = session.messages.take(index)
        draft = message.content
        attachedImageUris = message.imageUris
        updateActiveSession { current ->
            current.copy(
                messages = keptMessages,
                preview = previewFor(keptMessages),
                updatedAt = currentTime()
            )
        }
    }

    fun retryMessage(messageId: String) {
        if (sendingSessionId != null) return
        val session = activeSession
        val targetIndex = session.messages.indexOfFirst { it.id == messageId }
        if (targetIndex < 0) return

        val userIndex = (targetIndex downTo 0).firstOrNull { session.messages[it].role == "user" } ?: return
        val message = session.messages[userIndex]
        val keptMessages = session.messages.take(userIndex)
        updateActiveSession { current ->
            current.copy(
                messages = keptMessages,
                preview = previewFor(keptMessages),
                updatedAt = currentTime()
            )
        }
        draft = message.content
        attachedImageUris = message.imageUris
        sendDraft()
    }

    fun openPersonaSheet() {
        personaSheetVisible = true
    }

    fun closePersonaSheet() {
        personaSheetVisible = false
    }

    fun openSessionDrawer() {
        sessionDrawerVisible = true
    }

    fun closeSessionDrawer() {
        sessionDrawerVisible = false
    }

    fun selectSession(id: String) {
        activeSessionId = id
        draft = ""
        attachedImageUris = emptyList()
        sessionDrawerVisible = false
        persistChatState()
    }

    fun createSession() {
        val personaName = if (sessions.isEmpty()) "New Persona" else "New Persona ${sessions.size + 1}"
        val session = ChatSession(
            persona = PersonaUiState(displayName = personaName)
        )
        sessions.add(0, session)
        activeSessionId = session.id
        draft = ""
        attachedImageUris = emptyList()
        sessionDrawerVisible = false
        persistChatState()
    }

    fun deleteSession(id: String) {
        val index = sessions.indexOfFirst { it.id == id }
        if (index < 0) return

        val deletingActiveSession = activeSessionId == id
        if (sessions.size == 1) {
            val freshSession = ChatSession()
            sessions[0] = freshSession
            activeSessionId = freshSession.id
        } else {
            sessions.removeAt(index)
            if (deletingActiveSession) {
                activeSessionId = sessions.getOrNull(index.coerceAtMost(sessions.lastIndex))?.id
                    ?: sessions.first().id
            }
        }

        if (deletingActiveSession) {
            draft = ""
            attachedImageUris = emptyList()
            personaSheetVisible = false
        }
        persistChatState()
    }

    fun deleteActiveSession() {
        deleteSession(activeSessionId)
    }

    fun toggleMorePersonaOptions() {
        morePersonaOptions = !morePersonaOptions
    }

    fun openApiKeyDialog() {
        apiKeyDraft = ""
        apiKeyDialogVisible = true
    }

    fun closeApiKeyDialog() {
        apiKeyDialogVisible = false
        apiKeyDraft = ""
    }

    fun updateApiKeyDraft(value: String) {
        apiKeyDraft = value.trim()
    }

    fun saveApiKey() {
        viewModelScope.launch {
            apiKeyManager.replaceProviderKey(persona.vendor.id, apiKeyDraft)
            apiKeyDialogVisible = false
            apiKeyDraft = ""
            chatError = null
        }
    }

    fun clearApiKey() {
        viewModelScope.launch {
            apiKeyManager.clearProviderKey(persona.vendor.id)
            chatError = null
        }
    }

    fun updateVendor(vendor: ApiVendor) {
        updatePersona {
            it.copy(
                vendor = vendor,
                model = vendor.defaultModel
            )
        }
        if (vendor != ApiVendor.Google) {
            webSearchEnabled = false
        }
    }

    fun updatePersonaName(value: String) {
        updatePersona { it.copy(displayName = value.take(32)) }
    }

    fun updatePersonaPrompt(value: String) {
        updatePersona { it.copy(instructionPrompt = value) }
    }

    fun updatePersonaModel(value: String) {
        updatePersona { it.copy(model = value) }
    }

    fun updateSafetyLevel(value: SafetyLevel) {
        updatePersona { it.copy(safetyLevel = value) }
    }

    fun updateTemperature(value: Float) {
        updatePersona { it.copy(temperature = value.coerceIn(0f, 2f)) }
    }

    fun updateAvatar(uri: Uri?) {
        persistImagePermission(uri)
        updatePersona {
            it.copy(
                avatarUri = uri,
                avatarScale = 1f,
                avatarOffsetX = 0f,
                avatarOffsetY = 0f
            )
        }
    }

    fun transformAvatar(zoomChange: Float, panX: Float, panY: Float) {
        updatePersona {
            val nextScale = (it.avatarScale * zoomChange).coerceIn(1f, 4f)
            it.copy(
                avatarScale = nextScale,
                avatarOffsetX = (it.avatarOffsetX + panX).coerceIn(-180f, 180f),
                avatarOffsetY = (it.avatarOffsetY + panY).coerceIn(-180f, 180f)
            )
        }
    }

    fun updateBackground(background: ChatBackground) {
        updateActiveSession { session ->
            session.copy(background = background)
        }
    }

    fun updateCustomBackground(uri: Uri?) {
        if (uri != null) {
            persistImagePermission(uri)
            updateBackground(ChatBackground.CustomImage(uri))
        }
    }

    fun attachImages(uris: List<Uri>) {
        val remainingSlots = (maxAttachedImages - attachedImageUris.size).coerceAtLeast(0)
        if (remainingSlots == 0) return
        viewModelScope.launch {
            val localUris = withContext(Dispatchers.IO) {
                uris.take(remainingSlots).mapNotNull { uri ->
                    persistImagePermission(uri)
                    copyAttachmentIntoAppStorage(uri)
                }
            }
            attachedImageUris = (attachedImageUris + localUris)
                .distinctBy { it.toString() }
                .take(maxAttachedImages)
        }
    }

    fun removeAttachedImage(uri: Uri) {
        attachedImageUris = attachedImageUris.filterNot { it == uri }
    }

    fun updateWebSearchEnabled(value: Boolean) {
        webSearchEnabled = value && persona.vendor == ApiVendor.Google
    }

    fun openAppSettings() {
        appSettingsVisible = true
    }

    fun closeAppSettings() {
        appSettingsVisible = false
    }

    fun updateAppIcon(choice: AppIconChoice) {
        appIconChoice = choice
        applyLauncherChoice(appNameChoice, appIconChoice)
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.edit { preferences ->
                preferences[appIconChoiceKey] = choice.id
            }
        }
    }

    fun updateAppName(choice: AppNameChoice) {
        appNameChoice = choice
        applyLauncherChoice(appNameChoice, appIconChoice)
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.edit { preferences ->
                preferences[appNameChoiceKey] = choice.id
            }
        }
    }

    fun savePersona() {
        personaSheetVisible = false
        persistChatState()
    }

    private val activeSession: ChatSession
        get() = sessions.firstOrNull { it.id == activeSessionId } ?: sessions.first()

    private fun updatePersona(transform: (PersonaUiState) -> PersonaUiState) {
        updateActiveSession { session ->
            session.copy(persona = transform(session.persona))
        }
    }

    private fun updateActiveSession(transform: (ChatSession) -> ChatSession) {
        val index = sessions.indexOfFirst { it.id == activeSessionId }
        if (index >= 0) {
            sessions[index] = transform(sessions[index])
            persistChatState()
        }
    }

    private fun appendMessage(sessionId: String, message: ChatMessage, preview: String) {
        val index = sessions.indexOfFirst { it.id == sessionId }
        if (index >= 0) {
            val session = sessions[index]
            sessions[index] = session.copy(
                messages = session.messages + message,
                preview = preview,
                updatedAt = currentTime()
            )
            persistChatState()
        }
    }

    private suspend fun restoreChatState() {
        val rawState = settingsDataStore.data.first()[chatStateKey] ?: return
        val restoredState = decodeChatState(rawState) ?: return
        restoringState = true
        try {
            sessions.clear()
            sessions.addAll(restoredState.sessions.ifEmpty { listOf(ChatSession()) })
            activeSessionId = restoredState.activeSessionId
                .takeIf { id -> sessions.any { it.id == id } }
                ?: sessions.first().id
        } finally {
            restoringState = false
        }
    }

    private suspend fun restoreLauncherChoice() {
        val preferences = settingsDataStore.data.first()
        val restoredIcon = AppIconChoice.entries.firstOrNull { it.id == preferences[appIconChoiceKey] } ?: AppIconChoice.Minimalist
        val restoredName = AppNameChoice.entries.firstOrNull { it.id == preferences[appNameChoiceKey] } ?: AppNameChoice.Zora
        appIconChoice = restoredIcon
        appNameChoice = restoredName
        applyLauncherChoice(restoredName, restoredIcon)
    }

    private fun applyLauncherChoice(name: AppNameChoice, icon: AppIconChoice) {
        val app = getApplication<Application>()
        val packageManager = app.packageManager
        AppNameChoice.entries.forEach { nameOption ->
            AppIconChoice.entries.forEach { iconOption ->
                val state = if (nameOption == name && iconOption == icon) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }
                packageManager.setComponentEnabledSetting(
                    ComponentName(app, "${app.packageName}.${launcherAliasName(nameOption, iconOption)}"),
                    state,
                    PackageManager.DONT_KILL_APP
                )
            }
        }
    }

    private fun launcherAliasName(name: AppNameChoice, icon: AppIconChoice): String {
        return when (name) {
            AppNameChoice.Zora -> when (icon) {
                AppIconChoice.Minimalist -> "MainActivityZoraMinimalistAlias"
                AppIconChoice.Waifu -> "MainActivityZoraWaifuAlias"
            }
            AppNameChoice.SanLoVerse -> when (icon) {
                AppIconChoice.Minimalist -> "MainActivitySlvMinimalistAlias"
                AppIconChoice.Waifu -> "MainActivitySlvWaifuAlias"
            }
        }
    }

    private fun persistChatState() {
        if (restoringState) return
        val activeId = activeSessionId
        val sessionSnapshot = sessions.toList()
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.edit { preferences ->
                preferences[chatStateKey] = encodeChatState(activeId, sessionSnapshot)
            }
        }
    }

    private fun previewFor(messages: List<ChatMessage>): String {
        val message = messages.lastOrNull() ?: return "No messages yet"
        return if (message.imageUris.isEmpty()) {
            message.content.take(72)
        } else {
            "[${message.imageUris.size} image] ${message.content.take(58)}".trim()
        }
    }

    private fun recordUsage(reply: GeminiChatReply) {
        val today = currentDay()
        val current = if (quotaUsage.day == today) quotaUsage else QuotaUsageState(day = today)
        quotaUsage = current.copy(
            requestsToday = current.requestsToday + 1,
            totalTokensToday = current.totalTokensToday + reply.totalTokenCount,
            lastPromptTokens = reply.promptTokenCount,
            lastResponseTokens = reply.responseTokenCount,
            lastTotalTokens = reply.totalTokenCount
        )
    }

    private suspend fun loadBitmap(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            if (uri.scheme == "file") {
                BitmapFactory.decodeFile(uri.path)
            } else {
                getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input)
                }
            }
        }.getOrNull()
    }

    private fun copyAttachmentIntoAppStorage(uri: Uri): Uri? {
        return runCatching {
            val app = getApplication<Application>()
            val directory = File(app.filesDir, "message_images").apply { mkdirs() }
            val file = File(directory, "message_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg")
            app.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@runCatching null
            Uri.fromFile(file)
        }.getOrNull()
    }

    private fun persistImagePermission(uri: Uri?) {
        if (uri == null) return
        runCatching {
            getApplication<Application>().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }
}

private data class RestoredChatState(
    val activeSessionId: String,
    val sessions: List<ChatSession>
)

private fun encodeChatState(activeSessionId: String, sessions: List<ChatSession>): String {
    return JSONObject()
        .put("activeSessionId", activeSessionId)
        .put(
            "sessions",
            JSONArray().apply {
                sessions.forEach { session -> put(session.toJson()) }
            }
        )
        .toString()
}

private fun decodeChatState(rawState: String): RestoredChatState? {
    return runCatching {
        val root = JSONObject(rawState)
        val sessionsJson = root.optJSONArray("sessions") ?: JSONArray()
        val restoredSessions = buildList {
            for (index in 0 until sessionsJson.length()) {
                add(sessionsJson.getJSONObject(index).toChatSession())
            }
        }
        RestoredChatState(
            activeSessionId = root.optString("activeSessionId"),
            sessions = restoredSessions
        )
    }.getOrNull()
}

private fun ChatSession.toJson(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("persona", persona.toJson())
        .put("background", background.toJson())
        .put("preview", preview)
        .put("updatedAt", updatedAt)
        .put(
            "messages",
            JSONArray().apply {
                messages.forEach { message -> put(message.toJson()) }
            }
        )
}

private fun JSONObject.toChatSession(): ChatSession {
    val messagesJson = optJSONArray("messages") ?: JSONArray()
    val restoredMessages = buildList {
        for (index in 0 until messagesJson.length()) {
            add(messagesJson.getJSONObject(index).toChatMessage())
        }
    }
    return ChatSession(
        id = optString("id").ifBlank { UUID.randomUUID().toString() },
        persona = optJSONObject("persona")?.toPersonaUiState() ?: PersonaUiState(),
        background = optJSONObject("background")?.toChatBackground() ?: ChatBackground.DarkMode,
        preview = optString("preview").ifBlank { previewForRestored(restoredMessages) },
        updatedAt = optString("updatedAt").ifBlank { "Now" },
        messages = restoredMessages
    )
}

private fun PersonaUiState.toJson(): JSONObject {
    return JSONObject()
        .put("displayName", displayName)
        .put("tagline", tagline)
        .put("instructionPrompt", instructionPrompt)
        .put("vendor", vendor.id)
        .put("model", model)
        .put("safetyLevel", safetyLevel.name)
        .put("temperature", temperature.toDouble())
        .put("avatarUri", avatarUri?.toString() ?: JSONObject.NULL)
        .put("avatarScale", avatarScale.toDouble())
        .put("avatarOffsetX", avatarOffsetX.toDouble())
        .put("avatarOffsetY", avatarOffsetY.toDouble())
        .put(
            "traits",
            JSONArray().apply {
                traits.forEach { trait -> put(trait) }
            }
        )
}

private fun JSONObject.toPersonaUiState(): PersonaUiState {
    val vendor = ApiVendor.entries.firstOrNull { it.id == optString("vendor") } ?: ApiVendor.Google
    val traitsJson = optJSONArray("traits")
    val restoredTraits = buildList {
        if (traitsJson != null) {
            for (index in 0 until traitsJson.length()) {
                add(traitsJson.optString(index))
            }
        }
    }.filter { it.isNotBlank() }

    return PersonaUiState(
        displayName = optString("displayName").ifBlank { "New Persona" },
        tagline = optString("tagline").ifBlank { "Custom roleplay companion" },
        instructionPrompt = optString("instructionPrompt"),
        vendor = vendor,
        model = optString("model").ifBlank { vendor.defaultModel },
        safetyLevel = SafetyLevel.entries.firstOrNull { it.name == optString("safetyLevel") } ?: SafetyLevel.None,
        temperature = optDouble("temperature", 1.0).toFloat().coerceIn(0f, 2f),
        avatarUri = optNullableString("avatarUri")?.let(Uri::parse),
        avatarScale = optDouble("avatarScale", 1.0).toFloat().coerceIn(1f, 4f),
        avatarOffsetX = optDouble("avatarOffsetX", 0.0).toFloat().coerceIn(-180f, 180f),
        avatarOffsetY = optDouble("avatarOffsetY", 0.0).toFloat().coerceIn(-180f, 180f),
        traits = restoredTraits.ifEmpty { listOf("Empathetic", "Encouraging", "Curious", "Calm") }
    )
}

private fun ChatBackground.toJson(): JSONObject {
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
    }
}

private fun JSONObject.toChatBackground(): ChatBackground {
    return when (optString("type")) {
        "light" -> ChatBackground.LightMode
        "grey" -> ChatBackground.GreyMode
        "white" -> ChatBackground.PureWhite
        "black" -> ChatBackground.PureBlack
        "preset_black" -> ChatBackground.PresetBlack
        "preset_white" -> ChatBackground.PresetWhite
        "custom" -> optNullableString("uri")?.let { ChatBackground.CustomImage(Uri.parse(it)) } ?: ChatBackground.DarkMode
        else -> ChatBackground.DarkMode
    }
}

private fun ChatMessage.toJson(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("role", role)
        .put("content", content)
        .put(
            "imageUris",
            JSONArray().apply {
                imageUris.forEach { uri -> put(uri.toString()) }
            }
        )
        .put("time", time)
}

private fun JSONObject.toChatMessage(): ChatMessage {
    val imageUriArray = optJSONArray("imageUris")
    val restoredImageUris = buildList {
        if (imageUriArray != null) {
            for (index in 0 until imageUriArray.length()) {
                imageUriArray.optString(index).takeIf { it.isNotBlank() }?.let { add(Uri.parse(it)) }
            }
        } else {
            optNullableString("imageUri")?.let { add(Uri.parse(it)) }
        }
    }
    return ChatMessage(
        id = optString("id").ifBlank { UUID.randomUUID().toString() },
        role = optString("role").ifBlank { "user" },
        content = optString("content"),
        imageUris = restoredImageUris.take(6),
        time = optString("time").ifBlank { currentTime() }
    )
}

private fun JSONObject.optNullableString(name: String): String? {
    if (!has(name) || isNull(name)) return null
    return optString(name).takeIf { it.isNotBlank() }
}

private fun previewForRestored(messages: List<ChatMessage>): String {
    val message = messages.lastOrNull() ?: return "No messages yet"
    return if (message.imageUris.isEmpty()) {
        message.content.take(72)
    } else {
        "[${message.imageUris.size} image] ${message.content.take(58)}".trim()
    }
}

private fun currentTime(): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
}

private fun currentDay(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("America/Los_Angeles")
    }.format(Date())
}

private fun loadMasterPrompt(application: Application): String {
    return runCatching {
        application.assets.open("local_master_prompt.txt").bufferedReader().use { reader ->
            reader.readText()
        }
    }.getOrNull()?.takeIf { it.isNotBlank() }.orEmpty()
}

private fun PersonaUiState.toEntity(id: String): PersonaEntity {
    return PersonaEntity(
        id = id,
        name = displayName,
        avatarUri = avatarUri?.toString(),
        systemPrompt = instructionPrompt,
        model = model,
        temperature = temperature
    )
}

private fun List<ChatMessage>.toEntities(chatId: String): List<MessageEntity> {
    return map { message ->
        MessageEntity(
            id = message.id,
            chatId = chatId,
            role = message.role,
            content = message.content,
            timestamp = System.currentTimeMillis()
        )
    }
}
