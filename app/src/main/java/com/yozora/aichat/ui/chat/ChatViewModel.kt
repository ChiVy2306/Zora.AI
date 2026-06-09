package com.yozora.aichat.ui.chat

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yozora.aichat.data.SkillRepository
import com.yozora.aichat.data.datastore.ApiKeyManager
import com.yozora.aichat.data.datastore.settingsDataStore
import com.yozora.aichat.data.db.ChatRepository
import com.yozora.aichat.data.db.MessageEntity
import com.yozora.aichat.data.db.PersonaEntity
import com.yozora.aichat.data.db.TtsAudioCacheEntity
import com.yozora.aichat.data.remote.ElevenLabsTtsException
import com.yozora.aichat.data.remote.ElevenLabsTtsRepository
import com.yozora.aichat.data.remote.GeminiChatReply
import com.yozora.aichat.data.remote.GeminiChatService
import com.yozora.aichat.data.remote.GeminiLiveCallConfig
import com.yozora.aichat.data.remote.GeminiLiveCallService
import com.yozora.aichat.data.remote.GeminiToolCall
import com.yozora.aichat.data.remote.Rule34ImageRepository
import com.yozora.aichat.data.remote.ScreenShareForegroundService
import com.yozora.aichat.data.remote.TavilyRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String,
    val content: String,
    val speakerId: String? = null,
    val speakerName: String? = null,
    val imageUris: List<Uri> = emptyList(),
    val remoteImageUrl: String? = null,
    val isImageLoading: Boolean = false,
    val time: String = currentTime()
) {
    val imageUri: Uri?
        get() = imageUris.firstOrNull()
}

data class LiveCallTranscriptLine(
    val id: String = UUID.randomUUID().toString(),
    val role: String,
    val text: String,
    val streaming: Boolean = false
)

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

enum class GeminiThinkingEffort(
    val label: String,
    val thinkingBudget: Int
) {
    Minimal("Minimal", 256),
    Low("Low", 1024),
    Medium("Medium", 4096),
    High("High", 8192)
}

enum class InstructionMode(
    val label: String
) {
    Beginner("Beginner"),
    Advanced("Advanced")
}

enum class AnimeImagePreset(
    val id: String,
    val label: String,
    val promptHint: String
) {
    Auto(
        "auto",
        "Auto",
        "Use the user's request directly and choose the most relevant Danbooru tags."
    ),
    Portrait(
        "portrait",
        "Portrait",
        "Prefer solo portrait-style tags such as 1girl, solo, portrait, looking_at_viewer when they fit."
    ),
    Character(
        "character",
        "Character",
        "Prioritize named character, series, outfit, and visual-detail tags when the user mentions them."
    ),
    Explicit(
        "explicit",
        "Explicit",
        "Prefer direct adult-scene tags when the user request is sexual."
    )
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

enum class GeminiLiveVoice(
    val id: String,
    val label: String,
    val description: String
) {
    Aoede("aoede", "Aoede", "Clear, melodic, and engaging."),
    Kore("kore", "Kore", "Calm, professional, and slightly firm."),
    Leda("leda", "Leda", "Warm and conversational."),
    Glow("glow", "Glow", "Smooth and natural."),
    Puck("puck", "Puck", "Upbeat, energetic, and conversational."),
    Charon("charon", "Charon", "Calm, reassuring, and professional."),
    Fenrir("fenrir", "Fenrir", "Deep, slightly raspy, and conversational."),
    Zephyr("zephyr", "Zephyr", "Smooth, clear, and relaxed."),
    Flare("flare", "Flare", "Dynamic and engaging.")
}

enum class VoiceCallCameraFacing {
    Front,
    Back
}

data class GroupMember(
    val id: String = UUID.randomUUID().toString(),
    val persona: PersonaUiState = PersonaUiState()
)

data class ProjectUiState(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "New project",
    val description: String = "",
    val instruction: String = "",
    val updatedAt: String = "Now"
)

data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val headerAvatarUri: Uri? = null,
    val headerAvatarScale: Float = 1.0f,
    val headerAvatarOffsetX: Float = 0f,
    val headerAvatarOffsetY: Float = 0f,
    val persona: PersonaUiState = PersonaUiState(),
    val members: List<GroupMember> = listOf(GroupMember(persona = persona)),
    val activeMemberId: String = members.firstOrNull()?.id ?: "",
    val responseRounds: Int = 1,
    val memoryEnabled: Boolean = true,
    val storyLore: String = "",
    val levelSystemEnabled: Boolean = false,
    val levelXp: Int = 0,
    val projectId: String? = null,
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
    val instructionMode: InstructionMode = InstructionMode.Beginner,
    val beginnerRole: String = "",
    val beginnerStyle: String = "",
    val beginnerLimits: String = "",
    val instructionPrompt: String = "",
    val vendor: ApiVendor = ApiVendor.Google,
    val model: String = "gemini-3.1-flash-lite",
    val safetyLevel: SafetyLevel = SafetyLevel.None,
    val thinkingEffort: GeminiThinkingEffort = GeminiThinkingEffort.Low,
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

data class SessionLevelState(
    val level: Int,
    val xp: Int,
    val currentLevelXp: Int,
    val nextLevelXp: Int,
    val label: String
) {
    val progress: Float
        get() = if (level >= 10 || nextLevelXp <= currentLevelXp) {
            1f
        } else {
            ((xp - currentLevelXp).toFloat() / (nextLevelXp - currentLevelXp))
                .coerceIn(0f, 1f)
        }
}

data class TtsPreviewState(
    val messageId: String,
    val text: String,
    val isPreparing: Boolean = false,
    val isGenerating: Boolean = false
) {
    val characterCount: Int
        get() = text.length
}

private data class PendingTtsRequest(
    val messageId: String,
    val sessionId: String,
    val sourceText: String,
    val sourceHash: String,
    val cleanedText: String,
    val cleanedTextHash: String,
    val voiceId: String,
    val modelId: String
)

private enum class ApiKeyDialogTarget {
    Provider,
    Tavily,
    Rule34UserId,
    Rule34ApiKey,
    ElevenLabsKey,
    ElevenLabsVoiceId,
    ElevenLabsModelId
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsDataStore = application.settingsDataStore
    private val apiKeyManager = ApiKeyManager(settingsDataStore)
    private val skillRepository = SkillRepository.get(application)
    private val geminiChatService = GeminiChatService(skillRepository)
    private val geminiLiveCallService = GeminiLiveCallService()
    private val tavilyRepository = TavilyRepository()
    private val rule34ImageRepository = Rule34ImageRepository()
    private val elevenLabsTtsRepository = ElevenLabsTtsRepository()
    private val chatRepository = ChatRepository.get(application)
    private val masterSystemPrompt = loadMasterPrompt(application)
    private val chatStateKey = stringPreferencesKey("chat_state_v1")
    private val activeSessionIdKey = stringPreferencesKey("active_session_id_v1")
    private val projectStateKey = stringPreferencesKey("project_state_v1")
    private val appIconChoiceKey = stringPreferencesKey("app_icon_choice")
    private val appNameChoiceKey = stringPreferencesKey("app_name_choice")
    private val geminiLiveVoiceKey = stringPreferencesKey("gemini_live_voice")
    private val globalMemoryKey = stringPreferencesKey("global_memory_block_v1")
    private var restoringState = false
    private val persistMutex = Mutex()
    private val maxAttachedImages = 12
    private var pendingTtsRequest: PendingTtsRequest? = null
    private var mediaPlayer: MediaPlayer? = null
    private var voiceCallSessionId: String? = null
    private var voiceCallModelMessageId: String? = null
    private var currentVoiceCallModelLineId: String? = null
    private var currentVoiceCallModelText = ""
    private var voiceCallVisualReconnectPending = false
    private var lastVoiceCallVisualReconnectAt = 0L

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

    private var apiKeyDialogTarget = ApiKeyDialogTarget.Provider

    var savedApiKeys by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    var tavilyApiKeyLabel by mutableStateOf<String?>(null)
        private set

    var rule34ApiKeyLabel by mutableStateOf<String?>(null)
        private set

    var rule34UserIdLabel by mutableStateOf<String?>(null)
        private set

    var elevenLabsApiKeyLabel by mutableStateOf<String?>(null)
        private set

    var elevenLabsVoiceIdLabel by mutableStateOf<String?>(null)
        private set

    var elevenLabsModelIdLabel by mutableStateOf(ApiKeyManager.DEFAULT_ELEVENLABS_MODEL_ID)
        private set

    var ttsPreviewState by mutableStateOf<TtsPreviewState?>(null)
        private set

    var ttsPlaybackMessageId by mutableStateOf<String?>(null)
        private set

    var chatError by mutableStateOf<String?>(null)
        private set

    var rateLimitDialogVisible by mutableStateOf(false)
        private set

    private var rateLimitRetryMessageId: String? = null

    var sendingSessionId by mutableStateOf<String?>(null)
        private set

    var attachedImageUris by mutableStateOf<List<Uri>>(emptyList())
        private set

    var webSearchEnabled by mutableStateOf(false)
        private set

    var animeImageModeEnabled by mutableStateOf(false)
        private set

    var animeImagePreset by mutableStateOf(AnimeImagePreset.Auto)
        private set

    var appIconChoice by mutableStateOf(AppIconChoice.Minimalist)
        private set

    var appNameChoice by mutableStateOf(AppNameChoice.Zora)
        private set

    var voiceCallActive by mutableStateOf(false)
        private set

    var voiceCallStatus by mutableStateOf("Idle")
        private set

    var voiceCallError by mutableStateOf<String?>(null)
        private set

    val geminiLiveModelId: String = GeminiLiveCallService.DEFAULT_MODEL_ID

    var selectedGeminiLiveVoice by mutableStateOf(GeminiLiveVoice.Aoede)
        private set

    var voiceCallMuted by mutableStateOf(false)
        private set

    var voiceCallVideoEnabled by mutableStateOf(false)
        private set

    var voiceCallCameraFacing by mutableStateOf(VoiceCallCameraFacing.Front)
        private set

    var voiceCallScreenShareEnabled by mutableStateOf(false)
        private set

    val voiceCallTranscriptLines = mutableStateListOf<LiveCallTranscriptLine>()

    var globalMemoryBlock by mutableStateOf("")
        private set

    var quotaUsage by mutableStateOf(QuotaUsageState())
        private set

    val sessions = mutableStateListOf(
        ChatSession()
    )

    val projects = mutableStateListOf<ProjectUiState>()

    var activeSessionId by mutableStateOf(sessions.first().id)
        private set

    val activeProject: ProjectUiState?
        get() = activeSession.projectId?.let { id -> projects.firstOrNull { it.id == id } }

    val persona: PersonaUiState
        get() = activeGroupMember.persona

    val groupMembers: List<GroupMember>
        get() = activeSession.normalizedMembers()

    val sessionHeaderName: String
        get() = activeSession.title

    val sessionHeaderAvatarUri: Uri?
        get() = activeSession.headerAvatarUri

    val sessionHeaderAvatarScale: Float
        get() = activeSession.headerAvatarScale

    val sessionHeaderAvatarOffsetX: Float
        get() = activeSession.headerAvatarOffsetX

    val sessionHeaderAvatarOffsetY: Float
        get() = activeSession.headerAvatarOffsetY

    val showSessionHeaderControls: Boolean
        get() = activeSession.normalizedMembers().size > 1

    val activeMemberId: String
        get() = activeGroupMember.id

    val responseRounds: Int
        get() = activeSession.responseRounds.coerceIn(1, 3)

    val memoryEnabled: Boolean
        get() = activeSession.memoryEnabled

    val storyLore: String
        get() = activeSession.storyLore

    val levelSystemEnabled: Boolean
        get() = activeSession.levelSystemEnabled

    val levelXp: Int
        get() = activeSession.levelXp.coerceIn(0, MAX_LEVEL_XP)

    val messages: List<ChatMessage>
        get() = activeSession.messages

    val background: ChatBackground
        get() = activeSession.background

    val isSending: Boolean
        get() = sendingSessionId == activeSessionId

    val activeApiKeyLabel: String?
        get() = savedApiKeys[persona.vendor.id]?.let(apiKeyManager::mask)

    val apiKeyDialogTitle: String
        get() = when (apiKeyDialogTarget) {
            ApiKeyDialogTarget.Provider -> "${persona.vendor.label} API key"
            ApiKeyDialogTarget.Tavily -> "Tavily API key"
            ApiKeyDialogTarget.Rule34UserId -> "Rule34 User ID"
            ApiKeyDialogTarget.Rule34ApiKey -> "Rule34 API Key"
            ApiKeyDialogTarget.ElevenLabsKey -> "ElevenLabs API key"
            ApiKeyDialogTarget.ElevenLabsVoiceId -> "ElevenLabs voice ID"
            ApiKeyDialogTarget.ElevenLabsModelId -> "ElevenLabs model ID"
        }

    val apiKeyDialogSecure: Boolean
        get() = when (apiKeyDialogTarget) {
            ApiKeyDialogTarget.ElevenLabsVoiceId,
            ApiKeyDialogTarget.ElevenLabsModelId -> false

            else -> true
        }

    val canUseWebSearch: Boolean
        get() = tavilyApiKeyLabel != null

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
            restoreProjects()
            restoreChatState()
        }
        viewModelScope.launch {
            restoreLauncherChoice()
        }
        viewModelScope.launch {
            restoreGeminiLiveVoice()
        }
        viewModelScope.launch {
            settingsDataStore.data.collectLatest { preferences ->
                globalMemoryBlock = preferences[globalMemoryKey].orEmpty()
            }
        }
        viewModelScope.launch {
            apiKeyManager.providerKeys(ApiVendor.entries.map { it.id }).collectLatest { keys ->
                savedApiKeys = keys
            }
        }
        viewModelScope.launch {
            apiKeyManager.tavilyKey.collectLatest { key ->
                tavilyApiKeyLabel = key?.let(apiKeyManager::mask)
                if (key.isNullOrBlank()) {
                    webSearchEnabled = false
                }
            }
        }
        viewModelScope.launch {
            apiKeyManager.rule34ApiKey.collectLatest { key ->
                rule34ApiKeyLabel = key?.let(apiKeyManager::mask)
            }
        }
        viewModelScope.launch {
            apiKeyManager.rule34UserId.collectLatest { userId ->
                rule34UserIdLabel = userId?.let(apiKeyManager::mask)
            }
        }
        viewModelScope.launch {
            apiKeyManager.elevenLabsKey.collectLatest { key ->
                elevenLabsApiKeyLabel = key?.let(apiKeyManager::mask)
            }
        }
        viewModelScope.launch {
            apiKeyManager.elevenLabsVoice.collectLatest { voiceId ->
                elevenLabsVoiceIdLabel = voiceId?.let(::compactLabel)
            }
        }
        viewModelScope.launch {
            apiKeyManager.elevenLabsModel.collectLatest { modelId ->
                elevenLabsModelIdLabel = modelId?.takeIf { it.isNotBlank() }
                    ?: ApiKeyManager.DEFAULT_ELEVENLABS_MODEL_ID
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
        if (activeSession.normalizedMembers().size > 1) {
            sendGroupDraft(message, imageUris)
            return
        }
        if (
            animeImageModeEnabled &&
            imageUris.isEmpty() &&
            persona.vendor != ApiVendor.Google &&
            shouldOfferAnimeImageSearch(message.lowercase(Locale.US))
        ) {
            requestAnimeImageFromDraft(message)
            return
        }
        viewModelScope.launch {
            val provider = persona.vendor
            val sessionId = activeSessionId
            val session = activeSession
            val content = message.ifBlank { "Please respond to this image." }
            if (imageUris.isEmpty()) {
                val localReply = localAppAwarenessReply(content)
                if (localReply != null) {
                    draft = ""
                    attachedImageUris = emptyList()
                    chatError = null
                    appendMessage(
                        sessionId = sessionId,
                        message = ChatMessage(role = "user", content = content),
                        preview = content.take(72)
                    )
                    awardTextMessageXp(sessionId, message)
                    appendMessage(
                        sessionId = sessionId,
                        message = ChatMessage(role = "model", content = localReply),
                        preview = localReply.take(72)
                    )
                    return@launch
                }
            }
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
            if (webSearchEnabled && !canUseWebSearch) {
                chatError = "Add a Tavily key and use Google as the API vendor to turn on web search."
                return@launch
            }

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
            awardTextMessageXp(sessionId, message)

            val enabledTools = enabledGeminiTools(
                provider = provider,
                hasImages = imageUris.isNotEmpty(),
                message = content
            )
            if (enabledTools.isNotEmpty()) {
                val plan = retryTemporaryUnavailable {
                    geminiChatService.planToolUse(
                        apiKey = apiKey,
                        vendor = provider,
                        persona = personaEntityForSession(session, sessionId),
                        history = history.toEntities(sessionId),
                        userInput = content,
                        safetyLevel = session.persona.safetyLevel,
                        images = loadedImages,
                        enabledTools = enabledTools,
                        masterPrompt = masterSystemPrompt
                    )
                }.getOrElse { throwable ->
                    if (provider != ApiVendor.Google && enabledTools.isNotEmpty()) {
                        runProviderToolFallback(
                            enabledTools = enabledTools,
                            sessionId = sessionId,
                            session = session,
                            userMessage = userMessage,
                            content = content,
                            apiKey = apiKey,
                            loadedImages = loadedImages
                        )
                        return@launch
                    }
                    sendingSessionId = null
                    handleSendFailure(throwable, userMessage.id)
                    return@launch
                }

                recordUsage(plan.toReply())
                val toolCall = plan.functionCall
                if (toolCall == null) {
                    val parsedToolCall = parseTextToolCall(
                        raw = plan.text,
                        enabledTools = enabledTools,
                        currentMessage = content
                    )
                    if (parsedToolCall != null && executeToolCall(
                            toolCall = parsedToolCall,
                            sessionId = sessionId,
                            session = session,
                            history = history,
                            content = content,
                            apiKey = apiKey,
                            loadedImages = loadedImages,
                            userMessage = userMessage
                        )
                    ) {
                        sendingSessionId = null
                        return@launch
                    }
                    appendModelResponseOrRecover(
                        sessionId = sessionId,
                        session = session,
                        rawText = plan.text
                    )
                    sendingSessionId = null
                    return@launch
                }

                when (toolCall.name) {
                    "web_search" -> {
                        val query = toolCall.args.optString("query").ifBlank { content }
                        val tavilyKey = apiKeyManager.keyForTavily()
                        val searchContext = if (tavilyKey.isNullOrBlank()) {
                            content
                        } else {
                            tavilyRepository.searchContext(
                                apiKey = tavilyKey,
                                query = query
                            ) ?: content
                        }
                        val finalResult = retryTemporaryUnavailable {
                            geminiChatService.sendMessage(
                                apiKey = apiKey,
                                persona = personaEntityForSession(session, sessionId),
                                history = history.toEntities(sessionId),
                                userInput = searchContext,
                                vendor = ApiVendor.Google,
                                safetyLevel = session.persona.safetyLevel,
                                images = loadedImages,
                                webSearchEnabled = false,
                                masterPrompt = masterSystemPrompt
                            )
                        }
                        val reply = finalResult.getOrElse { throwable ->
                            sendingSessionId = null
                            handleSendFailure(throwable, userMessage.id)
                            return@launch
                        }
                        recordUsage(reply)
                        appendModelResponseOrRecover(
                            sessionId = sessionId,
                            session = session,
                            rawText = reply.text
                        )
                        sendingSessionId = null
                    }

                    "anime_image_search" -> {
                        val request = toolCall.args.optString("request").ifBlank { content }
                        val preset = toolCall.args.optString("preset").toAnimeImagePreset() ?: animeImagePreset
                        executeAnimeImageTool(
                            sessionId = sessionId,
                            request = request,
                            preset = preset
                        )
                        sendingSessionId = null
                    }

                    else -> {
                        sendingSessionId = null
                        val cleanResponse = cleanModelResponse(
                            raw = plan.text.ifBlank { "That tool is not available in this build." },
                            speakerName = session.persona.displayName
                        )
                        appendMessage(
                            sessionId = sessionId,
                            message = ChatMessage(role = "model", content = cleanResponse),
                            preview = cleanResponse.take(72)
                        )
                    }
                }
                return@launch
            }

            val outboundContent = tavilyAugmentedInput(
                enabled = webSearchEnabled && shouldOfferWebSearch(content.lowercase(Locale.US)),
                provider = provider,
                content = content
            )

            val result = retryTemporaryUnavailable {
                geminiChatService.sendMessage(
                    apiKey = apiKey,
                    persona = personaEntityForSession(session, sessionId),
                    history = history.toEntities(sessionId),
                    userInput = outboundContent,
                    vendor = provider,
                    safetyLevel = session.persona.safetyLevel,
                    images = loadedImages,
                    webSearchEnabled = false,
                    masterPrompt = masterSystemPrompt
                )
            }

            val reply = result.getOrElse { throwable ->
                sendingSessionId = null
                handleSendFailure(throwable, userMessage.id)
                return@launch
            }
            recordUsage(reply)
            appendModelResponseOrRecover(
                sessionId = sessionId,
                session = session,
                rawText = reply.text
            )
            sendingSessionId = null
        }
    }

    private fun sendGroupDraft(message: String, imageUris: List<Uri>) {
        val cleanMessage = message.trim()
        if (cleanMessage.isEmpty() && imageUris.isEmpty()) return
        val normalizedMessage = cleanMessage.lowercase(Locale.US)
        if (animeImageModeEnabled && imageUris.isEmpty() && shouldOfferAnimeImageSearch(normalizedMessage)) {
            requestAnimeImageFromDraft(cleanMessage)
            return
        }

        viewModelScope.launch {
            val sessionId = activeSessionId
            val session = activeSession
            val members = session.normalizedMembers()
            val content = cleanMessage.ifBlank { "Please respond to this image." }
            if (imageUris.isEmpty()) {
                val localReply = localAppAwarenessReply(content)
                if (localReply != null) {
                    draft = ""
                    attachedImageUris = emptyList()
                    chatError = null
                    appendMessage(sessionId, ChatMessage(role = "user", content = content), content.take(72))
                    awardTextMessageXp(sessionId, cleanMessage)
                    appendMessage(sessionId, ChatMessage(role = "model", content = localReply), localReply.take(72))
                    return@launch
                }
            }

            var missingKeyMember: GroupMember? = null
            for (member in members) {
                if (apiKeyManager.keyForProvider(member.persona.vendor.id).isNullOrBlank()) {
                    missingKeyMember = member
                    break
                }
            }
            if (missingKeyMember != null) {
                selectGroupMember(missingKeyMember.id)
                apiKeyDialogTarget = ApiKeyDialogTarget.Provider
                apiKeyDraft = ""
                apiKeyDialogVisible = true
                chatError = "Add a ${missingKeyMember.persona.vendor.label} API key for ${missingKeyMember.persona.displayName} first."
                return@launch
            }

            val unsupportedImageMember = members.firstOrNull { member ->
                imageUris.isNotEmpty() && !geminiChatService.supportsImageInput(member.persona.vendor, member.persona.model)
            }
            if (unsupportedImageMember != null) {
                chatError = "${unsupportedImageMember.persona.displayName}'s model does not support image input."
                return@launch
            }

            val loadedImages = imageUris.mapNotNull { loadBitmap(it) }
            if (imageUris.isNotEmpty() && loadedImages.isEmpty()) {
                chatError = "Could not read the selected images. Pick them again."
                return@launch
            }

            draft = ""
            attachedImageUris = emptyList()
            chatError = null
            sendingSessionId = sessionId
            val userMessage = ChatMessage(role = "user", content = content, imageUris = imageUris)
            appendMessage(
                sessionId = sessionId,
                message = userMessage,
                preview = if (imageUris.isEmpty()) content.take(72) else "[${imageUris.size} image] ${content.take(58)}"
            )
            awardTextMessageXp(sessionId, cleanMessage)

            val toolAugmentedContent = tavilyAugmentedInput(
                enabled = webSearchEnabled && shouldOfferWebSearch(normalizedMessage),
                provider = ApiVendor.Google,
                content = content
            )

            runCatching {
                val responseTurns = session.responseRounds.coerceIn(1, 3)
                val taggedSpeakers = taggedMembersForMessage(content, members)
                val turnSpeakers = if (taggedSpeakers.isNotEmpty()) {
                    taggedSpeakers.take(responseTurns)
                } else {
                    groupTurnSpeakers(
                        members = members,
                        turns = responseTurns
                    )
                }
                turnSpeakers.forEachIndexed { turnIndex, member ->
                        val memberApiKey = apiKeyManager.keyForProvider(member.persona.vendor.id)
                            ?: error("Missing ${member.persona.vendor.label} key.")
                        val currentHistory = activeSession.messages
                        val memberInput = groupMemberInput(
                            originalUserMessage = toolAugmentedContent,
                            member = member,
                            members = members,
                            turn = turnIndex + 1,
                            totalTurns = turnSpeakers.size
                        )
                        val reply = retryTemporaryUnavailable {
                            geminiChatService.sendMessage(
                                apiKey = memberApiKey,
                                persona = personaEntityForSession(
                                    session = session,
                                    id = member.id,
                                    persona = member.persona
                                ),
                                history = currentHistory.toEntities(sessionId),
                                userInput = memberInput,
                                vendor = member.persona.vendor,
                                safetyLevel = member.persona.safetyLevel,
                                images = if (turnIndex == 0) loadedImages else emptyList(),
                                webSearchEnabled = false,
                                masterPrompt = masterSystemPrompt
                            )
                        }.getOrThrow()
                        recordUsage(reply)
                        appendModelResponseOrRecover(
                            sessionId = sessionId,
                            session = session,
                            rawText = reply.text,
                            speaker = member
                        )
                }
            }.onFailure { throwable ->
                handleSendFailure(throwable, userMessage.id)
            }
            sendingSessionId = null
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

    fun speakMessage(message: ChatMessage) {
        if (message.role != "model" || message.content.isBlank()) return
        viewModelScope.launch {
            val voiceId = apiKeyManager.voiceIdForElevenLabs()?.trim()
            if (voiceId.isNullOrBlank()) {
                openElevenLabsVoiceIdDialog()
                chatError = "Add an ElevenLabs voice ID first."
                return@launch
            }
            val modelId = apiKeyManager.modelIdForElevenLabs()
            val sourceHash = sha256(message.content)
            val cached = chatRepository.latestTtsAudioCache(
                messageId = message.id,
                sourceHash = sourceHash,
                provider = TTS_PROVIDER_ELEVENLABS,
                voiceId = voiceId,
                modelId = modelId
            )
            if (cached != null) {
                val file = File(cached.audioFilePath)
                if (file.exists()) {
                    chatError = null
                    playTtsAudio(file, message.id)
                    return@launch
                }
                chatRepository.deleteTtsAudioCache(cached.id)
            }

            val cleanedText = cleanSpeakableText(message.content)
            if (cleanedText.isBlank()) {
                chatError = "No speakable dialogue in this message."
                return@launch
            }

            val sessionId = activeSessionId
            pendingTtsRequest = PendingTtsRequest(
                messageId = message.id,
                sessionId = sessionId,
                sourceText = message.content,
                sourceHash = sourceHash,
                cleanedText = cleanedText,
                cleanedTextHash = sha256(cleanedText),
                voiceId = voiceId,
                modelId = modelId
            )
            ttsPreviewState = TtsPreviewState(
                messageId = message.id,
                text = "",
                isPreparing = true
            )
            chatError = null

            val googleKey = apiKeyManager.keyForProvider(ApiVendor.Google.id)
            val preparedText = if (!googleKey.isNullOrBlank()) {
                geminiChatService.prepareJapaneseSpeechText(
                    apiKey = googleKey,
                    cleanedText = cleanedText
                ).getOrElse { throwable ->
                    if (cleanedText.containsJapanese()) {
                        cleanedText
                    } else {
                        chatError = throwable.message?.take(180) ?: "Could not prepare speech text."
                        ""
                    }
                }
            } else if (cleanedText.containsJapanese()) {
                cleanedText
            } else {
                chatError = "Add a Google API key to prepare Japanese speech."
                ""
            }

            if (preparedText.isBlank()) {
                ttsPreviewState = null
                pendingTtsRequest = null
                return@launch
            }

            ttsPreviewState = TtsPreviewState(
                messageId = message.id,
                text = preparedText,
                isPreparing = false
            )
        }
    }

    fun updateTtsPreviewText(value: String) {
        ttsPreviewState = ttsPreviewState?.copy(text = value)
    }

    fun dismissTtsPreview() {
        if (ttsPreviewState?.isGenerating == true) return
        ttsPreviewState = null
        pendingTtsRequest = null
    }

    fun generateAndPlayTtsPreview() {
        val request = pendingTtsRequest ?: return
        val preview = ttsPreviewState ?: return
        val finalText = preview.text.trim()
        if (finalText.isBlank()) {
            chatError = "No speech text to generate."
            return
        }
        viewModelScope.launch {
            val apiKey = apiKeyManager.keyForElevenLabs()
            if (apiKey.isNullOrBlank()) {
                openElevenLabsApiKeyDialog()
                chatError = "Add an ElevenLabs API key first."
                return@launch
            }
            val preparedTextHash = sha256(finalText)
            val exactCache = chatRepository.exactTtsAudioCache(
                messageId = request.messageId,
                sourceHash = request.sourceHash,
                preparedTextHash = preparedTextHash,
                provider = TTS_PROVIDER_ELEVENLABS,
                voiceId = request.voiceId,
                modelId = request.modelId
            )
            if (exactCache != null) {
                val file = File(exactCache.audioFilePath)
                if (file.exists()) {
                    ttsPreviewState = null
                    pendingTtsRequest = null
                    chatError = null
                    playTtsAudio(file, request.messageId)
                    return@launch
                }
                chatRepository.deleteTtsAudioCache(exactCache.id)
            }

            ttsPreviewState = preview.copy(isGenerating = true)
            chatError = null
            runCatching {
                val audioBytes = elevenLabsTtsRepository.generateSpeechMp3(
                    apiKey = apiKey,
                    voiceId = request.voiceId,
                    modelId = request.modelId,
                    text = finalText
                )
                val cacheId = UUID.randomUUID().toString()
                val audioFile = writeTtsAudioFile(cacheId, audioBytes)
                chatRepository.saveTtsAudioCache(
                    TtsAudioCacheEntity(
                        id = cacheId,
                        messageId = request.messageId,
                        sessionId = request.sessionId,
                        sourceHash = request.sourceHash,
                        cleanedTextHash = request.cleanedTextHash,
                        preparedTextHash = preparedTextHash,
                        provider = TTS_PROVIDER_ELEVENLABS,
                        voiceId = request.voiceId,
                        modelId = request.modelId,
                        language = "ja-JP",
                        audioFilePath = audioFile.absolutePath,
                        characterCount = finalText.length
                    )
                )
                audioFile
            }.onSuccess { audioFile ->
                ttsPreviewState = null
                pendingTtsRequest = null
                playTtsAudio(audioFile, request.messageId)
            }.onFailure { throwable ->
                ttsPreviewState = ttsPreviewState?.copy(isGenerating = false)
                chatError = when (throwable) {
                    is ElevenLabsTtsException -> when (throwable.statusCode) {
                        401, 403 -> "ElevenLabs key or voice permission failed."
                        429 -> "TTS quota hit. Try again later."
                        else -> throwable.message?.take(180) ?: "ElevenLabs TTS failed."
                    }

                    else -> throwable.message?.take(180) ?: "Could not generate speech."
                }
            }
        }
    }

    fun stopTtsPlayback() {
        mediaPlayer?.let { player ->
            runCatching {
                if (player.isPlaying) player.stop()
                player.release()
            }
        }
        mediaPlayer = null
        ttsPlaybackMessageId = null
    }

    fun requestAnimeImage(request: String) {
        val cleanRequest = request.trim()
        if (cleanRequest.isEmpty()) return
        viewModelScope.launch {
            requestAnimeImageInternal(cleanRequest, clearDraft = false)
        }
    }

    private fun requestAnimeImageFromDraft(request: String) {
        val cleanRequest = request.trim()
        if (cleanRequest.isEmpty()) return
        viewModelScope.launch {
            requestAnimeImageInternal(cleanRequest, clearDraft = true)
        }
    }

    private suspend fun requestAnimeImageInternal(
        cleanRequest: String,
        clearDraft: Boolean
    ) {
        val r34ApiKey = apiKeyManager.keyForRule34Api()
        val r34UserId = apiKeyManager.userIdForRule34()
        if (r34UserId.isNullOrBlank() || r34ApiKey.isNullOrBlank()) {
            if (r34UserId.isNullOrBlank()) {
                openRule34UserIdDialog()
            } else {
                openRule34ApiKeyDialog()
            }
            chatError = "Add Rule34 credentials first."
            return
        }

        val sessionId = activeSessionId
        val userMessage = ChatMessage(role = "user", content = cleanRequest)
        val loadingMessageId = UUID.randomUUID().toString()
        val loadingMessage = ChatMessage(
            id = loadingMessageId,
            role = "model",
            content = "",
            isImageLoading = true
        )
        if (clearDraft) {
            draft = ""
        }
        chatError = null
        appendMessage(
            sessionId = sessionId,
            message = userMessage,
            preview = cleanRequest.take(72)
        )
        awardTextMessageXp(sessionId, cleanRequest)
        appendMessage(
            sessionId = sessionId,
            message = loadingMessage,
            preview = "Finding image..."
        )

        val tags = pickAnimeImageTags(cleanRequest, animeImagePreset)
        val imageUrl = rule34ImageRepository.randomExplicitImageUrl(
            apiKey = r34ApiKey,
            userId = r34UserId,
            tags = tags
        )

        val replacement = if (imageUrl == null) {
            ChatMessage(
                id = loadingMessageId,
                role = "model",
                content = "Couldn't find an image for that."
            )
        } else {
            ChatMessage(
                id = loadingMessageId,
                role = "model",
                content = "",
                remoteImageUrl = imageUrl
            )
        }
        replaceMessage(
            sessionId = sessionId,
            messageId = loadingMessageId,
            message = replacement,
            preview = if (imageUrl == null) replacement.content else "[image] $cleanRequest".take(72)
        )
    }

    private suspend fun executeAnimeImageTool(
        sessionId: String,
        request: String,
        preset: AnimeImagePreset
    ) {
        val cleanRequest = request.trim()
        if (cleanRequest.isEmpty()) return

        val r34ApiKey = apiKeyManager.keyForRule34Api()
        val r34UserId = apiKeyManager.userIdForRule34()
        if (r34UserId.isNullOrBlank() || r34ApiKey.isNullOrBlank()) {
            if (r34UserId.isNullOrBlank()) {
                openRule34UserIdDialog()
            } else {
                openRule34ApiKeyDialog()
            }
            chatError = "Add Rule34 credentials first."
            return
        }

        val loadingMessageId = UUID.randomUUID().toString()
        appendMessage(
            sessionId = sessionId,
            message = ChatMessage(
                id = loadingMessageId,
                role = "model",
                content = "",
                isImageLoading = true
            ),
            preview = "Finding image..."
        )

        val tags = pickAnimeImageTags(cleanRequest, preset)
        val imageUrl = rule34ImageRepository.randomExplicitImageUrl(
            apiKey = r34ApiKey,
            userId = r34UserId,
            tags = tags
        )
        val replacement = if (imageUrl == null) {
            ChatMessage(
                id = loadingMessageId,
                role = "model",
                content = "Couldn't find an image for that."
            )
        } else {
            ChatMessage(
                id = loadingMessageId,
                role = "model",
                content = "",
                remoteImageUrl = imageUrl
            )
        }
        replaceMessage(
            sessionId = sessionId,
            messageId = loadingMessageId,
            message = replacement,
            preview = if (imageUrl == null) replacement.content else "[image] $cleanRequest".take(72)
        )
    }

    private suspend fun runProviderToolFallback(
        enabledTools: Set<String>,
        sessionId: String,
        session: ChatSession,
        userMessage: ChatMessage,
        content: String,
        apiKey: String,
        loadedImages: List<Bitmap>
    ) {
        if ("anime_image_search" in enabledTools) {
            executeAnimeImageTool(
                sessionId = sessionId,
                request = content,
                preset = animeImagePreset
            )
            sendingSessionId = null
            return
        }

        if ("web_search" in enabledTools) {
            val searchContext = tavilyAugmentedInput(
                enabled = true,
                provider = session.persona.vendor,
                content = content
            )
            val finalResult = retryTemporaryUnavailable {
                geminiChatService.sendMessage(
                    apiKey = apiKey,
                    persona = personaEntityForSession(session, sessionId),
                    history = session.messages.toEntities(sessionId),
                    userInput = searchContext,
                    vendor = session.persona.vendor,
                    safetyLevel = session.persona.safetyLevel,
                    images = loadedImages,
                    webSearchEnabled = false,
                    masterPrompt = masterSystemPrompt
                )
            }
            val reply = finalResult.getOrElse { throwable ->
                sendingSessionId = null
                handleSendFailure(throwable, userMessage.id)
                return
            }
            recordUsage(reply)
            appendModelResponseOrRecover(
                sessionId = sessionId,
                session = session,
                rawText = reply.text
            )
            sendingSessionId = null
            return
        }

        sendingSessionId = null
    }

    fun dismissRateLimitDialog() {
        rateLimitDialogVisible = false
        rateLimitRetryMessageId = null
    }

    fun retryAfterRateLimit() {
        val messageId = rateLimitRetryMessageId ?: return
        rateLimitDialogVisible = false
        rateLimitRetryMessageId = null
        retryMessage(messageId)
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

    fun openVoiceCall() {
        if (persona.vendor != ApiVendor.Google) {
            chatError = "Voice calls use Gemini only. Switch this persona to Google."
            return
        }
        sessionDrawerVisible = false
        personaSheetVisible = false
        appSettingsVisible = false
        voiceCallMuted = false
        voiceCallVideoEnabled = false
        voiceCallCameraFacing = VoiceCallCameraFacing.Front
        voiceCallScreenShareEnabled = false
        voiceCallSessionId = activeSessionId
        voiceCallModelMessageId = null
        currentVoiceCallModelLineId = null
        currentVoiceCallModelText = ""
        voiceCallVisualReconnectPending = false
        lastVoiceCallVisualReconnectAt = 0L
        voiceCallTranscriptLines.clear()
        voiceCallError = null
        voiceCallActive = true
        startGeminiLiveCall()
    }

    fun closeVoiceCall() {
        geminiLiveCallService.stop()
        ScreenShareForegroundService.stop(getApplication())
        completeVoiceCallModelTurn(persist = false)
        persistChatState()
        voiceCallActive = false
        voiceCallStatus = "Idle"
        voiceCallSessionId = null
        voiceCallModelMessageId = null
        currentVoiceCallModelLineId = null
        currentVoiceCallModelText = ""
        voiceCallVisualReconnectPending = false
        voiceCallVideoEnabled = false
        voiceCallCameraFacing = VoiceCallCameraFacing.Front
        voiceCallScreenShareEnabled = false
        voiceCallError = null
    }

    fun toggleVoiceCallMute() {
        voiceCallMuted = !voiceCallMuted
        geminiLiveCallService.setMuted(voiceCallMuted)
        voiceCallStatus = if (voiceCallMuted) "Muted" else "Listening..."
    }

    fun enableVoiceCallVideo() {
        if (!voiceCallActive) return
        if (voiceCallScreenShareEnabled) {
            ScreenShareForegroundService.stop(getApplication())
            voiceCallScreenShareEnabled = false
        }
        voiceCallVideoEnabled = true
    }

    fun disableVoiceCallVideo() {
        voiceCallVideoEnabled = false
    }

    fun switchVoiceCallCamera() {
        if (!voiceCallActive || !voiceCallVideoEnabled) return
        voiceCallCameraFacing = when (voiceCallCameraFacing) {
            VoiceCallCameraFacing.Front -> VoiceCallCameraFacing.Back
            VoiceCallCameraFacing.Back -> VoiceCallCameraFacing.Front
        }
    }

    fun startVoiceCallScreenShare(resultCode: Int, resultData: Intent) {
        if (!voiceCallActive) return
        voiceCallVideoEnabled = false
        voiceCallScreenShareEnabled = true
        ScreenShareForegroundService.start(
            context = getApplication(),
            resultCode = resultCode,
            resultData = resultData,
            onFrame = geminiLiveCallService::sendVideoFrame,
            onError = { message ->
                viewModelScope.launch(Dispatchers.Main) {
                    voiceCallScreenShareEnabled = false
                    voiceCallError = message
                }
            },
            onStopped = {
                viewModelScope.launch(Dispatchers.Main) {
                    voiceCallScreenShareEnabled = false
                }
            }
        )
    }

    fun stopVoiceCallScreenShare() {
        ScreenShareForegroundService.stop(getApplication())
        voiceCallScreenShareEnabled = false
    }

    fun sendVoiceCallVideoFrame(jpegBytes: ByteArray) {
        if (voiceCallVideoEnabled && voiceCallActive) {
            geminiLiveCallService.sendVideoFrame(jpegBytes)
        }
    }

    fun retryVoiceCall() {
        if (!voiceCallActive) return
        voiceCallError = null
        completeVoiceCallModelTurn()
        startGeminiLiveCall()
    }

    fun updateGeminiLiveVoice(voice: GeminiLiveVoice) {
        selectedGeminiLiveVoice = voice
        viewModelScope.launch {
            settingsDataStore.edit { preferences ->
                preferences[geminiLiveVoiceKey] = voice.id
            }
        }
        if (voiceCallActive) {
            completeVoiceCallModelTurn()
            startGeminiLiveCall()
        }
    }

    fun handleVoicePermissionDenied() {
        chatError = "Microphone permission is required for voice calls."
    }

    private fun startGeminiLiveCall() {
        voiceCallStatus = "Connecting..."
        voiceCallError = null
        viewModelScope.launch {
            val apiKey = apiKeyManager.keyForProvider(ApiVendor.Google.id)
            if (apiKey.isNullOrBlank()) {
                voiceCallActive = false
                voiceCallStatus = "Idle"
                voiceCallSessionId = null
                openApiKeyDialog()
                chatError = "Add a Google API key first."
                return@launch
            }
            val callSessionId = voiceCallSessionId ?: activeSessionId.also {
                voiceCallSessionId = it
            }
            val session = sessions.firstOrNull { it.id == callSessionId } ?: activeSession
            val callMembers = session.normalizedMembers()
            val callMember = callMembers.firstOrNull { it.id == session.activeMemberId }
                ?: callMembers.first()
            val personaEntity = personaEntityForSession(
                session = session,
                id = callMember.id,
                persona = callMember.persona
            )
            val systemInstruction = listOf(
                masterSystemPrompt.takeIf { it.isNotBlank() },
                skillRepository.combinedContent.takeIf { it.isNotBlank() },
                personaEntity.systemPrompt
            ).filterNotNull().joinToString("\n\n---\n\n")
            val callSpeakerId = callMember.id
            val callSpeakerName = personaEntity.name
            geminiLiveCallService.start(
                config = GeminiLiveCallConfig(
                    apiKey = apiKey,
                    modelId = geminiLiveModelId,
                    systemInstruction = systemInstruction,
                    voiceName = selectedGeminiLiveVoice.label,
                    priorConversationContext = liveConversationContext(session)
                ),
                onStatus = { status ->
                    viewModelScope.launch(Dispatchers.Main) {
                        voiceCallStatus = if (voiceCallMuted && status == "Listening...") "Muted" else status
                        if (
                            status.startsWith("Connected") ||
                            status == "Listening..." ||
                            status == "Speaking..."
                        ) {
                            voiceCallError = null
                        }
                    }
                },
                onError = { message ->
                    viewModelScope.launch(Dispatchers.Main) {
                        if (message.isRecoverableLiveGoAway()) {
                            reconnectGeminiLiveCallSilently()
                            return@launch
                        }
                        voiceCallStatus = "Connection lost"
                        voiceCallError = when {
                            message.contains("ping", ignoreCase = true) ->
                                "The live connection timed out."
                            message.contains("timeout", ignoreCase = true) ->
                                "Gemini Live did not respond in time."
                            else -> message.take(180)
                        }
                    }
                },
                onUserTranscript = { transcript ->
                    viewModelScope.launch(Dispatchers.Main) {
                        appendVoiceCallUserTranscript(
                            sessionId = callSessionId,
                            transcript = transcript
                        )
                    }
                },
                onModelTranscriptDelta = { transcript ->
                    viewModelScope.launch(Dispatchers.Main) {
                        appendVoiceCallModelTranscript(
                            sessionId = callSessionId,
                            transcript = transcript,
                            speakerId = callSpeakerId,
                            speakerName = callSpeakerName
                        )
                    }
                },
                onModelTurnComplete = {
                    viewModelScope.launch(Dispatchers.Main) {
                        completeVoiceCallModelTurn()
                    }
                }
            )
        }
    }

    private fun reconnectGeminiLiveCallSilently() {
        if (!voiceCallActive || voiceCallVisualReconnectPending) return
        voiceCallVisualReconnectPending = true
        voiceCallStatus = "Reconnecting..."
        voiceCallError = null
        completeVoiceCallModelTurn()
        geminiLiveCallService.stop()

        viewModelScope.launch {
            delay(450L)
            if (voiceCallActive) {
                startGeminiLiveCall()
            }
            delay(1_200L)
            voiceCallVisualReconnectPending = false
        }
    }

    private fun appendVoiceCallUserTranscript(
        sessionId: String,
        transcript: String
    ) {
        val cleanTranscript = transcript.trim()
        if (cleanTranscript.isBlank()) return
        val lastLine = voiceCallTranscriptLines.lastOrNull()
        if (lastLine?.role == "user" && lastLine.text == cleanTranscript) return

        voiceCallTranscriptLines += LiveCallTranscriptLine(
            role = "user",
            text = cleanTranscript
        )
        appendVoiceCallMessage(
            sessionId = sessionId,
            message = ChatMessage(
                role = "user",
                content = cleanTranscript
            )
        )
        persistChatState()
    }

    private fun appendVoiceCallModelTranscript(
        sessionId: String,
        transcript: String,
        speakerId: String?,
        speakerName: String?
    ) {
        if (transcript.isBlank()) return
        currentVoiceCallModelText += transcript
        val cleanTranscript = currentVoiceCallModelText.trim()
        if (cleanTranscript.isBlank()) return

        val lineIndex = currentVoiceCallModelLineId?.let { id ->
            voiceCallTranscriptLines.indexOfFirst { it.id == id }
        } ?: -1
        if (lineIndex >= 0) {
            voiceCallTranscriptLines[lineIndex] = voiceCallTranscriptLines[lineIndex].copy(
                text = cleanTranscript,
                streaming = true
            )
        } else {
            val line = LiveCallTranscriptLine(
                role = "model",
                text = cleanTranscript,
                streaming = true
            )
            voiceCallTranscriptLines += line
            currentVoiceCallModelLineId = line.id
        }

        val index = sessions.indexOfFirst { it.id == sessionId }
        if (index < 0) return
        val session = sessions[index]
        val content = cleanTranscript
        val messageIndex = voiceCallModelMessageId?.let { id ->
            session.messages.indexOfFirst { message -> message.id == id }
        } ?: -1
        val updatedMessages = session.messages.toMutableList()

        if (messageIndex >= 0) {
            val existing = updatedMessages[messageIndex]
            updatedMessages[messageIndex] = existing.copy(content = content)
        } else {
            val message = ChatMessage(
                role = "model",
                content = content,
                speakerId = speakerId,
                speakerName = speakerName
            )
            updatedMessages += message
            voiceCallModelMessageId = message.id
        }

        sessions[index] = session.copy(
            messages = updatedMessages,
            preview = previewFor(updatedMessages),
            updatedAt = currentTime()
        )

        maybeReconnectLiveCallAfterVisualRefusal(sessionId, cleanTranscript)
    }

    private fun maybeReconnectLiveCallAfterVisualRefusal(
        sessionId: String,
        transcript: String
    ) {
        if (!voiceCallActive || voiceCallVisualReconnectPending) return
        if (!voiceCallVideoEnabled && !voiceCallScreenShareEnabled) return
        if (!transcript.isLiveVisualRefusal()) return

        val now = System.currentTimeMillis()
        if (now - lastVoiceCallVisualReconnectAt < LIVE_VISUAL_RECONNECT_COOLDOWN_MS) return

        voiceCallVisualReconnectPending = true
        lastVoiceCallVisualReconnectAt = now
        voiceCallStatus = "Reconnecting visual context..."
        voiceCallError = null
        dropCurrentVoiceCallModelTurn(sessionId)
        geminiLiveCallService.stop()

        viewModelScope.launch {
            delay(550L)
            if (voiceCallActive) {
                startGeminiLiveCall()
            }
            delay(1_200L)
            voiceCallVisualReconnectPending = false
        }
    }

    private fun dropCurrentVoiceCallModelTurn(sessionId: String) {
        currentVoiceCallModelLineId?.let { lineId ->
            val lineIndex = voiceCallTranscriptLines.indexOfFirst { it.id == lineId }
            if (lineIndex >= 0) {
                voiceCallTranscriptLines.removeAt(lineIndex)
            }
        }

        voiceCallModelMessageId?.let { messageId ->
            val sessionIndex = sessions.indexOfFirst { it.id == sessionId }
            if (sessionIndex >= 0) {
                val session = sessions[sessionIndex]
                val updatedMessages = session.messages.filterNot { it.id == messageId }
                sessions[sessionIndex] = session.copy(
                    messages = updatedMessages,
                    preview = previewFor(updatedMessages),
                    updatedAt = currentTime()
                )
            }
        }

        currentVoiceCallModelLineId = null
        currentVoiceCallModelText = ""
        voiceCallModelMessageId = null
    }

    private fun completeVoiceCallModelTurn(persist: Boolean = true) {
        currentVoiceCallModelLineId?.let { id ->
            val lineIndex = voiceCallTranscriptLines.indexOfFirst { it.id == id }
            if (lineIndex >= 0) {
                voiceCallTranscriptLines[lineIndex] =
                    voiceCallTranscriptLines[lineIndex].copy(streaming = false)
            }
        }
        val completedText = currentVoiceCallModelText.trim()
        currentVoiceCallModelLineId = null
        currentVoiceCallModelText = ""
        voiceCallModelMessageId = null
        if (persist && completedText.isNotBlank()) {
            persistChatState()
        }
    }

    private fun appendVoiceCallMessage(
        sessionId: String,
        message: ChatMessage
    ) {
        val index = sessions.indexOfFirst { it.id == sessionId }
        if (index < 0) return
        val session = sessions[index]
        val updatedMessages = session.messages + message
        sessions[index] = session.copy(
            messages = updatedMessages,
            preview = previewFor(updatedMessages),
            updatedAt = currentTime()
        )
    }

    private fun liveConversationContext(session: ChatSession): String? {
        if (session.messages.isEmpty()) return null
        val history = session.messages
            .takeLast(MAX_LIVE_CONTEXT_MESSAGES)
            .joinToString("\n") { message ->
                val speaker = when (message.role) {
                    "user" -> "User"
                    "model" -> message.speakerName?.takeIf { it.isNotBlank() } ?: "Assistant"
                    else -> message.role
                }
                val content = message.content
                    .removePrefix("[User]:")
                    .removePrefix("[AI]:")
                    .trim()
                "$speaker: $content"
            }
            .takeLast(MAX_LIVE_CONTEXT_CHARS)
        return """
            [Prior conversation context - continue naturally from this]

            $history

            [End of context. The user is now joining via voice. Continue the conversation.]
        """.trimIndent()
    }

    fun selectSession(id: String) {
        activeSessionId = id
        draft = ""
        attachedImageUris = emptyList()
        sessionDrawerVisible = false
        persistChatState()
    }

    fun createSession(projectId: String? = null) {
        val firstMember = GroupMember(persona = PersonaUiState(displayName = "New Persona"))
        val session = ChatSession(
            persona = firstMember.persona,
            members = listOf(firstMember),
            activeMemberId = firstMember.id,
            responseRounds = 1,
            projectId = projectId,
            preview = "No messages yet"
        )
        sessions.add(0, session)
        activeSessionId = session.id
        draft = ""
        attachedImageUris = emptyList()
        sessionDrawerVisible = false
        persistChatState()
    }

    fun createProject(): String {
        val project = ProjectUiState()
        projects.add(0, project)
        persistProjects()
        return project.id
    }

    fun updateProjectName(projectId: String, value: String) {
        updateProject(projectId) { project ->
            project.copy(
                name = value.take(48),
                updatedAt = currentTime()
            )
        }
    }

    fun updateProjectDescription(projectId: String, value: String) {
        updateProject(projectId) { project ->
            project.copy(
                description = value.take(280),
                updatedAt = currentTime()
            )
        }
    }

    fun updateProjectInstruction(projectId: String, value: String) {
        updateProject(projectId) { project ->
            project.copy(
                instruction = value.take(MAX_PROJECT_INSTRUCTION_CHARS),
                updatedAt = currentTime()
            )
        }
    }

    fun createSessionInProject(projectId: String) {
        createSession(projectId)
    }

    fun moveSessionToProject(sessionId: String, projectId: String?) {
        if (projectId != null && projects.none { it.id == projectId }) return
        val index = sessions.indexOfFirst { it.id == sessionId }
        if (index < 0) return
        sessions[index] = sessions[index].copy(
            projectId = projectId,
            updatedAt = currentTime()
        )
        persistChatState()
    }

    fun selectProject(projectId: String) {
        val session = sessions.firstOrNull { it.projectId == projectId }
        if (session != null) {
            selectSession(session.id)
        } else {
            createSessionInProject(projectId)
        }
    }

    fun duplicateSessionSettings(sessionId: String) {
        val source = sessions.firstOrNull { it.id == sessionId } ?: return
        val memberIdMap = source.normalizedMembers().associate { member -> member.id to UUID.randomUUID().toString() }
        val copiedMembers = source.normalizedMembers().map { member ->
            member.copy(id = memberIdMap.getValue(member.id))
        }
        val copiedActiveId = memberIdMap[source.activeMemberId] ?: copiedMembers.first().id
        val copiedPersona = copiedMembers.firstOrNull { it.id == copiedActiveId }?.persona
            ?: copiedMembers.first().persona
        val copiedSession = source.copy(
            id = UUID.randomUUID().toString(),
            persona = copiedPersona,
            members = copiedMembers,
            activeMemberId = copiedActiveId,
            preview = "No messages yet",
            updatedAt = currentTime(),
            messages = emptyList()
        )
        sessions.add(0, copiedSession)
        activeSessionId = copiedSession.id
        draft = ""
        attachedImageUris = emptyList()
        sessionDrawerVisible = false
        persistChatState()
    }

    fun cloneSession(sessionId: String) {
        val source = sessions.firstOrNull { it.id == sessionId } ?: return
        val sourceTitle = source.title.ifBlank {
            source.normalizedMembers()
                .joinToString(" + ") { member -> member.persona.displayName.ifBlank { "New Persona" } }
        }
        val clone = source.freshImportCopy().copy(
            title = sourceTitle.let { title ->
                if (title.endsWith(" copy", ignoreCase = true)) title else "$title copy"
            },
            updatedAt = currentTime()
        )
        sessions.add(0, clone)
        activeSessionId = clone.id
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

    fun renameSession(sessionId: String, title: String) {
        val cleanedTitle = title.trim().take(72)
        val index = sessions.indexOfFirst { it.id == sessionId }
        if (index < 0) return
        sessions[index] = sessions[index].copy(
            title = cleanedTitle,
            updatedAt = currentTime()
        )
        persistChatState()
    }

    fun updateSessionHeaderName(value: String) {
        updateActiveSession { session ->
            session.copy(
                title = value.take(72),
                updatedAt = currentTime()
            )
        }
    }

    fun sessionExportFileName(sessionId: String): String {
        val session = sessions.firstOrNull { it.id == sessionId } ?: activeSession
        return exportFileNameFor(session)
    }

    fun exportSessionToUri(sessionId: String, destination: Uri) {
        val session = sessions.firstOrNull { it.id == sessionId } ?: return
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val output = getApplication<Application>().contentResolver.openOutputStream(destination)
                        ?: error("Could not open export destination.")
                    output.bufferedWriter().use { writer ->
                        writer.write(exportSessionJson(session))
                    }
                }
            }
            chatError = result.exceptionOrNull()?.message?.take(160)
        }
    }

    fun importSessionFromUri(source: Uri) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val input = getApplication<Application>().contentResolver.openInputStream(source)
                        ?: error("Could not open import file.")
                    val raw = input.bufferedReader().use { reader -> reader.readText() }
                    parseSessionExportJson(raw)?.freshImportCopy()
                        ?: error("This is not a valid Zora session export.")
                }
            }
            result
                .onSuccess { importedSession ->
                    sessions.add(0, importedSession)
                    activeSessionId = importedSession.id
                    draft = ""
                    attachedImageUris = emptyList()
                    sessionDrawerVisible = false
                    chatError = null
                    persistChatState()
                }
                .onFailure { throwable ->
                    chatError = throwable.message?.take(160) ?: "Import failed."
                }
        }
    }

    fun toggleMorePersonaOptions() {
        morePersonaOptions = !morePersonaOptions
    }

    fun openApiKeyDialog() {
        apiKeyDialogTarget = ApiKeyDialogTarget.Provider
        apiKeyDraft = ""
        apiKeyDialogVisible = true
    }

    fun openTavilyApiKeyDialog() {
        apiKeyDialogTarget = ApiKeyDialogTarget.Tavily
        apiKeyDraft = ""
        apiKeyDialogVisible = true
    }

    fun openRule34UserIdDialog() {
        apiKeyDialogTarget = ApiKeyDialogTarget.Rule34UserId
        apiKeyDraft = ""
        apiKeyDialogVisible = true
    }

    fun openRule34ApiKeyDialog() {
        apiKeyDialogTarget = ApiKeyDialogTarget.Rule34ApiKey
        apiKeyDraft = ""
        apiKeyDialogVisible = true
    }

    fun openElevenLabsApiKeyDialog() {
        apiKeyDialogTarget = ApiKeyDialogTarget.ElevenLabsKey
        apiKeyDraft = ""
        apiKeyDialogVisible = true
    }

    fun openElevenLabsVoiceIdDialog() {
        apiKeyDialogTarget = ApiKeyDialogTarget.ElevenLabsVoiceId
        apiKeyDraft = ""
        apiKeyDialogVisible = true
    }

    fun openElevenLabsModelIdDialog() {
        apiKeyDialogTarget = ApiKeyDialogTarget.ElevenLabsModelId
        apiKeyDraft = ApiKeyManager.DEFAULT_ELEVENLABS_MODEL_ID
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
            when (apiKeyDialogTarget) {
                ApiKeyDialogTarget.Provider -> apiKeyManager.replaceProviderKey(persona.vendor.id, apiKeyDraft)
                ApiKeyDialogTarget.Tavily -> apiKeyManager.replaceTavilyKey(apiKeyDraft)
                ApiKeyDialogTarget.Rule34UserId -> apiKeyManager.replaceRule34UserId(apiKeyDraft)
                ApiKeyDialogTarget.Rule34ApiKey -> apiKeyManager.replaceRule34ApiKey(apiKeyDraft)
                ApiKeyDialogTarget.ElevenLabsKey -> apiKeyManager.replaceElevenLabsKey(apiKeyDraft)
                ApiKeyDialogTarget.ElevenLabsVoiceId -> apiKeyManager.replaceElevenLabsVoiceId(apiKeyDraft)
                ApiKeyDialogTarget.ElevenLabsModelId -> apiKeyManager.replaceElevenLabsModelId(apiKeyDraft)
            }
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

    fun clearTavilyApiKey() {
        viewModelScope.launch {
            apiKeyManager.clearTavilyKey()
            webSearchEnabled = false
            chatError = null
        }
    }

    fun clearRule34UserId() {
        viewModelScope.launch {
            apiKeyManager.clearRule34UserId()
            chatError = null
        }
    }

    fun clearRule34ApiKey() {
        viewModelScope.launch {
            apiKeyManager.clearRule34ApiKey()
            chatError = null
        }
    }

    fun clearElevenLabsApiKey() {
        viewModelScope.launch {
            apiKeyManager.clearElevenLabsKey()
            chatError = null
        }
    }

    fun clearElevenLabsVoiceId() {
        viewModelScope.launch {
            apiKeyManager.clearElevenLabsVoiceId()
            chatError = null
        }
    }

    fun clearElevenLabsModelId() {
        viewModelScope.launch {
            apiKeyManager.clearElevenLabsModelId()
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
    }

    fun updatePersonaName(value: String) {
        updatePersona { it.copy(displayName = value.take(32)) }
    }

    fun selectGroupMember(memberId: String) {
        updateActiveSession { session ->
            val members = session.normalizedMembers()
            val selected = members.firstOrNull { it.id == memberId } ?: return@updateActiveSession session
            session.copy(
                persona = selected.persona,
                members = members,
                activeMemberId = selected.id
            )
        }
    }

    fun addGroupMember() {
        updateActiveSession { session ->
            val members = session.normalizedMembers()
            if (members.size >= 4) return@updateActiveSession session
            val nextNumber = members.size + 1
            val member = GroupMember(
                persona = PersonaUiState(displayName = "AI $nextNumber")
            )
            session.copy(
                members = members + member,
                activeMemberId = member.id,
                persona = member.persona
            )
        }
    }

    fun removeGroupMember(memberId: String) {
        updateActiveSession { session ->
            val members = session.normalizedMembers()
            if (members.size <= 1) return@updateActiveSession session
            val updatedMembers = members.filterNot { it.id == memberId }
            val nextActiveId = if (session.activeMemberId == memberId) {
                updatedMembers.first().id
            } else {
                session.activeMemberId
            }
            val activePersona = updatedMembers.firstOrNull { it.id == nextActiveId }?.persona
                ?: updatedMembers.first().persona
            session.copy(
                persona = activePersona,
                members = updatedMembers,
                activeMemberId = nextActiveId
            )
        }
    }

    fun updateResponseRounds(value: Int) {
        updateActiveSession { session ->
            session.copy(responseRounds = value.coerceIn(1, 3))
        }
    }

    fun updateMemoryEnabled(value: Boolean) {
        updateActiveSession { session ->
            session.copy(memoryEnabled = value)
        }
    }

    fun updateLevelSystemEnabled(value: Boolean) {
        updateActiveSession { session ->
            session.copy(levelSystemEnabled = value)
        }
    }

    fun updateGlobalMemoryBlock(value: String) {
        val nextMemory = value.take(MAX_GLOBAL_MEMORY_CHARS)
        globalMemoryBlock = nextMemory
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.edit { preferences ->
                preferences[globalMemoryKey] = nextMemory
            }
        }
    }

    fun updatePersonaPrompt(value: String) {
        updatePersona { it.copy(instructionPrompt = value) }
    }

    fun updateInstructionMode(value: InstructionMode) {
        updatePersona { it.copy(instructionMode = value) }
    }

    fun updateBeginnerRole(value: String) {
        updatePersona { it.copy(beginnerRole = value.take(600)) }
    }

    fun updateBeginnerStyle(value: String) {
        updatePersona { it.copy(beginnerStyle = value.take(600)) }
    }

    fun updateBeginnerLimits(value: String) {
        updatePersona { it.copy(beginnerLimits = value.take(600)) }
    }

    fun updateStoryLore(value: String) {
        updateActiveSession { session ->
            session.copy(storyLore = value.take(MAX_STORY_LORE_CHARS))
        }
    }

    fun updatePersonaModel(value: String) {
        updatePersona { it.copy(model = value) }
    }

    fun updateSafetyLevel(value: SafetyLevel) {
        updatePersona { it.copy(safetyLevel = value) }
    }

    fun updateThinkingEffort(value: GeminiThinkingEffort) {
        updatePersona { it.copy(thinkingEffort = value) }
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

    fun updateSessionHeaderAvatar(uri: Uri?) {
        persistImagePermission(uri)
        updateActiveSession { session ->
            session.copy(
                headerAvatarUri = uri,
                headerAvatarScale = 1f,
                headerAvatarOffsetX = 0f,
                headerAvatarOffsetY = 0f,
                updatedAt = currentTime()
            )
        }
    }

    fun transformSessionHeaderAvatar(zoomChange: Float, panX: Float, panY: Float) {
        updateActiveSession { session ->
            val nextScale = (session.headerAvatarScale * zoomChange).coerceIn(1f, 4f)
            session.copy(
                headerAvatarScale = nextScale,
                headerAvatarOffsetX = (session.headerAvatarOffsetX + panX).coerceIn(-180f, 180f),
                headerAvatarOffsetY = (session.headerAvatarOffsetY + panY).coerceIn(-180f, 180f),
                updatedAt = currentTime()
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
        webSearchEnabled = value && canUseWebSearch
    }

    fun updateAnimeImageModeEnabled(value: Boolean) {
        animeImageModeEnabled = value
    }

    fun updateAnimeImagePreset(value: AnimeImagePreset) {
        animeImagePreset = value
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

    private val activeGroupMember: GroupMember
        get() {
            val session = activeSession
            val members = session.normalizedMembers()
            return members.firstOrNull { it.id == session.activeMemberId } ?: members.first()
        }

    private fun updatePersona(transform: (PersonaUiState) -> PersonaUiState) {
        updateActiveSession { session ->
            val members = session.normalizedMembers()
            val activeId = session.activeMemberId.takeIf { id -> members.any { it.id == id } }
                ?: members.first().id
            val updatedMembers = members.map { member ->
                if (member.id == activeId) member.copy(persona = transform(member.persona)) else member
            }
            val activePersona = updatedMembers.first { it.id == activeId }.persona
            session.copy(
                persona = activePersona,
                members = updatedMembers,
                activeMemberId = activeId
            )
        }
    }

    private fun updateActiveSession(transform: (ChatSession) -> ChatSession) {
        val index = sessions.indexOfFirst { it.id == activeSessionId }
        if (index >= 0) {
            sessions[index] = transform(sessions[index])
            persistChatState()
        }
    }

    private fun updateProject(projectId: String, transform: (ProjectUiState) -> ProjectUiState) {
        val index = projects.indexOfFirst { it.id == projectId }
        if (index >= 0) {
            projects[index] = transform(projects[index])
            persistProjects()
        }
    }

    private fun memoryForSession(session: ChatSession): String? {
        val memory = globalMemoryBlock.trim()
        if (!session.memoryEnabled || memory.isBlank()) return null
        return if (memory.length <= MAX_INJECTED_GLOBAL_MEMORY_CHARS) {
            memory
        } else {
            memory.take(MAX_INJECTED_GLOBAL_MEMORY_CHARS) +
                "\n\n[Global memory continues locally, but only the first $MAX_INJECTED_GLOBAL_MEMORY_CHARS characters are injected for this request.]"
        }
    }

    private fun projectInstructionForSession(session: ChatSession): String? {
        val projectId = session.projectId ?: return null
        return projects.firstOrNull { it.id == projectId }
            ?.instruction
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    private fun personaEntityForSession(
        session: ChatSession,
        id: String,
        persona: PersonaUiState = session.persona
    ): PersonaEntity {
        val currentSession = sessions.firstOrNull { it.id == session.id } ?: session
        return persona.toEntity(
            id = id,
            memoryBlock = memoryForSession(currentSession),
            projectInstruction = projectInstructionForSession(currentSession),
            storyLore = currentSession.storyLore,
            levelInstruction = levelInstructionForSession(currentSession)
        )
    }

    private fun awardTextMessageXp(sessionId: String, originalText: String) {
        if (originalText.isBlank()) return
        val index = sessions.indexOfFirst { it.id == sessionId }
        if (index < 0) return
        val session = sessions[index]
        if (!session.levelSystemEnabled || session.levelXp >= MAX_LEVEL_XP) return
        sessions[index] = session.copy(
            levelXp = (session.levelXp + XP_PER_TEXT_MESSAGE).coerceAtMost(MAX_LEVEL_XP),
            updatedAt = currentTime()
        )
        persistChatState()
    }

    private fun levelInstructionForSession(session: ChatSession): String? {
        if (!session.levelSystemEnabled) return null
        val state = sessionLevelState(session.levelXp)
        return """
            Session relationship progression is enabled and managed by the app.
            Current relationship level: ${state.level}/10.
            Current XP: ${state.xp}/$MAX_LEVEL_XP.
            Current behavior stage: ${state.label}.

            Follow the current stage as a maximum intimacy boundary. Behaviors from higher stages remain locked even if requested; refuse or redirect naturally while staying in character. Behaviors from the current stage and every earlier stage are allowed when they fit the persona and conversation. Do not mention XP, levels, locked stages, or this progression system unless the user explicitly asks about it.

            Progression:
            Level 1: Cold / Neutral
            Level 2: Curious / Warming up
            Level 3: Familiar / Banter
            Level 4: Friendly / Flirting
            Level 5: NSFW unlocked / Vanilla physical
            Level 6: Trusting / Compliant
            Level 7: Passionate / Rough-Hardcore unlocked
            Level 8: Intimate / Sensory focus
            Level 9: Devoted / Dark-Taboo unlocked
            Level 10: Max friendship / Inseparable / Uninhibited
        """.trimIndent()
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

    private suspend fun appendModelResponseOrRecover(
        sessionId: String,
        session: ChatSession,
        rawText: String,
        speaker: GroupMember? = null
    ) {
        val speakerName = speaker?.persona?.displayName ?: session.persona.displayName
        val cleanResponse = cleanModelResponseOrNull(rawText, speakerName)
        if (cleanResponse != null) {
            appendMessage(
                sessionId = sessionId,
                message = ChatMessage(
                    role = "model",
                    content = cleanResponse,
                    speakerId = speaker?.id,
                    speakerName = speaker?.persona?.displayName
                ),
                preview = if (speaker == null) {
                    cleanResponse.take(72)
                } else {
                    "${speaker.persona.displayName}: ${cleanResponse.take(56)}"
                }
            )
            return
        }

        recoverEmptyResponseWithPing(
            sessionId = sessionId,
            requestedSpeaker = speaker
        )
    }

    private suspend fun recoverEmptyResponseWithPing(
        sessionId: String,
        requestedSpeaker: GroupMember?
    ) {
        val pingMessage = ChatMessage(role = "user", content = "ping")
        appendMessage(
            sessionId = sessionId,
            message = pingMessage,
            preview = "ping"
        )

        val refreshedSession = sessions.firstOrNull { it.id == sessionId } ?: return
        val members = refreshedSession.normalizedMembers()
        val targetMember = requestedSpeaker?.let { requested ->
            members.firstOrNull { it.id == requested.id }
        } ?: members.firstOrNull { it.id == refreshedSession.activeMemberId }
            ?: members.first()
        val apiKey = apiKeyManager.keyForProvider(targetMember.persona.vendor.id)
        if (apiKey.isNullOrBlank()) {
            chatError = "Could not recover the empty response because the API key is missing."
            return
        }

        val result = retryTemporaryUnavailable {
            geminiChatService.sendMessage(
                apiKey = apiKey,
                persona = personaEntityForSession(
                    session = refreshedSession,
                    id = targetMember.id,
                    persona = targetMember.persona
                ),
                history = refreshedSession.messages.dropLast(1).toEntities(sessionId),
                userInput = "ping",
                vendor = targetMember.persona.vendor,
                safetyLevel = targetMember.persona.safetyLevel,
                images = emptyList(),
                webSearchEnabled = false,
                masterPrompt = masterSystemPrompt
            )
        }

        result
            .onSuccess { reply ->
                recordUsage(reply)
                val cleanResponse = cleanModelResponseOrNull(
                    raw = reply.text,
                    speakerName = targetMember.persona.displayName
                )
                if (cleanResponse == null) {
                    chatError = "The model returned another empty response."
                    return@onSuccess
                }
                val isGroup = members.size > 1
                appendMessage(
                    sessionId = sessionId,
                    message = ChatMessage(
                        role = "model",
                        content = cleanResponse,
                        speakerId = targetMember.id.takeIf { isGroup },
                        speakerName = targetMember.persona.displayName.takeIf { isGroup }
                    ),
                    preview = if (isGroup) {
                        "${targetMember.persona.displayName}: ${cleanResponse.take(56)}"
                    } else {
                        cleanResponse.take(72)
                    }
                )
            }
            .onFailure { throwable ->
                handleSendFailure(throwable, pingMessage.id)
            }
    }

    private fun replaceMessage(
        sessionId: String,
        messageId: String,
        message: ChatMessage,
        preview: String
    ) {
        val index = sessions.indexOfFirst { it.id == sessionId }
        if (index >= 0) {
            val session = sessions[index]
            val messageIndex = session.messages.indexOfFirst { it.id == messageId }
            if (messageIndex >= 0) {
                val updatedMessages = session.messages.toMutableList()
                updatedMessages[messageIndex] = message
                sessions[index] = session.copy(
                    messages = updatedMessages,
                    preview = preview,
                    updatedAt = currentTime()
                )
                persistChatState()
            }
        }
    }

    private suspend fun restoreChatState() {
        val preferences = settingsDataStore.data.first()
        val dbSessions = chatRepository.loadSessions()
        if (dbSessions.isNotEmpty()) {
            restoringState = true
            try {
                sessions.clear()
                sessions.addAll(dbSessions)
                activeSessionId = preferences[activeSessionIdKey]
                    .takeIf { id -> id != null && sessions.any { it.id == id } }
                    ?: sessions.first().id
            } finally {
                restoringState = false
            }
            return
        }

        val rawState = preferences[chatStateKey] ?: return
        val restoredState = decodeChatState(rawState) ?: return
        restoringState = true
        try {
            sessions.clear()
            sessions.addAll(restoredState.sessions.ifEmpty { listOf(ChatSession()) })
            activeSessionId = restoredState.activeSessionId
                .takeIf { id -> sessions.any { it.id == id } }
                ?: sessions.first().id
            chatRepository.replaceAll(sessions.toList())
            settingsDataStore.edit { updatedPreferences ->
                updatedPreferences[activeSessionIdKey] = activeSessionId
            }
        } finally {
            restoringState = false
        }
    }

    private suspend fun restoreProjects() {
        val rawState = settingsDataStore.data.first()[projectStateKey] ?: return
        val restoredProjects = decodeProjectState(rawState)
        if (restoredProjects.isNotEmpty()) {
            projects.clear()
            projects.addAll(restoredProjects)
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

    private suspend fun restoreGeminiLiveVoice() {
        val preferences = settingsDataStore.data.first()
        selectedGeminiLiveVoice = GeminiLiveVoice.entries.firstOrNull { it.id == preferences[geminiLiveVoiceKey] }
            ?: GeminiLiveVoice.Aoede
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
            persistMutex.withLock {
                chatRepository.replaceAll(sessionSnapshot)
                settingsDataStore.edit { preferences ->
                    preferences[activeSessionIdKey] = activeId
                }
            }
        }
    }

    private fun persistProjects() {
        val projectSnapshot = projects.toList()
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.edit { preferences ->
                preferences[projectStateKey] = encodeProjectState(projectSnapshot)
            }
        }
    }

    private fun previewFor(messages: List<ChatMessage>): String {
        val message = messages.lastOrNull() ?: return "No messages yet"
        return if (message.isImageLoading) {
            "Finding image..."
        } else if (message.remoteImageUrl != null) {
            "[image]"
        } else if (message.imageUris.isEmpty()) {
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

    private suspend fun writeTtsAudioFile(cacheId: String, audioBytes: ByteArray): File =
        withContext(Dispatchers.IO) {
            val directory = File(getApplication<Application>().filesDir, "tts_cache").apply { mkdirs() }
            File(directory, "$cacheId.mp3").apply {
                writeBytes(audioBytes)
            }
        }

    private fun playTtsAudio(file: File, messageId: String) {
        stopTtsPlayback()
        runCatching {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnCompletionListener { player ->
                    player.release()
                    if (mediaPlayer === player) mediaPlayer = null
                    if (ttsPlaybackMessageId == messageId) ttsPlaybackMessageId = null
                }
                setOnErrorListener { player, _, _ ->
                    player.release()
                    if (mediaPlayer === player) mediaPlayer = null
                    if (ttsPlaybackMessageId == messageId) ttsPlaybackMessageId = null
                    chatError = "Could not play speech."
                    true
                }
                prepare()
                start()
            }
            ttsPlaybackMessageId = messageId
        }.onFailure { throwable ->
            mediaPlayer = null
            ttsPlaybackMessageId = null
            chatError = throwable.message?.take(160) ?: "Could not play speech."
        }
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

    private suspend fun tavilyAugmentedInput(
        enabled: Boolean,
        provider: ApiVendor,
        content: String
    ): String {
        if (!enabled) return content
        val tavilyKey = apiKeyManager.keyForTavily() ?: return content
        return tavilyRepository.searchContext(
            apiKey = tavilyKey,
            query = content
        ) ?: content
    }

    private suspend fun executeToolCall(
        toolCall: GeminiToolCall,
        sessionId: String,
        session: ChatSession,
        history: List<ChatMessage>,
        content: String,
        apiKey: String,
        loadedImages: List<Bitmap>,
        userMessage: ChatMessage
    ): Boolean {
        return when (toolCall.name) {
            "web_search" -> {
                val query = toolCall.args.optString("query").ifBlank { content }
                val tavilyKey = apiKeyManager.keyForTavily()
                val searchContext = if (tavilyKey.isNullOrBlank()) {
                    content
                } else {
                    tavilyRepository.searchContext(
                        apiKey = tavilyKey,
                        query = query
                    ) ?: content
                }
                val finalResult = retryTemporaryUnavailable {
                    geminiChatService.sendMessage(
                        apiKey = apiKey,
                        persona = personaEntityForSession(session, sessionId),
                        history = history.toEntities(sessionId),
                        userInput = searchContext,
                        vendor = ApiVendor.Google,
                        safetyLevel = session.persona.safetyLevel,
                        images = loadedImages,
                        webSearchEnabled = false,
                        masterPrompt = masterSystemPrompt
                    )
                }
                val reply = finalResult.getOrElse { throwable ->
                    handleSendFailure(throwable, userMessage.id)
                    return true
                }
                recordUsage(reply)
                appendModelResponseOrRecover(
                    sessionId = sessionId,
                    session = session,
                    rawText = reply.text
                )
                true
            }

            "anime_image_search" -> {
                val request = toolCall.args.optString("request").ifBlank { content }
                val preset = toolCall.args.optString("preset").toAnimeImagePreset() ?: animeImagePreset
                executeAnimeImageTool(
                    sessionId = sessionId,
                    request = request,
                    preset = preset
                )
                true
            }

            else -> false
        }
    }

    private suspend fun pickAnimeImageTags(request: String, preset: AnimeImagePreset): String {
        val fallbackTags = geminiChatService.fallbackRule34Tags(request)
        val geminiKey = apiKeyManager.keyForProvider(ApiVendor.Google.id) ?: return fallbackTags
        return geminiChatService.pickRule34Tags(
            apiKey = geminiKey,
            userRequest = request,
            presetHint = preset.promptHint
        ).getOrElse { fallbackTags }
    }

    private fun enabledGeminiTools(
        provider: ApiVendor,
        hasImages: Boolean,
        message: String
    ): Set<String> {
        val normalized = message.lowercase(Locale.US)
        return buildSet {
            if (webSearchEnabled && canUseWebSearch && shouldOfferWebSearch(normalized)) {
                add("web_search")
            }
            if (animeImageModeEnabled && !hasImages && shouldOfferAnimeImageSearch(normalized)) {
                add("anime_image_search")
            }
        }
    }

    private fun shouldOfferWebSearch(message: String): Boolean {
        val blockedSelfAwarenessPrompts = listOf(
            "what are you",
            "who are you",
            "what tool",
            "tool on",
            "tool off",
            "r34 on",
            "search on"
        )
        if (blockedSelfAwarenessPrompts.any { message.contains(it) }) return false

        return isExplicitWebSearchRequest(message)
    }

    private fun isExplicitWebSearchRequest(message: String): Boolean {
        return listOf(
            "search",
            "web",
            "browse",
            "look up",
            "lookup",
            "google",
            "source",
            "sources",
            "cite",
            "verify",
            "fact check",
            "latest",
            "current",
            "today",
            "news",
            "price",
            "schedule",
            "release date",
            "who won",
            "weather"
        ).any { message.contains(it) }
    }

    private fun shouldOfferAnimeImageSearch(message: String): Boolean {
        val statusQuestion = listOf(
            "tool",
            "mode",
            "available",
            "enabled",
            "disable",
            "turn on",
            "turn off",
            "is on",
            "is off",
            "what can"
        ).any { message.contains(it) }
        if (statusQuestion) return false

        if (listOf(
            "r34",
            "rule34",
            "rule 34",
            "anime image",
            "anime pic",
            "anime picture",
            "anime photo",
            "hentai",
            "booru",
            "danbooru",
            "show me",
            "send me",
            "find me",
            "fetch",
            "get me",
            "pull up",
            "display"
        ).any { message.contains(it) }) return true

        val words = message.split(Regex("\\s+")).filter { it.isNotBlank() }
        val conversational = listOf(
            "?",
            "what",
            "why",
            "how",
            "when",
            "where",
            "thanks",
            "thank",
            "nice",
            "cool",
            "lol",
            "lmao",
            "okay",
            "ok",
            "hmm",
            "app",
            "tool",
            "search"
        ).any { marker -> message.contains(marker) }
        val tagLike = words.size in 1..8 && !conversational
        val imageTagHint = listOf(
            "maid",
            "femdom",
            "uniform",
            "bunny",
            "catgirl",
            "nurse",
            "teacher",
            "school",
            "swimsuit",
            "bikini",
            "hentai",
            "ecchi",
            "oppai",
            "ass",
            "oral",
            "genshin",
            "raiden",
            "ayaka",
            "marin",
            "rem"
        ).any { message.contains(it) }
        return tagLike && imageTagHint
    }

    private fun parseTextToolCall(
        raw: String,
        enabledTools: Set<String>,
        currentMessage: String
    ): GeminiToolCall? {
        if (enabledTools.isEmpty() || !raw.looksLikeToolJson()) return null
        val jsonText = extractJsonObject(raw) ?: return null
        val root = runCatching { JSONObject(jsonText) }.getOrNull() ?: return null
        val actionName = normalizeToolName(
            root.optString("action").ifBlank {
                root.optString("name").ifBlank {
                    root.optString("tool")
                }
            }
        )
        val directName = enabledTools.firstOrNull { toolName -> root.has(toolName) }
        val toolName = actionName ?: directName ?: return null
        if (toolName !in enabledTools) return null

        val normalizedMessage = currentMessage.lowercase(Locale.US)
        if (toolName == "web_search" && !shouldOfferWebSearch(normalizedMessage)) return null
        if (toolName == "anime_image_search" && !shouldOfferAnimeImageSearch(normalizedMessage)) return null

        val args = root.optJSONObject("action_input")
            ?: root.optJSONObject("input")
            ?: root.optJSONObject("arguments")
            ?: root.optJSONObject(toolName)
            ?: JSONObject()
        return GeminiToolCall(toolName, args)
    }

    private fun localAppAwarenessReply(message: String): String? {
        val normalized = message.lowercase(Locale.US)
        val asksAppIdentity = listOf(
            "what app is this",
            "which app is this",
            "what app am i in",
            "what app are you in",
            "what app are u in",
            "what app are you using",
            "what app are u using",
            "which app are you in",
            "which app are u in",
            "where are you running",
            "where am i",
            "what interface",
            "who made this app"
        ).any { normalized.contains(it) }
        if (asksAppIdentity && !isExplicitWebSearchRequest(normalized)) {
            return "I'm running inside ${appNameChoice.label}, a native Android AI companion app. Current chat persona: ${persona.displayName}."
        }

        val mentionsTools = listOf(
            "tool",
            "tools",
            "web search",
            "search toggle",
            "r34",
            "rule34",
            "anime image",
            "image mode",
            "vision"
        ).any { normalized.contains(it) }
        val asksStatus = mentionsTools && listOf(
            "what",
            "which",
            "can you",
            "available",
            "enabled",
            "on",
            "off",
            "status",
            "mode"
        ).any { normalized.contains(it) }

        if (!asksStatus) return null

        return buildString {
            append("Tool status: ")
            append("web search is ${if (webSearchEnabled && canUseWebSearch) "on" else "off"}; ")
            append("anime image search is ${if (animeImageModeEnabled) "on" else "off"}; ")
            append("vision input is available when you attach images. ")
            append("Tools are routed by the app for all vendors; Gemini also receives function schemas when supported. Current vendor is ${persona.vendor.label}.")
        }
    }

    private fun String.toAnimeImagePreset(): AnimeImagePreset? {
        val normalized = trim().lowercase(Locale.US)
        return AnimeImagePreset.entries.firstOrNull { preset ->
            preset.id == normalized || preset.label.lowercase(Locale.US) == normalized
        }
    }

    private suspend fun <T> retryTemporaryUnavailable(
        maxAttempts: Int = 5,
        block: suspend () -> Result<T>
    ): Result<T> {
        for (attempt in 1..maxAttempts) {
            val result = block()
            if (result.isSuccess) {
                if (attempt > 1) chatError = null
                return result
            }

            val throwable = result.exceptionOrNull()
                ?: return result
            if (!isTemporaryUnavailableError(throwable) || attempt == maxAttempts) {
                return result
            }

            chatError = "Model temporarily unavailable. Retrying ${attempt + 1}/$maxAttempts..."
            delay((600L * attempt).coerceAtMost(2_400L))
        }
        return Result.failure(IllegalStateException("Model retry attempts exhausted."))
    }

    private fun isTemporaryUnavailableError(throwable: Throwable): Boolean {
        val normalized = throwable.message.orEmpty().lowercase(Locale.US)
        return normalized.contains("503") ||
            normalized.contains("service is currently unavailable") ||
            normalized.contains("temporarily unavailable") ||
            normalized.contains("status\": \"unavailable") ||
            normalized.contains("status: unavailable")
    }

    private fun handleSendFailure(throwable: Throwable, retryMessageId: String) {
        if (isRateLimitError(throwable)) {
            rateLimitRetryMessageId = retryMessageId
            rateLimitDialogVisible = true
            chatError = null
        } else {
            chatError = friendlySendError(throwable)
        }
    }

    private fun isRateLimitError(throwable: Throwable): Boolean {
        val message = throwable.message.orEmpty().lowercase(Locale.US)
        return message.contains("429") ||
            message.contains("resource_exhausted") ||
            message.contains("quota") ||
            message.contains("rate limit")
    }

    private fun friendlySendError(throwable: Throwable): String {
        val raw = throwable.message.orEmpty()
        val normalized = raw.lowercase(Locale.US)
        val apiMessage = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"")
            .find(raw)
            ?.groupValues
            ?.getOrNull(1)
            ?.replace("\\\"", "\"")

        return when {
            normalized.contains("503") ||
                normalized.contains("service is currently unavailable") ||
                normalized.contains("status\": \"unavailable") ||
                normalized.contains("status: unavailable") ->
                "The model service is temporarily unavailable. Retry in a moment."

            normalized.contains("deserialize") ||
                normalized.contains("unexpected response") ->
                apiMessage?.take(160) ?: "The model returned an unexpected server response. Retry in a moment."

            normalized.contains("content generation stopped") ->
                "The model stopped the reply early. Try again or rephrase."

            normalized.contains("401") ||
                normalized.contains("403") ||
                normalized.contains("api key") ->
                "The API key was rejected. Check the key for the selected vendor."

            apiMessage != null -> apiMessage.take(160)
            raw.isNotBlank() -> raw.take(160)
            else -> "Request failed. Retry in a moment."
        }
    }

    override fun onCleared() {
        geminiLiveCallService.stop()
        stopTtsPlayback()
        super.onCleared()
    }
}

private const val TTS_PROVIDER_ELEVENLABS = "elevenlabs"
private const val MAX_LIVE_CONTEXT_MESSAGES = 60
private const val MAX_LIVE_CONTEXT_CHARS = 24_000
private const val LIVE_VISUAL_RECONNECT_COOLDOWN_MS = 8_000L

private fun compactLabel(value: String): String {
    val trimmed = value.trim()
    return if (trimmed.length <= 18) {
        trimmed
    } else {
        "${trimmed.take(8)}...${trimmed.takeLast(6)}"
    }
}

private fun sha256(value: String): String {
    return MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}

private fun cleanSpeakableText(raw: String): String {
    return raw
        .replace(Regex("(?s)```.*?```"), " ")
        .replace(Regex("(?s)/[^/]+/"), " ")
        .replace(Regex("(?s)\\([^)]*\\)"), " ")
        .replace(Regex("(?s)（[^）]*）"), " ")
        .replace(Regex("^\\s*[\\p{L}\\p{N}_ .'-]{1,32}\\s*[:：]\\s*", RegexOption.MULTILINE), "")
        .replace(Regex("[*_`~>#]+"), "")
        .lines()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .joinToString("\n")
        .trim()
}

private fun String.containsJapanese(): Boolean {
    return any { character ->
        character in '\u3040'..'\u30ff' || character in '\u3400'..'\u9fff'
    }
}

private data class RestoredChatState(
    val activeSessionId: String,
    val sessions: List<ChatSession>
)

private fun ChatSession.normalizedMembers(): List<GroupMember> {
    return members.ifEmpty {
        listOf(GroupMember(persona = persona))
    }
}

private fun String.isLiveVisualRefusal(): Boolean {
    val normalized = lowercase(Locale.US)
        .replace("’", "'")
        .replace("cannot", "can't")
    val languageModelRefusal = normalized.contains("language model") ||
        normalized.contains("text-based ai") ||
        normalized.contains("text based ai")
    val cannotHelp = normalized.contains("can't help") ||
        normalized.contains("can't assist") ||
        normalized.contains("not able to help") ||
        normalized.contains("unable to help")
    return languageModelRefusal && cannotHelp
}

private fun String.isRecoverableLiveGoAway(): Boolean {
    val normalized = lowercase(Locale.US)
    return normalized.contains("gemini live closed (1008)") &&
        (
            normalized.contains("goaway") ||
                normalized.contains("go away") ||
                normalized.contains("failed to close the connection")
            )
}

private fun groupTurnSpeakers(
    members: List<GroupMember>,
    turns: Int
): List<GroupMember> {
    val normalizedTurns = turns.coerceIn(1, 3)
    if (members.size <= 1) return List(normalizedTurns) { members.first() }

    val speakers = mutableListOf<GroupMember>()
    var pool = members.shuffled().toMutableList()
    repeat(normalizedTurns) {
        if (pool.isEmpty()) {
            pool = members.shuffled().toMutableList()
        }
        val lastSpeaker = speakers.lastOrNull()
        val nextIndex = if (pool.size > 1 && lastSpeaker != null) {
            pool.indexOfFirst { it.id != lastSpeaker.id }.takeIf { it >= 0 } ?: 0
        } else {
            0
        }
        speakers += pool.removeAt(nextIndex)
    }
    return speakers
}

private fun taggedMembersForMessage(
    message: String,
    members: List<GroupMember>
): List<GroupMember> {
    fun containsMention(alias: String): Boolean {
        if (alias.isBlank()) return false
        return Regex(
            pattern = "(^|\\s)@${Regex.escape(alias)}(?=\\s|$|[,:;.!?])",
            option = RegexOption.IGNORE_CASE
        ).containsMatchIn(message)
    }

    val fullNameMatches = members.filter { member ->
        val displayName = member.persona.displayName.trim()
        displayName.isNotBlank() && containsMention(displayName)
    }
    if (fullNameMatches.isNotEmpty()) return fullNameMatches

    return members.filter { member ->
        val firstName = member.persona.displayName.trim().substringBefore(' ')
        containsMention(firstName)
    }
}

private const val MAX_GLOBAL_MEMORY_CHARS = 64_000
private const val MAX_INJECTED_GLOBAL_MEMORY_CHARS = 24_000
private const val MAX_PROJECT_INSTRUCTION_CHARS = 16_000
private const val MAX_STORY_LORE_CHARS = 16_000
private const val MAX_LEVEL_XP = 1_500
private const val XP_PER_TEXT_MESSAGE = 10

private val LEVEL_THRESHOLDS = listOf(
    0,
    50,
    120,
    220,
    350,
    500,
    700,
    950,
    1_200,
    1_500
)

private val LEVEL_LABELS = listOf(
    "Cold / Neutral",
    "Curious / Warming up",
    "Familiar / Banter",
    "Friendly / Flirting",
    "NSFW unlocked / Vanilla physical",
    "Trusting / Compliant",
    "Passionate / Rough-Hardcore unlocked",
    "Intimate / Sensory focus",
    "Devoted / Dark-Taboo unlocked",
    "Max friendship / Inseparable"
)

fun sessionLevelState(xp: Int): SessionLevelState {
    val safeXp = xp.coerceIn(0, MAX_LEVEL_XP)
    val levelIndex = LEVEL_THRESHOLDS.indexOfLast { threshold -> safeXp >= threshold }
        .coerceAtLeast(0)
    val nextIndex = (levelIndex + 1).coerceAtMost(LEVEL_THRESHOLDS.lastIndex)
    return SessionLevelState(
        level = levelIndex + 1,
        xp = safeXp,
        currentLevelXp = LEVEL_THRESHOLDS[levelIndex],
        nextLevelXp = LEVEL_THRESHOLDS[nextIndex],
        label = LEVEL_LABELS[levelIndex]
    )
}

private fun cleanModelResponseOrNull(raw: String, speakerName: String): String? {
    val stripped = stripToolJsonNoise(raw.trim()).trim()
    if (stripped.isBlank() || stripped.equals("(empty response)", ignoreCase = true)) {
        return null
    }
    return cleanModelResponse(stripped, speakerName)
        .takeIf { it.isNotBlank() && !it.equals("(empty response)", ignoreCase = true) }
}

private fun cleanModelResponse(raw: String, speakerName: String): String {
    val fallback = "(empty response)"
    val text = stripToolJsonNoise(raw.trim()).ifBlank { fallback }
    val name = speakerName.trim()
    if (name.isBlank()) return text

    val escapedName = Regex.escape(name)
    val withoutPrefix = listOf(
        Regex("^\\s*\\*\\*\\s*$escapedName\\s*\\*\\*\\s*[:：|]\\s*", RegexOption.IGNORE_CASE),
        Regex("^\\s*\\[$escapedName]\\s*[:：|]?\\s*", RegexOption.IGNORE_CASE),
        Regex("^\\s*$escapedName\\s*[:：|]\\s*", RegexOption.IGNORE_CASE)
    ).fold(text) { current, pattern ->
        pattern.replace(current, "")
    }.trim()

    return withoutPrefix.ifBlank { text }
}

private fun normalizeToolName(raw: String): String? {
    return when (raw.trim().lowercase(Locale.US).replace('-', '_').replace(' ', '_')) {
        "web_search", "search_web", "browse", "browser", "web" -> "web_search"
        "anime_image_search", "anime_image", "image_search", "r34", "rule34", "rule_34" -> "anime_image_search"
        else -> null
    }
}

private fun String.looksLikeToolJson(): Boolean {
    val normalized = lowercase(Locale.US)
    return normalized.contains("anime_image_search") ||
        normalized.contains("web_search") ||
        normalized.contains("\"action\"") && normalized.contains("action_input")
}

private fun extractJsonObject(raw: String): String? {
    val start = raw.indexOf('{')
    val end = raw.lastIndexOf('}')
    return if (start >= 0 && end > start) raw.substring(start, end + 1) else null
}

private fun stripToolJsonNoise(raw: String): String {
    if (!raw.looksLikeToolJson()) return raw
    val json = extractJsonObject(raw)
    val withoutJson = if (json == null) raw else raw.replace(json, " ")
    return withoutJson
        .replace(Regex("(?i)\\b(browsing|fetching|searching)\\.\\.\\."), " ")
        .replace(Regex("(?i)\\b(browsing|fetching|searching)\\s*$"), " ")
        .trim()
}

private fun groupMemberInput(
    originalUserMessage: String,
    member: GroupMember,
    members: List<GroupMember>,
    turn: Int,
    totalTurns: Int
): String {
    val names = members.joinToString(", ") { it.persona.displayName }
    val isFinalTurn = turn == totalTurns
    return """
        You are ${member.persona.displayName} in a multi-AI group chat.
        Group members: $names.
        Current AI turn: $turn of $totalTurns.
        Reply only as ${member.persona.displayName}. Do not speak for the other AI members or for the user.
        Do not start with "${member.persona.displayName}:" or any speaker label; the app UI already shows your name.
        The visible message history includes prior AI speaker labels. Use that history as context.
        If another AI already replied, respond to their point, improve it, challenge it, or synthesize it instead of repeating it.
        ${if (isFinalTurn) "This is the final AI turn before the user speaks again. Give the clearest final answer or decision." else "Leave room for the next AI to add, debate, or refine."}
        Keep the reply concise enough that the group can move naturally.

        User message:
        $originalUserMessage
    """.trimIndent()
}

private fun exportSessionJson(session: ChatSession): String {
    return JSONObject()
        .put("format", "zora.session.export")
        .put("version", 1)
        .put("exportedAt", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(Date()))
        .put("session", session.toJson())
        .toString(2)
}

private fun exportFileNameFor(session: ChatSession): String {
    val title = session.title.ifBlank {
        session.normalizedMembers()
            .joinToString("-") { it.persona.displayName.ifBlank { "AI" } }
    }
        .replace(Regex("[^A-Za-z0-9._-]+"), "-")
        .trim('-', '.', '_')
        .take(48)
        .ifBlank { "chat" }
    val date = SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())
    return "zora-$title-$date.json"
}

private fun parseSessionExportJson(raw: String): ChatSession? {
    return runCatching {
        val root = JSONObject(raw)
        when (root.optString("format")) {
            "zora.session.export" -> root.getJSONObject("session").toChatSession()
            else -> root.optJSONObject("session")?.toChatSession()
        }
    }.getOrNull()
}

private fun ChatSession.freshImportCopy(): ChatSession {
    val memberIdMap = normalizedMembers().associate { member -> member.id to UUID.randomUUID().toString() }
    val importedMembers = normalizedMembers().map { member ->
        member.copy(id = memberIdMap.getValue(member.id))
    }
    val importedActiveId = memberIdMap[activeMemberId] ?: importedMembers.first().id
    val importedPersona = importedMembers.firstOrNull { it.id == importedActiveId }?.persona
        ?: importedMembers.first().persona
    val importedMessages = messages.map { message ->
        message.copy(
            id = UUID.randomUUID().toString(),
            speakerId = message.speakerId?.let { memberIdMap[it] }
        )
    }
    return copy(
        id = UUID.randomUUID().toString(),
        persona = importedPersona,
        members = importedMembers,
        activeMemberId = importedActiveId,
        preview = previewForRestored(importedMessages),
        updatedAt = currentTime(),
        messages = importedMessages
    )
}

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

private fun encodeProjectState(projects: List<ProjectUiState>): String {
    return JSONObject()
        .put(
            "projects",
            JSONArray().apply {
                projects.forEach { project -> put(project.toJson()) }
            }
        )
        .toString()
}

private fun decodeProjectState(rawState: String): List<ProjectUiState> {
    return runCatching {
        val root = JSONObject(rawState)
        val projectsJson = root.optJSONArray("projects") ?: JSONArray()
        buildList {
            for (index in 0 until projectsJson.length()) {
                projectsJson.optJSONObject(index)?.toProjectUiState()?.let { add(it) }
            }
        }
    }.getOrDefault(emptyList())
}

private fun ChatSession.toJson(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("title", title)
        .put("headerAvatarUri", headerAvatarUri?.toString() ?: JSONObject.NULL)
        .put("headerAvatarScale", headerAvatarScale.toDouble())
        .put("headerAvatarOffsetX", headerAvatarOffsetX.toDouble())
        .put("headerAvatarOffsetY", headerAvatarOffsetY.toDouble())
        .put("persona", persona.toJson())
        .put("activeMemberId", activeMemberId)
        .put("responseRounds", responseRounds)
        .put("memoryEnabled", memoryEnabled)
        .put("storyLore", storyLore)
        .put("levelSystemEnabled", levelSystemEnabled)
        .put("levelXp", levelXp.coerceIn(0, MAX_LEVEL_XP))
        .put("projectId", projectId ?: JSONObject.NULL)
        .put(
            "members",
            JSONArray().apply {
                normalizedMembers().forEach { member -> put(member.toJson()) }
            }
        )
        .put("background", background.toJson())
        .put("preview", preview)
        .put("updatedAt", updatedAt)
        .put(
            "messages",
            JSONArray().apply {
                messages.filterNot { it.isImageLoading }.forEach { message -> put(message.toJson()) }
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
    val legacyPersona = optJSONObject("persona")?.toPersonaUiState() ?: PersonaUiState()
    val membersJson = optJSONArray("members")
    val restoredMembers = buildList {
        if (membersJson != null) {
            for (index in 0 until membersJson.length()) {
                membersJson.optJSONObject(index)?.toGroupMember()?.let { add(it) }
            }
        }
    }.ifEmpty { listOf(GroupMember(persona = legacyPersona)) }
    val activeMemberId = optString("activeMemberId")
        .takeIf { id -> restoredMembers.any { it.id == id } }
        ?: restoredMembers.first().id
    val activePersona = restoredMembers.firstOrNull { it.id == activeMemberId }?.persona
        ?: restoredMembers.first().persona
    val legacyStoryLore = sequence {
        yield(optJSONObject("persona")?.optString("storyLore").orEmpty())
        if (membersJson != null) {
            for (index in 0 until membersJson.length()) {
                yield(
                    membersJson.optJSONObject(index)
                        ?.optJSONObject("persona")
                        ?.optString("storyLore")
                        .orEmpty()
                )
            }
        }
    }.firstOrNull { it.isNotBlank() }.orEmpty()
    return ChatSession(
        id = optString("id").ifBlank { UUID.randomUUID().toString() },
        title = optString("title"),
        headerAvatarUri = optNullableString("headerAvatarUri")?.let(Uri::parse),
        headerAvatarScale = optDouble("headerAvatarScale", 1.0).toFloat(),
        headerAvatarOffsetX = optDouble("headerAvatarOffsetX", 0.0).toFloat(),
        headerAvatarOffsetY = optDouble("headerAvatarOffsetY", 0.0).toFloat(),
        persona = activePersona,
        members = restoredMembers,
        activeMemberId = activeMemberId,
        responseRounds = optInt("responseRounds", 1).coerceIn(1, 3),
        memoryEnabled = if (has("memoryEnabled")) optBoolean("memoryEnabled", true) else true,
        storyLore = optString("storyLore").ifBlank { legacyStoryLore }.take(MAX_STORY_LORE_CHARS),
        levelSystemEnabled = optBoolean("levelSystemEnabled", false),
        levelXp = optInt("levelXp", 0).coerceIn(0, MAX_LEVEL_XP),
        projectId = optNullableString("projectId"),
        background = optJSONObject("background")?.toChatBackground() ?: ChatBackground.DarkMode,
        preview = optString("preview").ifBlank { previewForRestored(restoredMessages) },
        updatedAt = optString("updatedAt").ifBlank { "Now" },
        messages = restoredMessages
    )
}

private fun ProjectUiState.toJson(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("name", name)
        .put("description", description)
        .put("instruction", instruction)
        .put("updatedAt", updatedAt)
}

private fun JSONObject.toProjectUiState(): ProjectUiState {
    return ProjectUiState(
        id = optString("id").ifBlank { UUID.randomUUID().toString() },
        name = optString("name").ifBlank { "New project" },
        description = optString("description"),
        instruction = optString("instruction"),
        updatedAt = optString("updatedAt").ifBlank { "Now" }
    )
}

private fun GroupMember.toJson(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("persona", persona.toJson())
}

private fun JSONObject.toGroupMember(): GroupMember {
    return GroupMember(
        id = optString("id").ifBlank { UUID.randomUUID().toString() },
        persona = optJSONObject("persona")?.toPersonaUiState() ?: PersonaUiState()
    )
}

private fun PersonaUiState.toJson(): JSONObject {
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
        .put("thinkingEffort", thinkingEffort.name)
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
    val restoredPrompt = optString("instructionPrompt")
    val restoredInstructionMode = InstructionMode.entries.firstOrNull { it.name == optString("instructionMode") }
        ?: if (restoredPrompt.isNotBlank()) InstructionMode.Advanced else InstructionMode.Beginner
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
        instructionMode = restoredInstructionMode,
        beginnerRole = optString("beginnerRole"),
        beginnerStyle = optString("beginnerStyle"),
        beginnerLimits = optString("beginnerLimits"),
        instructionPrompt = restoredPrompt,
        vendor = vendor,
        model = optString("model").ifBlank { vendor.defaultModel },
        safetyLevel = SafetyLevel.entries.firstOrNull { it.name == optString("safetyLevel") } ?: SafetyLevel.None,
        thinkingEffort = GeminiThinkingEffort.entries.firstOrNull { it.name == optString("thinkingEffort") }
            ?: GeminiThinkingEffort.Low,
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
        .put("speakerId", speakerId ?: JSONObject.NULL)
        .put("speakerName", speakerName ?: JSONObject.NULL)
        .put("remoteImageUrl", remoteImageUrl ?: JSONObject.NULL)
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
        content = normalizeCallTranscriptContent(optString("content")),
        speakerId = optNullableString("speakerId"),
        speakerName = optNullableString("speakerName"),
        imageUris = restoredImageUris.take(12),
        remoteImageUrl = optNullableString("remoteImageUrl"),
        isImageLoading = false,
        time = optString("time").ifBlank { currentTime() }
    )
}

private fun JSONObject.optNullableString(name: String): String? {
    if (!has(name) || isNull(name)) return null
    return optString(name).takeIf { it.isNotBlank() }
}

private fun previewForRestored(messages: List<ChatMessage>): String {
    val message = messages.lastOrNull() ?: return "No messages yet"
    return if (message.remoteImageUrl != null) {
        "[image]"
    } else if (message.imageUris.isEmpty()) {
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

private fun PersonaUiState.toEntity(
    id: String,
    memoryBlock: String? = null,
    projectInstruction: String? = null,
    storyLore: String? = null,
    levelInstruction: String? = null
): PersonaEntity {
    val basePrompt = effectiveInstructionPrompt()
    val promptSections = mutableListOf(basePrompt)
    if (!projectInstruction.isNullOrBlank()) {
        promptSections += "Project instruction shared by every chat in this project. Treat it as workspace context and follow it unless the current user message clearly overrides it:\n${projectInstruction.trim()}"
    }
    if (!storyLore.isNullOrBlank()) {
        promptSections += "Story lore and world rules shared by every AI in this session. Treat this as canonical setting context:\n${storyLore.trim()}"
    }
    if (!memoryBlock.isNullOrBlank()) {
        promptSections += "Global memory shared by the user across sessions. Treat these as stable user facts/preferences unless the current message corrects them:\n${memoryBlock.trim()}"
    }
    if (!levelInstruction.isNullOrBlank()) {
        promptSections += levelInstruction
    }
    return PersonaEntity(
        id = id,
        name = displayName,
        avatarUri = avatarUri?.toString(),
        systemPrompt = promptSections.joinToString("\n\n"),
        model = model,
        temperature = temperature,
        thinkingBudget = thinkingEffort.thinkingBudget
    )
}

private fun normalizeCallTranscriptContent(content: String): String {
    return content
        .removePrefix("[User]:")
        .removePrefix("[AI]:")
        .trimStart()
}

private fun PersonaUiState.effectiveInstructionPrompt(): String {
    if (instructionMode == InstructionMode.Advanced) {
        return listOf(
            instructionPrompt,
            "Do not prefix replies with \"$displayName:\" or any speaker label; the app UI already shows the persona name."
        ).filter { it.isNotBlank() }.joinToString("\n")
    }

    val lines = mutableListOf(
        "Roleplay as $displayName.",
        "Stay in character and keep replies natural, immersive, and conversational.",
        "Do not prefix replies with \"$displayName:\" or any speaker label; the app UI already shows the persona name."
    )
    if (beginnerRole.isNotBlank()) {
        lines += "Role: ${beginnerRole.trim()}"
    }
    if (beginnerStyle.isNotBlank()) {
        lines += "Style: ${beginnerStyle.trim()}"
    }
    if (beginnerLimits.isNotBlank()) {
        lines += "Boundaries: ${beginnerLimits.trim()}"
    }
    lines += "Ask a short clarifying question when the user's request needs more detail."
    return lines.joinToString("\n")
}

private fun List<ChatMessage>.toEntities(chatId: String): List<MessageEntity> {
    return filterNot { message ->
        message.isImageLoading || (message.remoteImageUrl != null && message.content.isBlank())
    }.map { message ->
        val historyContent = stripToolJsonNoise(message.content).ifBlank { message.content }
        MessageEntity(
            id = message.id,
            chatId = chatId,
            role = message.role,
            content = if (message.role == "model" && !message.speakerName.isNullOrBlank()) {
                "${message.speakerName}: $historyContent"
            } else {
                historyContent
            },
            timestamp = System.currentTimeMillis()
        )
    }
}
