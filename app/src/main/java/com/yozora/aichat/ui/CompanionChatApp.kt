package com.yozora.aichat.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.projection.MediaProjectionConfig
import android.media.projection.MediaProjectionManager
import android.os.Build
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.ImageSearch
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.ScreenShare
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.yozora.aichat.R
import com.yozora.aichat.ui.chat.AnimeImagePreset
import com.yozora.aichat.ui.chat.AppIconChoice
import com.yozora.aichat.ui.chat.AppNameChoice
import com.yozora.aichat.ui.chat.ApiVendor
import com.yozora.aichat.ui.chat.ChatBackground
import com.yozora.aichat.ui.chat.ChatMessage
import com.yozora.aichat.ui.chat.ChatSession
import com.yozora.aichat.ui.chat.ChatViewModel
import com.yozora.aichat.ui.chat.GeminiLiveVoice
import com.yozora.aichat.ui.chat.GeminiThinkingEffort
import com.yozora.aichat.ui.chat.GroupMember
import com.yozora.aichat.ui.chat.InstructionMode
import com.yozora.aichat.ui.chat.LiveCallTranscriptLine
import com.yozora.aichat.ui.chat.PersonaUiState
import com.yozora.aichat.ui.chat.ProjectUiState
import com.yozora.aichat.ui.chat.QuotaUsageState
import com.yozora.aichat.ui.chat.SafetyLevel
import com.yozora.aichat.ui.chat.sessionLevelState
import com.yozora.aichat.ui.chat.TtsPreviewState
import com.yozora.aichat.ui.chat.VoiceCallCameraFacing
import com.yozora.aichat.ui.theme.AppAccent
import com.yozora.aichat.ui.theme.AppAccentDim
import com.yozora.aichat.ui.theme.AppAccentSoft
import com.yozora.aichat.ui.theme.AppBackground
import com.yozora.aichat.ui.theme.AppStroke
import com.yozora.aichat.ui.theme.AppSurface
import com.yozora.aichat.ui.theme.AppSurface2
import com.yozora.aichat.ui.theme.AppTextPrimary
import com.yozora.aichat.ui.theme.AppTextMuted
import com.yozora.aichat.ui.theme.AppTextSecondary
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import java.io.File
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

private const val APP_VERSION_NAME = "2.1.0"

private enum class DrawerSection {
    Chats,
    Projects
}

private enum class PersonaSettingsSection(val label: String) {
    Context("Context"),
    Ui("UI"),
    ApiVault("API vault")
}

@Composable
fun CompanionChatApp(
    viewModel: ChatViewModel = viewModel()
) {
    var viewedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var actionMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var aboutDialogVisible by remember { mutableStateOf(false) }
    val appVersionLabel = "${viewModel.appNameChoice.label} v$APP_VERSION_NAME"
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val voicePermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.openVoiceCall()
        } else {
            viewModel.handleVoicePermissionDenied()
        }
    }
    val cameraPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.enableVoiceCallVideo()
        }
    }
    val screenShareLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            viewModel.startVoiceCallScreenShare(result.resultCode, data)
        }
    }
    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importSessionFromUri(uri)
        }
    }

    if (viewModel.voiceCallActive) {
        BackHandler(onBack = viewModel::closeVoiceCall)
        VoiceCallScreen(
            persona = viewModel.persona,
            isOnline = viewModel.activeApiKeyLabel != null,
            status = viewModel.voiceCallStatus,
            error = viewModel.voiceCallError,
            modelId = viewModel.geminiLiveModelId,
            selectedVoice = viewModel.selectedGeminiLiveVoice,
            muted = viewModel.voiceCallMuted,
            videoEnabled = viewModel.voiceCallVideoEnabled,
            cameraFacing = viewModel.voiceCallCameraFacing,
            screenShareEnabled = viewModel.voiceCallScreenShareEnabled,
            transcriptLines = viewModel.voiceCallTranscriptLines,
            onCameraFrame = viewModel::sendVoiceCallVideoFrame,
            onBack = viewModel::closeVoiceCall,
            onEndCall = viewModel::closeVoiceCall,
            onRetry = viewModel::retryVoiceCall,
            onVoiceChange = viewModel::updateGeminiLiveVoice,
            onToggleMute = viewModel::toggleVoiceCallMute,
            onToggleVideo = {
                if (viewModel.voiceCallVideoEnabled) {
                    viewModel.disableVoiceCallVideo()
                } else {
                    val granted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (granted) {
                        viewModel.enableVoiceCallVideo()
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            },
            onSwitchCamera = viewModel::switchVoiceCallCamera,
            onToggleScreenShare = {
                if (viewModel.voiceCallScreenShareEnabled) {
                    viewModel.stopVoiceCallScreenShare()
                } else {
                    val manager = context.getSystemService(MediaProjectionManager::class.java)
                    val captureIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        manager.createScreenCaptureIntent(
                            MediaProjectionConfig.createConfigForDefaultDisplay()
                        )
                    } else {
                        manager.createScreenCaptureIntent()
                    }
                    screenShareLauncher.launch(captureIntent)
                }
            }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        ChatScreen(
            persona = viewModel.persona,
            groupMembers = viewModel.groupMembers,
            sessionHeaderName = viewModel.sessionHeaderName,
            sessionHeaderAvatarUri = viewModel.sessionHeaderAvatarUri,
            sessionHeaderAvatarScale = viewModel.sessionHeaderAvatarScale,
            sessionHeaderAvatarOffsetX = viewModel.sessionHeaderAvatarOffsetX,
            sessionHeaderAvatarOffsetY = viewModel.sessionHeaderAvatarOffsetY,
            background = viewModel.background,
            messages = viewModel.messages,
            draft = viewModel.draft,
            isOnline = viewModel.activeApiKeyLabel != null,
            isSending = viewModel.isSending,
            chatError = viewModel.chatError,
            attachedImageUris = viewModel.attachedImageUris,
            webSearchEnabled = viewModel.webSearchEnabled,
            canUseWebSearch = viewModel.canUseWebSearch,
            animeImageModeEnabled = viewModel.animeImageModeEnabled,
            animeImagePreset = viewModel.animeImagePreset,
            onDraftChange = viewModel::updateDraft,
            onSend = viewModel::sendDraft,
            onAttachImages = viewModel::attachImages,
            onRemoveAttachedImage = viewModel::removeAttachedImage,
            onWebSearchChange = viewModel::updateWebSearchEnabled,
            onAnimeImageModeChange = viewModel::updateAnimeImageModeEnabled,
            onAnimeImagePresetChange = viewModel::updateAnimeImagePreset,
            onOpenImage = { viewedImageUri = it },
            onMessageLongPress = { actionMessage = it },
            onOpenSessions = viewModel::openSessionDrawer,
            onOpenPersona = viewModel::openPersonaSheet,
            onOpenVoiceCall = {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    viewModel.openVoiceCall()
                } else {
                    voicePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        )

        AnimatedVisibility(
            visible = viewModel.sessionDrawerVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.56f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = viewModel::closeSessionDrawer
                    )
            )
        }

        AnimatedVisibility(
            visible = viewModel.sessionDrawerVisible,
            enter = slideInHorizontally(initialOffsetX = { -it }),
            exit = slideOutHorizontally(targetOffsetX = { -it }),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            SessionDrawer(
                sessions = viewModel.sessions,
                projects = viewModel.projects,
                activeSessionId = viewModel.activeSessionId,
                activeProject = viewModel.activeProject,
                persona = viewModel.persona,
                appVersionLabel = appVersionLabel,
                onNewSession = { viewModel.createSession() },
                onNewProject = viewModel::createProject,
                onSelectSession = viewModel::selectSession,
                onNewSessionInProject = viewModel::createSessionInProject,
                onMoveSessionToProject = viewModel::moveSessionToProject,
                onUpdateProjectName = viewModel::updateProjectName,
                onUpdateProjectDescription = viewModel::updateProjectDescription,
                onUpdateProjectInstruction = viewModel::updateProjectInstruction,
                onDeleteSession = viewModel::deleteSession,
                onRenameSession = viewModel::renameSession,
                onCloneSession = viewModel::cloneSession,
                onDuplicateSessionSettings = viewModel::duplicateSessionSettings,
                onImportSession = { importLauncher.launch(arrayOf("application/json", "text/*")) },
                onOpenSettings = viewModel::openAppSettings,
                onOpenAbout = { aboutDialogVisible = true },
                onClose = viewModel::closeSessionDrawer
            )
        }

        AnimatedVisibility(
            visible = viewModel.personaSheetVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.58f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = viewModel::closePersonaSheet
                    )
            )
        }

        AnimatedVisibility(
            visible = viewModel.personaSheetVisible,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            PersonaSettingsSheet(
                persona = viewModel.persona,
                groupMembers = viewModel.groupMembers,
                showSessionHeaderControls = viewModel.showSessionHeaderControls,
                sessionHeaderName = viewModel.sessionHeaderName,
                sessionHeaderAvatarUri = viewModel.sessionHeaderAvatarUri,
                sessionHeaderAvatarScale = viewModel.sessionHeaderAvatarScale,
                sessionHeaderAvatarOffsetX = viewModel.sessionHeaderAvatarOffsetX,
                sessionHeaderAvatarOffsetY = viewModel.sessionHeaderAvatarOffsetY,
                activeMemberId = viewModel.activeMemberId,
                responseRounds = viewModel.responseRounds,
                memoryEnabled = viewModel.memoryEnabled,
                storyLore = viewModel.storyLore,
                levelSystemEnabled = viewModel.levelSystemEnabled,
                levelXp = viewModel.levelXp,
                background = viewModel.background,
                moreOptions = viewModel.morePersonaOptions,
                activeApiKeyLabel = viewModel.activeApiKeyLabel,
                tavilyApiKeyLabel = viewModel.tavilyApiKeyLabel,
                rule34UserIdLabel = viewModel.rule34UserIdLabel,
                rule34ApiKeyLabel = viewModel.rule34ApiKeyLabel,
                elevenLabsApiKeyLabel = viewModel.elevenLabsApiKeyLabel,
                elevenLabsVoiceIdLabel = viewModel.elevenLabsVoiceIdLabel,
                elevenLabsModelIdLabel = viewModel.elevenLabsModelIdLabel,
                quotaUsage = viewModel.quotaUsage,
                dailyRequestLimit = viewModel.dailyRequestLimit,
                onBack = viewModel::closePersonaSheet,
                onVendorChange = viewModel::updateVendor,
                onSelectMember = viewModel::selectGroupMember,
                onAddMember = viewModel::addGroupMember,
                onRemoveMember = viewModel::removeGroupMember,
                onResponseRoundsChange = viewModel::updateResponseRounds,
                onMemoryEnabledChange = viewModel::updateMemoryEnabled,
                onLevelSystemEnabledChange = viewModel::updateLevelSystemEnabled,
                onNameChange = viewModel::updatePersonaName,
                onSessionHeaderNameChange = viewModel::updateSessionHeaderName,
                onInstructionModeChange = viewModel::updateInstructionMode,
                onBeginnerRoleChange = viewModel::updateBeginnerRole,
                onBeginnerStyleChange = viewModel::updateBeginnerStyle,
                onBeginnerLimitsChange = viewModel::updateBeginnerLimits,
                onStoryLoreChange = viewModel::updateStoryLore,
                onPromptChange = viewModel::updatePersonaPrompt,
                onModelChange = viewModel::updatePersonaModel,
                onSafetyLevelChange = viewModel::updateSafetyLevel,
                onThinkingEffortChange = viewModel::updateThinkingEffort,
                onTemperatureChange = viewModel::updateTemperature,
                onAvatarChange = viewModel::updateAvatar,
                onAvatarTransform = viewModel::transformAvatar,
                onSessionHeaderAvatarChange = viewModel::updateSessionHeaderAvatar,
                onSessionHeaderAvatarTransform = viewModel::transformSessionHeaderAvatar,
                onBackgroundChange = viewModel::updateBackground,
                onCustomBackgroundChange = viewModel::updateCustomBackground,
                onToggleMore = viewModel::toggleMorePersonaOptions,
                onEditApiKey = viewModel::openApiKeyDialog,
                onClearApiKey = viewModel::clearApiKey,
                onEditTavilyKey = viewModel::openTavilyApiKeyDialog,
                onClearTavilyKey = viewModel::clearTavilyApiKey,
                onEditRule34UserId = viewModel::openRule34UserIdDialog,
                onClearRule34UserId = viewModel::clearRule34UserId,
                onEditRule34ApiKey = viewModel::openRule34ApiKeyDialog,
                onClearRule34ApiKey = viewModel::clearRule34ApiKey,
                onEditElevenLabsKey = viewModel::openElevenLabsApiKeyDialog,
                onClearElevenLabsKey = viewModel::clearElevenLabsApiKey,
                onEditElevenLabsVoiceId = viewModel::openElevenLabsVoiceIdDialog,
                onClearElevenLabsVoiceId = viewModel::clearElevenLabsVoiceId,
                onEditElevenLabsModelId = viewModel::openElevenLabsModelIdDialog,
                onClearElevenLabsModelId = viewModel::clearElevenLabsModelId,
                onDeleteSession = viewModel::deleteActiveSession,
                onSave = viewModel::savePersona
            )
        }

    }

    if (viewModel.apiKeyDialogVisible) {
        ApiKeyDialog(
            title = viewModel.apiKeyDialogTitle,
            value = viewModel.apiKeyDraft,
            onValueChange = viewModel::updateApiKeyDraft,
            onDismiss = viewModel::closeApiKeyDialog,
            onSave = viewModel::saveApiKey,
            secureInput = viewModel.apiKeyDialogSecure
        )
    }

    if (viewModel.appSettingsVisible) {
        AppSettingsDialog(
            selectedName = viewModel.appNameChoice,
            selectedIcon = viewModel.appIconChoice,
            globalMemoryBlock = viewModel.globalMemoryBlock,
            onNameChange = viewModel::updateAppName,
            onIconChange = viewModel::updateAppIcon,
            onGlobalMemoryChange = viewModel::updateGlobalMemoryBlock,
            onDismiss = viewModel::closeAppSettings
        )
    }

    viewedImageUri?.let { uri ->
        FullscreenImageViewer(
            uri = uri,
            onDismiss = { viewedImageUri = null }
        )
    }

    actionMessage?.let { message ->
        MessageActionSheet(
            message = message,
            onDismiss = { actionMessage = null },
            onCopy = {
                val copyText = message.content.ifBlank {
                    message.remoteImageUrl ?: message.imageUris.joinToString("\n") { it.toString() }
                }
                clipboardManager.setText(AnnotatedString(copyText))
                actionMessage = null
            },
            onEdit = {
                viewModel.beginEditMessage(message.id)
                actionMessage = null
            },
            onRetry = {
                viewModel.retryMessage(message.id)
                actionMessage = null
            },
            onSpeak = {
                viewModel.speakMessage(message)
                actionMessage = null
            }
        )
    }

    viewModel.ttsPreviewState?.let { preview ->
        SpeechPreviewSheet(
            state = preview,
            onTextChange = viewModel::updateTtsPreviewText,
            onDismiss = viewModel::dismissTtsPreview,
            onGenerate = viewModel::generateAndPlayTtsPreview
        )
    }

    if (aboutDialogVisible) {
        AboutAppDialog(
            appVersionLabel = appVersionLabel,
            onDismiss = { aboutDialogVisible = false }
        )
    }

    if (viewModel.rateLimitDialogVisible) {
        RateLimitRetryDialog(
            onDismiss = viewModel::dismissRateLimitDialog,
            onRetry = viewModel::retryAfterRateLimit
        )
    }

    when {
        viewModel.appSettingsVisible -> BackHandler(onBack = viewModel::closeAppSettings)
        aboutDialogVisible -> BackHandler { aboutDialogVisible = false }
        viewModel.rateLimitDialogVisible -> BackHandler(onBack = viewModel::dismissRateLimitDialog)
        viewModel.ttsPreviewState != null -> BackHandler(onBack = viewModel::dismissTtsPreview)
        actionMessage != null -> BackHandler { actionMessage = null }
        viewModel.personaSheetVisible -> BackHandler(onBack = viewModel::closePersonaSheet)
        viewModel.sessionDrawerVisible -> BackHandler(onBack = viewModel::closeSessionDrawer)
    }
}

@Composable
private fun ChatScreen(
    persona: PersonaUiState,
    groupMembers: List<GroupMember>,
    sessionHeaderName: String,
    sessionHeaderAvatarUri: android.net.Uri?,
    sessionHeaderAvatarScale: Float,
    sessionHeaderAvatarOffsetX: Float,
    sessionHeaderAvatarOffsetY: Float,
    background: ChatBackground,
    messages: List<ChatMessage>,
    draft: String,
    isOnline: Boolean,
    isSending: Boolean,
    chatError: String?,
    attachedImageUris: List<android.net.Uri>,
    webSearchEnabled: Boolean,
    canUseWebSearch: Boolean,
    animeImageModeEnabled: Boolean,
    animeImagePreset: AnimeImagePreset,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachImages: (List<android.net.Uri>) -> Unit,
    onRemoveAttachedImage: (android.net.Uri) -> Unit,
    onWebSearchChange: (Boolean) -> Unit,
    onAnimeImageModeChange: (Boolean) -> Unit,
    onAnimeImagePresetChange: (AnimeImagePreset) -> Unit,
    onOpenImage: (android.net.Uri) -> Unit,
    onMessageLongPress: (ChatMessage) -> Unit,
    onOpenSessions: () -> Unit,
    onOpenPersona: () -> Unit,
    onOpenVoiceCall: () -> Unit
) {
    var toolsSheetVisible by remember { mutableStateOf(false) }
    var cameraOutputUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val photoPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetMultipleContents(),
        onResult = onAttachImages
    )
    val filePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenMultipleDocuments(),
        onResult = onAttachImages
    )
    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicture(),
        onResult = { saved ->
            if (saved) {
                cameraOutputUri?.let { onAttachImages(listOf(it)) }
            }
            cameraOutputUri = null
        }
    )
    val hasChatContent = messages.isNotEmpty() || isSending
    val mentionTokenStart = draft.indexOfLast { it.isWhitespace() } + 1
    val mentionToken = draft.substring(mentionTokenStart)
    val mentionQuery = mentionToken.takeIf { it.startsWith("@") }?.drop(1).orEmpty()
    val mentionCandidates = if (groupMembers.size > 1 && mentionToken.startsWith("@")) {
        groupMembers.filter { member ->
            member.persona.displayName.contains(mentionQuery, ignoreCase = true)
        }
    } else {
        emptyList()
    }
    val showJumpToLatest by remember {
        derivedStateOf {
            hasChatContent &&
                (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 24)
        }
    }
    fun displayIndexForMessage(originalIndex: Int): Int {
        val typingOffset = if (isSending) 1 else 0
        return typingOffset + (messages.lastIndex - originalIndex).coerceAtLeast(0)
    }
    val topAnchorIndex = if (messages.isNotEmpty()) displayIndexForMessage(0) else 0
    val bottomAnchorIndex = 0
    suspend fun scrollDisplayIndexToTop(index: Int) {
        val totalItems = listState.layoutInfo.totalItemsCount
        if (totalItems <= 0) return
        val safeIndex = index.coerceIn(0, totalItems - 1)
        if (abs(listState.firstVisibleItemIndex - safeIndex) > 12) {
            listState.scrollToItem(safeIndex)
        } else {
            listState.animateScrollToItem(safeIndex)
        }
        yield()
        var itemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == safeIndex }
        if (itemInfo == null) {
            listState.scrollToItem(safeIndex)
            yield()
            itemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == safeIndex }
        }
        itemInfo ?: return
        val deltaToTop = itemInfo.offset - listState.layoutInfo.viewportStartOffset
        if (abs(deltaToTop) > 1) {
            listState.scrollBy(deltaToTop.toFloat())
        }
    }
    suspend fun scrollToNewest() {
        if (!hasChatContent) return
        if (listState.firstVisibleItemIndex > 10) {
            listState.scrollToItem(bottomAnchorIndex)
        } else {
            listState.animateScrollToItem(bottomAnchorIndex)
        }
    }
    suspend fun scrollToOldest() {
        if (!hasChatContent) return
        scrollDisplayIndexToTop(topAnchorIndex)
    }

    LaunchedEffect(messages.lastOrNull()?.id, isSending) {
        if (hasChatContent) {
            scrollToNewest()
        }
    }
    val startedTypewriterMessageIds = remember { mutableSetOf<String>() }
    val newestTextModelMessageId = messages.lastOrNull { message ->
        message.role == "model" &&
            message.content.isNotBlank() &&
            !message.isImageLoading &&
            message.remoteImageUrl == null
    }?.id

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        ChatBackgroundLayer(background = background)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            ChatHeader(
                persona = persona,
                groupMembers = groupMembers,
                sessionHeaderName = sessionHeaderName,
                sessionHeaderAvatarUri = sessionHeaderAvatarUri,
                sessionHeaderAvatarScale = sessionHeaderAvatarScale,
                sessionHeaderAvatarOffsetX = sessionHeaderAvatarOffsetX,
                sessionHeaderAvatarOffsetY = sessionHeaderAvatarOffsetY,
                isOnline = isOnline,
                onOpenSessions = onOpenSessions,
                onOpenPersona = onOpenPersona,
                onOpenVoiceCall = onOpenVoiceCall
            )
            LazyColumn(
                state = listState,
                reverseLayout = messages.isNotEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        EmptyChatState(
                            persona = persona,
                            onCreatePersona = onOpenPersona,
                            onSuggestion = onDraftChange
                        )
                    }
                } else {
                    if (isSending) {
                        item(key = "typing") {
                            TypingBubble(persona = persona)
                        }
                    }
                    items(messages.asReversed(), key = { it.id }) { message ->
                        AnimatedMessageBubble(
                            message = message,
                            persona = persona,
                            groupMembers = groupMembers,
                            animateText = message.id == newestTextModelMessageId &&
                                message.id !in startedTypewriterMessageIds,
                            onTextAnimationStart = { startedTypewriterMessageIds += it },
                            onOpenImage = onOpenImage,
                            onMessageLongPress = onMessageLongPress
                        )
                    }
                }
            }
            if (chatError != null) {
                ChatErrorLine(message = chatError)
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(bottom = 10.dp)
            ) {
                AnimatedVisibility(
                    visible = mentionCandidates.isNotEmpty(),
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut()
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(mentionCandidates, key = { it.id }) { member ->
                            Surface(
                                color = AppSurface2,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, AppStroke),
                                modifier = Modifier.clickable {
                                    val prefix = draft.substring(0, mentionTokenStart)
                                    onDraftChange("$prefix@${member.persona.displayName} ")
                                }
                            ) {
                                Text(
                                    text = "@${member.persona.displayName}",
                                    color = AppAccentSoft,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
                ChatInputBar(
                    placeholder = "Message ${persona.displayName.ifBlank { "New Persona" }}...",
                    draft = draft,
                    isSending = isSending,
                    attachedImageUris = attachedImageUris,
                    onDraftChange = onDraftChange,
                    onSend = {
                        focusManager.clearFocus()
                        onSend()
                    },
                    onOpenTools = {
                        focusManager.clearFocus(force = true)
                        toolsSheetVisible = true
                    },
                    onRemoveAttachedImage = onRemoveAttachedImage,
                    onOpenImage = onOpenImage
                )
            }
        }
        AnimatedVisibility(
            visible = showJumpToLatest,
            enter = fadeIn() + scaleIn(initialScale = 0.88f),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .imePadding()
                .navigationBarsPadding()
                .padding(end = 22.dp, bottom = 104.dp)
        ) {
            Surface(
                color = AppAccent,
                shape = CircleShape,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(46.dp)
                    .clickable {
                        coroutineScope.launch {
                            if (hasChatContent) {
                                scrollToNewest()
                            }
                        }
                    }
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Jump to latest message",
                    tint = Color.White,
                    modifier = Modifier.padding(9.dp)
                )
            }
        }
    }

    AnimatedVisibility(
        visible = toolsSheetVisible,
        enter = fadeIn(animationSpec = tween(150)) + slideInVertically(
            animationSpec = tween(220),
            initialOffsetY = { it / 4 }
        ),
        exit = fadeOut(animationSpec = tween(120)) + slideOutVertically(
            animationSpec = tween(180),
            targetOffsetY = { it / 4 }
        )
    ) {
        ChatToolsSheet(
            onDismiss = { toolsSheetVisible = false },
            onScrollTop = {
                toolsSheetVisible = false
                coroutineScope.launch {
                    delay(190)
                    scrollToOldest()
                }
            },
            onScrollBottom = {
                toolsSheetVisible = false
                coroutineScope.launch {
                    delay(190)
                    scrollToNewest()
                }
            },
            onCamera = {
                val uri = createCameraImageUri(context)
                cameraOutputUri = uri
                toolsSheetVisible = false
                cameraLauncher.launch(uri)
            },
            onPhotos = {
                toolsSheetVisible = false
                photoPicker.launch("image/*")
            },
            onFiles = {
                toolsSheetVisible = false
                filePicker.launch(arrayOf("image/*"))
            },
            messages = messages,
            onJumpToMessage = { index ->
                toolsSheetVisible = false
                coroutineScope.launch {
                    delay(190)
                    listState.animateScrollToItem(displayIndexForMessage(index))
                }
            },
            webSearchEnabled = webSearchEnabled,
            webSearchAvailable = canUseWebSearch,
            onWebSearchChange = onWebSearchChange,
            animeImageModeEnabled = animeImageModeEnabled,
            animeImagePreset = animeImagePreset,
            onAnimeImageModeChange = onAnimeImageModeChange,
            onAnimeImagePresetChange = onAnimeImagePresetChange
        )
    }

    if (toolsSheetVisible) {
        BackHandler { toolsSheetVisible = false }
    }
}

@Composable
private fun VoiceCallScreen(
    persona: PersonaUiState,
    isOnline: Boolean,
    status: String,
    error: String?,
    modelId: String,
    selectedVoice: GeminiLiveVoice,
    muted: Boolean,
    videoEnabled: Boolean,
    cameraFacing: VoiceCallCameraFacing,
    screenShareEnabled: Boolean,
    transcriptLines: List<LiveCallTranscriptLine>,
    onCameraFrame: (ByteArray) -> Unit,
    onBack: () -> Unit,
    onEndCall: () -> Unit,
    onRetry: () -> Unit,
    onVoiceChange: (GeminiLiveVoice) -> Unit,
    onToggleMute: () -> Unit,
    onToggleVideo: () -> Unit,
    onSwitchCamera: () -> Unit,
    onToggleScreenShare: () -> Unit
) {
    var voiceMenuExpanded by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableStateOf(0L) }
    val startedAt = remember { System.currentTimeMillis() }
    val compactLayout = LocalConfiguration.current.screenHeightDp < 760
    val avatarOuterSize = if (compactLayout) 112 else 148
    val avatarImageSize = if (compactLayout) 92 else 122

    LaunchedEffect(startedAt) {
        while (true) {
            elapsedSeconds = (System.currentTimeMillis() - startedAt) / 1000L
            delay(1000L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .background(
                Brush.radialGradient(
                    colors = listOf(AppAccent.copy(alpha = 0.24f), AppBackground, Color.Black),
                    radius = 980f
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back to chat",
                        tint = AppTextPrimary
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = persona.displayName.ifBlank { "New Persona" },
                        color = AppTextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) Color(0xFF35D07F) else Color(0xFF8B8898))
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = "AI Voice Call",
                            color = AppTextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Box {
                    IconButton(onClick = { voiceMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "Voice settings",
                            tint = AppTextPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = voiceMenuExpanded,
                        onDismissRequest = { voiceMenuExpanded = false },
                        modifier = Modifier.background(AppSurface2)
                    ) {
                        GeminiLiveVoice.entries.forEach { voice ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = voice.label,
                                            color = AppTextPrimary,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                        Text(
                                            text = voice.description,
                                            color = AppTextSecondary,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
                                onClick = {
                                    onVoiceChange(voice)
                                    voiceMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = if (compactLayout) 2.dp else 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.VolumeUp,
                    contentDescription = null,
                    tint = AppAccentSoft,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Gemini 3 Flash Live - ${formatCallDuration(elapsedSeconds)}",
                    color = AppTextPrimary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Text(
                text = modelId,
                color = AppTextSecondary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 3.dp)
            )

            Spacer(modifier = Modifier.height(if (compactLayout) 8.dp else 14.dp))
            if (videoEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .heightIn(max = if (compactLayout) 170.dp else 220.dp)
                        .aspectRatio(4f / 3f)
                ) {
                    LiveCameraPreview(
                        cameraFacing = cameraFacing,
                        onFrame = onCameraFrame,
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.dp, AppAccentSoft.copy(alpha = 0.82f), RoundedCornerShape(24.dp))
                    )
                    Surface(
                        color = AppSurface.copy(alpha = 0.78f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, AppStroke),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        IconButton(onClick = onSwitchCamera) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Switch camera",
                                tint = AppTextPrimary
                            )
                        }
                    }
                    Surface(
                        color = AppSurface.copy(alpha = 0.78f),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, AppStroke.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = if (cameraFacing == VoiceCallCameraFacing.Front) "Front camera" else "Back camera",
                            color = AppTextPrimary,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                        )
                    }
                }
            } else {
                VoiceAvatar(
                    persona = persona,
                    outerSize = avatarOuterSize,
                    imageSize = avatarImageSize
                )
            }

            Spacer(modifier = Modifier.height(if (compactLayout) 10.dp else 18.dp))
            val callDisplayName = persona.displayName.ifBlank { "New Persona" }
            Text(
                text = buildAnnotatedString {
                    val splitIndex = callDisplayName.lastIndexOf(' ')
                    if (splitIndex > 0) {
                        append(callDisplayName.substring(0, splitIndex))
                        append(" ")
                        withStyle(SpanStyle(color = AppAccentSoft, fontWeight = FontWeight.Bold)) {
                            append(callDisplayName.substring(splitIndex + 1))
                        }
                    } else {
                        withStyle(SpanStyle(color = AppAccentSoft, fontWeight = FontWeight.Bold)) {
                            append(callDisplayName)
                        }
                    }
                },
                color = AppTextPrimary,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isOnline) Color(0xFF35D07F) else Color(0xFF8B8898))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isOnline) "Online" else "Offline",
                    color = AppTextSecondary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            VoiceWaveform(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 8.dp)
                    .height(if (compactLayout) 38.dp else 48.dp)
            )

            LiveCallTranscriptPanel(
                lines = transcriptLines,
                status = status.takeIf { it.isNotBlank() && it != "Idle" }
                    ?: if (muted) "Muted" else "Listening...",
                voiceName = selectedVoice.label,
                error = error,
                onRetry = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 4.dp, bottom = 10.dp)
            )
            Surface(
                color = AppSurface.copy(alpha = 0.86f),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, AppStroke),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VoiceControlButton(
                        icon = if (muted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                        label = "Mute",
                        selected = muted,
                        onClick = onToggleMute
                    )
                    VoiceControlButton(
                        icon = Icons.Rounded.Videocam,
                        label = "Video",
                        selected = videoEnabled,
                        onClick = onToggleVideo
                    )
                    VoiceControlButton(
                        icon = Icons.Rounded.ScreenShare,
                        label = "Share",
                        selected = screenShareEnabled,
                        onClick = onToggleScreenShare
                    )
                    VoiceControlButton(
                        icon = Icons.Rounded.CallEnd,
                        label = "End",
                        danger = true,
                        onClick = onEndCall
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveCallTranscriptPanel(
    lines: List<LiveCallTranscriptLine>,
    status: String,
    voiceName: String,
    error: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recentLines = lines.takeLast(3)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AppSurface.copy(alpha = 0.74f))
            .border(1.dp, AppStroke.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (error != null) {
                Surface(
                    color = Color(0xFF321820),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF7E8B).copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error,
                            color = Color(0xFFFFB4BD),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        TextButton(onClick = onRetry) {
                            Text("Reconnect", color = AppAccentSoft)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (recentLines.isEmpty()) {
                Text(
                    text = if (status == "Speaking...") "Speaking..." else "Start talking",
                    color = AppTextMuted,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            } else {
                recentLines.forEachIndexed { index, line ->
                    val latest = index == recentLines.lastIndex
                    Text(
                        text = line.text,
                        color = when {
                            latest -> AppTextPrimary
                            line.role == "user" -> AppAccentSoft.copy(alpha = 0.68f)
                            else -> AppTextSecondary.copy(alpha = 0.62f)
                        },
                        style = if (latest) {
                            MaterialTheme.typography.bodyLarge
                        } else {
                            MaterialTheme.typography.bodyMedium
                        },
                        textAlign = TextAlign.Center,
                        maxLines = if (latest) 5 else 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = if (index == 0) 0.dp else 7.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (error == null) AppAccentSoft else Color(0xFFFF7E8B))
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = "$status  -  $voiceName",
                    color = AppTextMuted,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun LiveCameraPreview(
    cameraFacing: VoiceCallCameraFacing,
    onFrame: (ByteArray) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    DisposableEffect(lifecycleOwner, previewView, cameraFacing) {
        val analysisExecutor = Executors.newSingleThreadExecutor()
        val providerFuture = ProcessCameraProvider.getInstance(context)
        val mainExecutor = ContextCompat.getMainExecutor(context)
        var provider: ProcessCameraProvider? = null
        var disposed = false
        var lastFrameAt = 0L

        providerFuture.addListener({
            if (disposed) return@addListener
            provider = runCatching { providerFuture.get() }.getOrNull()
            val cameraProvider = provider ?: return@addListener
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val analysis = ImageAnalysis.Builder()
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { useCase ->
                    useCase.setAnalyzer(analysisExecutor) { image ->
                        val now = System.currentTimeMillis()
                        try {
                            if (now - lastFrameAt >= 1_000L) {
                                lastFrameAt = now
                                cameraImageToJpeg(image)?.let(onFrame)
                            }
                        } finally {
                            image.close()
                        }
                    }
            }
            runCatching {
                val selector = when (cameraFacing) {
                    VoiceCallCameraFacing.Front -> CameraSelector.DEFAULT_FRONT_CAMERA
                    VoiceCallCameraFacing.Back -> CameraSelector.DEFAULT_BACK_CAMERA
                }
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    preview,
                    analysis
                )
            }
        }, mainExecutor)

        onDispose {
            disposed = true
            provider?.unbindAll()
            analysisExecutor.shutdownNow()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

private fun cameraImageToJpeg(image: androidx.camera.core.ImageProxy): ByteArray? {
    val plane = image.planes.firstOrNull() ?: return null
    val width = image.width
    val height = image.height
    val rowPadding = plane.rowStride - plane.pixelStride * width
    val paddedWidth = width + rowPadding / plane.pixelStride
    val padded = Bitmap.createBitmap(paddedWidth, height, Bitmap.Config.ARGB_8888)
    plane.buffer.rewind()
    padded.copyPixelsFromBuffer(plane.buffer)
    val cropped = Bitmap.createBitmap(padded, 0, 0, width, height)
    if (cropped !== padded) padded.recycle()
    val matrix = Matrix().apply {
        postRotate(image.imageInfo.rotationDegrees.toFloat())
    }
    val rotated = Bitmap.createBitmap(
        cropped,
        0,
        0,
        cropped.width,
        cropped.height,
        matrix,
        true
    )
    if (rotated !== cropped) cropped.recycle()
    return ByteArrayOutputStream().use { output ->
        rotated.compress(Bitmap.CompressFormat.JPEG, 70, output)
        rotated.recycle()
        output.toByteArray()
    }
}

@Composable
private fun VoiceAvatar(
    persona: PersonaUiState,
    outerSize: Int,
    imageSize: Int
) {
    val pulse by rememberInfiniteTransition(label = "voice-pulse").animateFloat(
        initialValue = 0.84f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "voice-pulse-scale"
    )
    Box(
        modifier = Modifier
            .size(outerSize.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(pulse)
                .clip(CircleShape)
                .border(2.dp, AppAccentSoft.copy(alpha = 0.72f), CircleShape)
                .background(AppAccentDim.copy(alpha = 0.18f))
        )
        Avatar(
            persona = persona,
            size = imageSize,
            modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
        )
    }
}

@Composable
private fun VoiceWaveform(modifier: Modifier = Modifier) {
    val phase by rememberInfiniteTransition(label = "voice-wave").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "voice-wave-phase"
    )
    val bars = listOf(0.15f, 0.25f, 0.55f, 0.82f, 0.42f, 0.68f, 0.35f, 0.92f, 0.58f, 0.25f, 0.18f)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        bars.forEachIndexed { index, base ->
            val animated = (base + phase * if (index % 2 == 0) 0.34f else 0.18f).coerceIn(0.1f, 1f)
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .width(5.dp)
                    .height((16 + animated * 54).dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(AppAccent.copy(alpha = 0.32f), AppAccentSoft, AppAccent.copy(alpha = 0.32f))
                        )
                    )
            )
        }
    }
}

@Composable
private fun VoiceControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean = false,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    val color = when {
        danger -> Color(0xFFFF405A)
        selected -> AppAccent
        else -> AppSurface2
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(68.dp)
    ) {
        Surface(
            color = color,
            shape = CircleShape,
            border = BorderStroke(1.dp, if (danger) Color(0xFFFF6D7B) else AppAccent.copy(alpha = 0.55f)),
            modifier = Modifier
                .size(48.dp)
                .clickable(onClick = onClick)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.padding(12.dp)
            )
        }
        Text(
            text = label,
            color = if (danger) Color(0xFFFF7E8B) else AppTextPrimary,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
        )
    }
}

private fun formatCallDuration(seconds: Long): String {
    val minutes = seconds / 60L
    val remainder = seconds % 60L
    return String.format(Locale.US, "%02d:%02d", minutes, remainder)
}

@Composable
private fun ChatBackgroundLayer(background: ChatBackground) {
    when (background) {
        ChatBackground.DarkMode -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground)
        )

        ChatBackground.LightMode -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEDECF4))
        )

        ChatBackground.GreyMode -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF24242C))
        )

        ChatBackground.PureWhite -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        )

        ChatBackground.PureBlack -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        )

        ChatBackground.PresetBlack -> BackgroundImage(
            painter = painterResource(id = R.drawable.background_black),
            darkScrim = 0.24f
        )

        ChatBackground.PresetWhite -> BackgroundImage(
            painter = painterResource(id = R.drawable.background_white),
            darkScrim = 0.32f
        )

        is ChatBackground.CustomImage -> BackgroundImage(
            painter = rememberAsyncImagePainter(background.uri),
            darkScrim = 0.42f
        )
    }
}

@Composable
private fun BackgroundImage(
    painter: androidx.compose.ui.graphics.painter.Painter,
    darkScrim: Float
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = darkScrim))
        )
    }
}

@Composable
private fun ChatHeader(
    persona: PersonaUiState,
    groupMembers: List<GroupMember>,
    sessionHeaderName: String,
    sessionHeaderAvatarUri: android.net.Uri?,
    sessionHeaderAvatarScale: Float,
    sessionHeaderAvatarOffsetX: Float,
    sessionHeaderAvatarOffsetY: Float,
    isOnline: Boolean,
    onOpenSessions: () -> Unit,
    onOpenPersona: () -> Unit,
    onOpenVoiceCall: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onOpenSessions) {
            Icon(
                imageVector = Icons.Rounded.Menu,
                contentDescription = "Sessions",
                tint = AppAccentSoft
            )
        }
        SessionHeaderAvatar(
            fallbackPersona = persona,
            avatarUri = sessionHeaderAvatarUri,
            avatarScale = sessionHeaderAvatarScale,
            avatarOffsetX = sessionHeaderAvatarOffsetX,
            avatarOffsetY = sessionHeaderAvatarOffsetY,
            size = 54,
            modifier = Modifier.clickable(onClick = onOpenPersona)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = sessionHeaderName.ifBlank {
                    groupMembers.joinToString(" + ") { it.persona.displayName.ifBlank { "AI" } }
                },
                style = MaterialTheme.typography.titleMedium,
                color = AppTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isOnline) Color(0xFF35D07F) else Color(0xFF8B8898))
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = if (isOnline) "Online" else "Offline",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTextSecondary,
                    maxLines = 1
                )
            }
        }
        IconButton(onClick = onOpenVoiceCall) {
            Icon(
                imageVector = Icons.Rounded.Call,
                contentDescription = "Start voice call",
                tint = AppTextPrimary
            )
        }
        IconButton(onClick = onOpenPersona) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "Persona settings",
                tint = AppTextPrimary
            )
        }
    }
}

@Composable
private fun SessionDrawer(
    sessions: List<ChatSession>,
    projects: List<ProjectUiState>,
    activeSessionId: String,
    activeProject: ProjectUiState?,
    persona: PersonaUiState,
    appVersionLabel: String,
    onNewSession: () -> Unit,
    onNewProject: () -> String,
    onSelectSession: (String) -> Unit,
    onNewSessionInProject: (String) -> Unit,
    onMoveSessionToProject: (String, String?) -> Unit,
    onUpdateProjectName: (String, String) -> Unit,
    onUpdateProjectDescription: (String, String) -> Unit,
    onUpdateProjectInstruction: (String, String) -> Unit,
    onDeleteSession: (String) -> Unit,
    onRenameSession: (String, String) -> Unit,
    onCloneSession: (String) -> Unit,
    onDuplicateSessionSettings: (String) -> Unit,
    onImportSession: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onClose: () -> Unit
) {
    var deleteTarget by remember { mutableStateOf<ChatSession?>(null) }
    var actionTarget by remember { mutableStateOf<ChatSession?>(null) }
    var renameTarget by remember { mutableStateOf<ChatSession?>(null) }
    var renameDraft by remember { mutableStateOf("") }
    var projectAssignmentTarget by remember { mutableStateOf<ChatSession?>(null) }
    var addToProjectId by remember { mutableStateOf<String?>(null) }
    var sessionSearch by remember { mutableStateOf("") }
    var projectSearch by remember { mutableStateOf("") }
    var drawerSection by remember { mutableStateOf(DrawerSection.Chats) }
    var selectedProjectId by remember { mutableStateOf<String?>(activeProject?.id) }
    val selectedProject = selectedProjectId?.let { id -> projects.firstOrNull { it.id == id } }
    val sessionQuery = sessionSearch.trim()
    val filteredSessions = if (sessionQuery.isBlank()) {
        sessions
    } else {
        sessions.filter { session -> session.matchesSessionQuery(sessionQuery) }
    }
    val projectQuery = projectSearch.trim()
    val filteredProjects = if (projectQuery.isBlank()) {
        projects
    } else {
        projects.filter { project -> project.matchesProjectQuery(projectQuery) }
    }
    val activeProjectSessions = sessions.filter { it.projectId == selectedProjectId }

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.82f),
        color = AppBackground,
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_icon_minimalist),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = appVersionLabel.substringBefore(" v"),
                    color = AppTextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "App settings",
                        tint = AppTextSecondary
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Close sessions",
                        tint = AppTextPrimary
                    )
                }
            }

            DrawerActionRow(
                iconRes = R.drawable.asset_chat,
                label = "New chat",
                highlighted = true,
                onClick = onNewSession
            )
            DrawerActionRow(
                iconRes = R.drawable.asset_session,
                label = "Chats",
                selected = drawerSection == DrawerSection.Chats,
                onClick = {
                    drawerSection = DrawerSection.Chats
                    selectedProjectId = null
                }
            )
            DrawerActionRow(
                iconRes = R.drawable.asset_project,
                label = "Projects",
                selected = drawerSection == DrawerSection.Projects,
                onClick = { drawerSection = DrawerSection.Projects }
            )

            when (drawerSection) {
                DrawerSection.Chats -> {
                    DrawerSearchField(
                        value = sessionSearch,
                        onValueChange = { sessionSearch = it },
                        placeholder = "Search chats",
                        modifier = Modifier.padding(top = 18.dp)
                    )
                    SectionHeader("Recents")
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredSessions, key = { it.id }) { session ->
                            SessionRow(
                                session = session,
                                project = session.projectId?.let { id -> projects.firstOrNull { it.id == id } },
                                selected = session.id == activeSessionId,
                                onClick = { onSelectSession(session.id) },
                                onLongPress = { actionTarget = session }
                            )
                        }
                    }
                }

                DrawerSection.Projects -> {
                    if (selectedProject == null) {
                        DrawerSearchField(
                            value = projectSearch,
                            onValueChange = { projectSearch = it },
                            placeholder = "Search projects",
                            modifier = Modifier.padding(top = 14.dp)
                        )
                        SectionHeader("Projects")
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredProjects, key = { it.id }) { project ->
                                ProjectRow(
                                    project = project,
                                    chatCount = sessions.count { it.projectId == project.id },
                                    selected = project.id == activeProject?.id,
                                    onClick = {
                                        selectedProjectId = project.id
                                    }
                                )
                            }
                        }
                        Button(
                            onClick = {
                                selectedProjectId = onNewProject()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "New project",
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    } else {
                        ProjectDetailPanel(
                            project = selectedProject,
                            sessions = activeProjectSessions,
                            activeSessionId = activeSessionId,
                            modifier = Modifier.weight(1f),
                            onBack = { selectedProjectId = null },
                            onNameChange = { onUpdateProjectName(selectedProject.id, it) },
                            onDescriptionChange = { onUpdateProjectDescription(selectedProject.id, it) },
                            onInstructionChange = { onUpdateProjectInstruction(selectedProject.id, it) },
                            onNewChat = { onNewSessionInProject(selectedProject.id) },
                            onAddExistingChat = { addToProjectId = selectedProject.id },
                            onSelectSession = onSelectSession,
                            onSessionLongPress = { actionTarget = it }
                        )
                    }
                }
            }
            TextButton(
                onClick = onOpenAbout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 2.dp)
            ) {
                Text(
                    text = appVersionLabel,
                    color = AppTextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    deleteTarget?.let { session ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = AppSurface,
            title = {
                Text(
                    text = "Delete chat?",
                    color = AppTextPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = session.displayTitle(),
                    color = AppTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSession(session.id)
                        deleteTarget = null
                    }
                ) {
                    Text(text = "Delete", color = Color(0xFFFF7E8B))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(text = "Cancel", color = AppAccentSoft)
                }
            }
        )
    }

    actionTarget?.let { session ->
        SessionActionSheet(
            session = session,
            onDismiss = { actionTarget = null },
            onDuplicate = {
                onDuplicateSessionSettings(session.id)
                actionTarget = null
            },
            onClone = {
                onCloneSession(session.id)
                actionTarget = null
            },
            onRename = {
                renameTarget = session
                renameDraft = session.displayTitle()
                actionTarget = null
            },
            onMove = {
                projectAssignmentTarget = session
                actionTarget = null
            },
            onDelete = {
                deleteTarget = session
                actionTarget = null
            }
        )
    }

    projectAssignmentTarget?.let { session ->
        ProjectAssignmentSheet(
            session = session,
            projects = projects,
            onDismiss = { projectAssignmentTarget = null },
            onSelectProject = { projectId ->
                onMoveSessionToProject(session.id, projectId)
                projectAssignmentTarget = null
            }
        )
    }

    addToProjectId?.let { projectId ->
        projects.firstOrNull { it.id == projectId }?.let { project ->
            AddChatsToProjectSheet(
                project = project,
                sessions = sessions.filter { it.projectId != project.id },
                onDismiss = { addToProjectId = null },
                onAdd = { sessionId ->
                    onMoveSessionToProject(sessionId, project.id)
                }
            )
        }
    }

    renameTarget?.let { session ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            containerColor = AppSurface,
            title = {
                Text(
                    text = "Rename chat",
                    color = AppTextPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                OutlinedTextField(
                    value = renameDraft,
                    onValueChange = { renameDraft = it.take(72) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppTextPrimary,
                        unfocusedTextColor = AppTextPrimary,
                        focusedBorderColor = AppAccent.copy(alpha = 0.7f),
                        unfocusedBorderColor = AppStroke,
                        cursorColor = AppAccent,
                        focusedContainerColor = AppSurface2,
                        unfocusedContainerColor = AppSurface2
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRenameSession(session.id, renameDraft)
                        renameTarget = null
                    }
                ) {
                    Text(text = "Rename", color = AppAccentSoft)
                }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) {
                    Text(text = "Cancel", color = AppTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun SessionActionSheet(
    session: ChatSession,
    onDismiss: () -> Unit,
    onDuplicate: () -> Unit,
    onClone: () -> Unit,
    onRename: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.46f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            color = AppSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            border = BorderStroke(1.dp, AppStroke),
            shadowElevation = 18.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        ) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(42.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(AppStroke)
                )
                Text(
                    text = session.displayTitle(),
                    color = AppTextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 18.dp, bottom = 10.dp)
                )
                ActionSheetRow(
                    icon = Icons.Rounded.ContentCopy,
                    label = "Copy settings only",
                    onClick = onDuplicate
                )
                ActionSheetRow(
                    icon = Icons.Rounded.ChatBubbleOutline,
                    label = "Clone session",
                    onClick = onClone
                )
                ActionSheetRow(
                    icon = Icons.Rounded.Edit,
                    label = "Rename",
                    onClick = onRename
                )
                ActionSheetRow(
                    icon = Icons.Rounded.InsertDriveFile,
                    label = if (session.projectId == null) "Move to project" else "Change project",
                    onClick = onMove
                )
                ActionSheetRow(
                    icon = Icons.Rounded.DeleteOutline,
                    label = "Delete",
                    onClick = onDelete
                )
            }
        }
    }
}

@Composable
private fun ProjectAssignmentSheet(
    session: ChatSession,
    projects: List<ProjectUiState>,
    onDismiss: () -> Unit,
    onSelectProject: (String?) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            color = AppSurface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            border = BorderStroke(1.dp, AppStroke),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        ) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(18.dp)
            ) {
                Text(
                    text = "Move ${session.displayTitle()}",
                    color = AppTextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Choose where this chat belongs.",
                    color = AppTextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
                ProjectChoiceRow(
                    label = "No project",
                    selected = session.projectId == null,
                    onClick = { onSelectProject(null) }
                )
                projects.forEach { project ->
                    ProjectChoiceRow(
                        label = project.name.ifBlank { "New project" },
                        selected = session.projectId == project.id,
                        onClick = { onSelectProject(project.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectChoiceRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) AppAccentDim else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.InsertDriveFile,
                contentDescription = null,
                tint = if (selected) AppAccentSoft else AppTextSecondary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                color = AppTextPrimary,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.DoneAll,
                    contentDescription = null,
                    tint = AppAccentSoft
                )
            }
        }
    }
}

@Composable
private fun AddChatsToProjectSheet(
    project: ProjectUiState,
    sessions: List<ChatSession>,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var addedIds by remember(project.id) { mutableStateOf(emptySet<String>()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            color = AppSurface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            border = BorderStroke(1.dp, AppStroke),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        ) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Add chats",
                            color = AppTextPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = project.name,
                            color = AppTextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Done", color = AppAccentSoft)
                    }
                }
                if (sessions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Every chat is already in this project.",
                            color = AppTextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sessions, key = { it.id }) { session ->
                            val added = session.id in addedIds
                            Surface(
                                color = if (added) AppAccentDim else AppSurface2,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, AppStroke),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !added) {
                                        onAdd(session.id)
                                        addedIds = addedIds + session.id
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = session.displayTitle(),
                                            color = AppTextPrimary,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                        Text(
                                            text = session.preview,
                                            color = AppTextSecondary,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(
                                        imageVector = if (added) Icons.Rounded.DoneAll else Icons.Rounded.Add,
                                        contentDescription = null,
                                        tint = AppAccentSoft
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SessionRow(
    session: ChatSession,
    project: ProjectUiState?,
    selected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    Surface(
        color = if (selected) AppAccentDim.copy(alpha = 0.82f) else AppSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (selected) AppAccent.copy(alpha = 0.6f) else AppStroke),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (selected) AppAccent else AppSurface2),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.asset_session),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.displayTitle(),
                    color = AppTextPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = session.preview,
                    color = AppTextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (project != null) {
                    Text(
                        text = project.name,
                        color = AppAccentSoft,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(
                text = session.updatedAt,
                color = AppTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "Session actions",
                tint = AppTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EmptyChatState(
    persona: PersonaUiState,
    onCreatePersona: () -> Unit,
    onSuggestion: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 58.dp, start = 12.dp, end = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Avatar(persona = persona, size = 112)
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = buildAnnotatedString {
                append("Hi, I'm ")
                withStyle(SpanStyle(color = AppAccentSoft, fontWeight = FontWeight.Bold)) {
                    append(persona.displayName.ifBlank { "New Persona" })
                }
            },
            color = AppTextPrimary,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = "How can I help you today?",
            color = AppTextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )

        val suggestions = listOf(
            "Tell me a story" to Icons.Rounded.InsertDriveFile,
            "Help me write code" to Icons.Rounded.ContentCopy,
            "Brainstorm ideas" to Icons.Rounded.StarBorder,
            "Explain something" to Icons.Rounded.Info
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            suggestions.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { (label, icon) ->
                        SuggestionChip(
                            label = label,
                            icon = icon,
                            onClick = { onSuggestion(label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        TextButton(
            onClick = onCreatePersona,
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Text(
                text = "Edit persona",
                color = AppAccentSoft,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun SuggestionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = AppSurface.copy(alpha = 0.72f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppStroke),
        modifier = modifier
            .height(56.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppTextSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                color = AppTextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ChatErrorLine(message: String) {
    Text(
        text = message,
        color = Color(0xFFFFA0AA),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp)
    )
}

@Composable
private fun TypingBubble(persona: PersonaUiState) {
    val transition = rememberInfiniteTransition(label = "typingDots")
    val dot1 by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0.35f at 0
                1f at 180
                0.35f at 420
                0.35f at 1200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot1"
    )
    val dot2 by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0.35f at 0
                0.35f at 180
                1f at 400
                0.35f at 660
                0.35f at 1200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot2"
    )
    val dot3 by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0.35f at 0
                0.35f at 400
                1f at 620
                0.35f at 880
                0.35f at 1200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot3"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Avatar(persona = persona, size = 40)
        Spacer(modifier = Modifier.width(10.dp))
        Surface(
            shape = RoundedCornerShape(
                topStart = 22.dp,
                topEnd = 22.dp,
                bottomStart = 6.dp,
                bottomEnd = 22.dp
            ),
            color = AppSurface2,
            border = BorderStroke(1.dp, AppStroke.copy(alpha = 0.7f)),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TypingDot(alpha = dot1)
                TypingDot(alpha = dot2)
                TypingDot(alpha = dot3)
            }
        }
    }
}

@Composable
private fun TypingDot(alpha: Float) {
    Box(
        modifier = Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(AppAccentSoft.copy(alpha = alpha))
    )
}

@Composable
private fun AnimatedMessageBubble(
    message: ChatMessage,
    persona: PersonaUiState,
    groupMembers: List<GroupMember>,
    animateText: Boolean,
    onTextAnimationStart: (String) -> Unit,
    onOpenImage: (android.net.Uri) -> Unit,
    onMessageLongPress: (ChatMessage) -> Unit
) {
    var visible by remember(message.id) { mutableStateOf(false) }
    var displayedContent by remember(message.id) {
        mutableStateOf(if (animateText) "" else message.content)
    }

    LaunchedEffect(message.id) {
        visible = true
    }

    LaunchedEffect(message.id, message.content, animateText) {
        if (!animateText) {
            displayedContent = message.content
            return@LaunchedEffect
        }
        onTextAnimationStart(message.id)
        if (displayedContent.length > message.content.length) {
            displayedContent = ""
        }
        val chunkSize = (message.content.length / 90).coerceIn(2, 10)
        var index = displayedContent.length
        while (index < message.content.length) {
            index = (index + chunkSize).coerceAtMost(message.content.length)
            displayedContent = message.content.take(index)
            delay(12)
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(220)) +
            slideInVertically(animationSpec = tween(260), initialOffsetY = { it / 3 }) +
            scaleIn(animationSpec = tween(260), initialScale = 0.96f)
    ) {
        MessageBubble(
            message = message,
            persona = persona,
            groupMembers = groupMembers,
            displayContent = displayedContent,
            onOpenImage = onOpenImage,
            onMessageLongPress = onMessageLongPress
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    message: ChatMessage,
    persona: PersonaUiState,
    groupMembers: List<GroupMember>,
    displayContent: String = message.content,
    onOpenImage: (android.net.Uri) -> Unit,
    onMessageLongPress: (ChatMessage) -> Unit
) {
    val isUser = message.role == "user"
    val speakerPersona = if (isUser) {
        persona
    } else {
        groupMembers.firstOrNull { it.id == message.speakerId }?.persona ?: persona
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Avatar(persona = speakerPersona, size = 40)
            Spacer(modifier = Modifier.width(10.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 22.dp,
                topEnd = 22.dp,
                bottomStart = if (isUser) 22.dp else 6.dp,
                bottomEnd = if (isUser) 6.dp else 22.dp
            ),
            color = if (isUser) AppAccentDim else AppSurface2,
            border = if (isUser) BorderStroke(1.dp, AppAccent.copy(alpha = 0.18f)) else BorderStroke(1.dp, AppStroke.copy(alpha = 0.7f)),
            shadowElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .combinedClickable(
                    onClick = {},
                    onLongClick = { onMessageLongPress(message) }
                )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                if (!isUser && groupMembers.size > 1) {
                    Text(
                        text = message.speakerName ?: speakerPersona.displayName,
                        color = AppAccentSoft,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                if (message.isImageLoading) {
                    AnimeImageLoadingCard()
                }
                message.remoteImageUrl?.let { imageUrl ->
                    val imageUri = android.net.Uri.parse(imageUrl)
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "Fetched image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onOpenImage(imageUri) }
                    )
                }
                if (message.imageUris.isNotEmpty()) {
                    message.imageUris.forEachIndexed { index, uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Message image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (message.imageUris.size == 1) 180.dp else 118.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onOpenImage(uri) }
                        )
                        if (index != message.imageUris.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    if (displayContent.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                val showText = displayContent.isNotBlank() && !message.isImageLoading && message.remoteImageUrl == null
                if (showText) {
                    MarkdownContent(
                        input = displayContent,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.time,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTextSecondary.copy(alpha = 0.9f)
                    )
                    if (isUser) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Rounded.DoneAll,
                            contentDescription = "Sent",
                            tint = AppAccentSoft,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconRes: Int? = null,
    label: String,
    selected: Boolean = false,
    highlighted: Boolean = false,
    onClick: () -> Unit
) {
    val contentColor = if (highlighted || selected) AppAccentSoft else AppTextPrimary
    Surface(
        color = if (selected) AppSurface2 else Color.Transparent,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconRes != null) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
private fun DrawerSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = AppTextSecondary
            )
        },
        placeholder = {
            Text(text = placeholder, color = AppTextSecondary)
        },
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AppTextPrimary,
            unfocusedTextColor = AppTextPrimary,
            focusedBorderColor = AppAccent,
            unfocusedBorderColor = AppStroke,
            cursorColor = AppAccent,
            focusedContainerColor = AppSurface2,
            unfocusedContainerColor = AppSurface2
        ),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun SectionHeader(label: String) {
    Text(
        text = label,
        color = AppTextSecondary,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
    )
}

@Composable
private fun ProjectRow(
    project: ProjectUiState,
    chatCount: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) AppSurface2 else Color.Transparent,
        shape = RoundedCornerShape(14.dp),
        border = if (selected) BorderStroke(1.dp, AppStroke) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.asset_project),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = project.name.ifBlank { "New project" },
                    color = AppTextPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (selected) {
                    Icon(
                        imageVector = Icons.Rounded.StarBorder,
                        contentDescription = null,
                        tint = AppAccentSoft,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = "$chatCount chats · Edited ${project.updatedAt}",
                color = AppTextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
}

@Composable
private fun ProjectDetailPanel(
    project: ProjectUiState,
    sessions: List<ChatSession>,
    activeSessionId: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onInstructionChange: (String) -> Unit,
    onNewChat: () -> Unit,
    onAddExistingChat: () -> Unit,
    onSelectSession: (String) -> Unit,
    onSessionLongPress: (ChatSession) -> Unit
) {
    var editVisible by remember(project.id) { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back to projects",
                    tint = AppTextPrimary
                )
            }
            Text(
                text = "Project",
                color = AppTextSecondary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            text = project.name.ifBlank { "New project" },
            color = AppTextPrimary,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
        if (project.description.isNotBlank()) {
            Text(
                text = project.description,
                color = AppTextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        Surface(
            color = AppSurface,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, AppStroke),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
                .clickable { editVisible = true }
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Project instruction",
                        color = AppTextPrimary,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit project",
                        tint = AppAccentSoft,
                        modifier = Modifier.size(19.dp)
                    )
                }
                Text(
                    text = project.instruction.ifBlank {
                        "Add shared context and instructions for every chat in this project."
                    },
                    color = if (project.instruction.isBlank()) AppTextMuted else AppTextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onNewChat,
                colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("New chat", color = Color.White)
            }
            Button(
                onClick = onAddExistingChat,
                colors = ButtonDefaults.buttonColors(containerColor = AppSurface2),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Icon(Icons.Rounded.InsertDriveFile, contentDescription = null, tint = AppAccentSoft)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add chat", color = AppTextPrimary)
            }
        }
        SectionHeader("Recent chats")
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sessions, key = { it.id }) { session ->
                SessionRow(
                    session = session,
                    project = project,
                    selected = session.id == activeSessionId,
                    onClick = { onSelectSession(session.id) },
                    onLongPress = { onSessionLongPress(session) }
                )
            }
        }
    }

    if (editVisible) {
        AlertDialog(
            onDismissRequest = { editVisible = false },
            containerColor = AppSurface,
            title = {
                Text("Edit project", color = AppTextPrimary)
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = project.name,
                        onValueChange = onNameChange,
                        label = { Text("Project name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = project.description,
                        onValueChange = onDescriptionChange,
                        label = { Text("Description") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = project.instruction,
                        onValueChange = onInstructionChange,
                        label = { Text("Shared instruction") },
                        minLines = 5,
                        maxLines = 10,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { editVisible = false }) {
                    Text("Done", color = AppAccentSoft)
                }
            }
        )
    }
}

@Composable
private fun AnimeImageLoadingCard() {
    val transition = rememberInfiniteTransition(label = "animeImageLoading")
    val alpha by transition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.68f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "animeImageLoadingAlpha"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppStroke.copy(alpha = alpha))
    ) {
        Surface(
            color = AppSurface.copy(alpha = 0.72f),
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    color = AppAccentSoft,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Fetching image",
                    color = AppTextPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun MessageActionSheet(
    message: ChatMessage,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onRetry: () -> Unit,
    onSpeak: () -> Unit
) {
    val isUser = message.role == "user"
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.46f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            color = AppSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            border = BorderStroke(1.dp, AppStroke),
            shadowElevation = 18.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        ) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(42.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(AppStroke)
                )
                Text(
                    text = if (isUser) "Your message" else "AI response",
                    color = AppTextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 18.dp, bottom = 10.dp)
                )
                ActionSheetRow(
                    icon = Icons.Rounded.ContentCopy,
                    label = if (message.content.isBlank() && message.remoteImageUrl != null) "Copy image link" else "Copy",
                    onClick = onCopy
                )
                if (isUser) {
                    ActionSheetRow(
                        icon = Icons.Rounded.Edit,
                        label = "Edit",
                        onClick = onEdit
                    )
                }
                ActionSheetRow(
                    icon = Icons.Rounded.Refresh,
                    label = if (isUser) "Retry from here" else "Retry response",
                    onClick = onRetry
                )
                if (!isUser && message.content.isNotBlank()) {
                    ActionSheetRow(
                        icon = Icons.Rounded.VolumeUp,
                        label = "Speak",
                        onClick = onSpeak
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeechPreviewSheet(
    state: TtsPreviewState,
    onTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onGenerate: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.46f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            color = AppSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            border = BorderStroke(1.dp, AppStroke),
            shadowElevation = 18.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        ) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(42.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(AppStroke)
                )
                Text(
                    text = "Speech preview",
                    color = AppTextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 18.dp)
                )
                Text(
                    text = "Edit before ElevenLabs uses credits.",
                    color = AppTextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                if (state.isPreparing) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = AppAccentSoft,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Preparing Japanese speech",
                            color = AppTextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = state.text,
                        onValueChange = onTextChange,
                        enabled = !state.isGenerating,
                        minLines = 4,
                        maxLines = 8,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppTextPrimary,
                            unfocusedTextColor = AppTextPrimary,
                            disabledTextColor = AppTextSecondary,
                            focusedBorderColor = AppAccent.copy(alpha = 0.7f),
                            unfocusedBorderColor = AppStroke,
                            disabledBorderColor = AppStroke,
                            cursorColor = AppAccent,
                            focusedContainerColor = AppSurface2,
                            unfocusedContainerColor = AppSurface2,
                            disabledContainerColor = AppSurface2
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 148.dp)
                    )
                    Text(
                        text = "Estimated ElevenLabs usage: ${state.characterCount} chars",
                        color = AppTextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !state.isGenerating
                    ) {
                        Text(
                            text = "Cancel",
                            color = AppTextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onGenerate,
                        enabled = !state.isPreparing && !state.isGenerating && state.text.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (state.isGenerating) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (state.isGenerating) "Generating" else "Generate & play",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionSheetRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppAccentSoft,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                color = AppTextPrimary,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun ChatToolsSheet(
    onDismiss: () -> Unit,
    onScrollTop: () -> Unit,
    onScrollBottom: () -> Unit,
    onCamera: () -> Unit,
    onPhotos: () -> Unit,
    onFiles: () -> Unit,
    messages: List<ChatMessage>,
    onJumpToMessage: (Int) -> Unit,
    webSearchEnabled: Boolean,
    webSearchAvailable: Boolean,
    onWebSearchChange: (Boolean) -> Unit,
    animeImageModeEnabled: Boolean,
    animeImagePreset: AnimeImagePreset,
    onAnimeImageModeChange: (Boolean) -> Unit,
    onAnimeImagePresetChange: (AnimeImagePreset) -> Unit
) {
    var keywordSearch by remember { mutableStateOf("") }
    val keywordResults = remember(messages, keywordSearch) {
        val query = keywordSearch.trim()
        if (query.isBlank()) {
            emptyList()
        } else {
            messages.mapIndexedNotNull { index, message ->
                if (
                    message.content.contains(query, ignoreCase = true) ||
                    message.speakerName.orEmpty().contains(query, ignoreCase = true)
                ) {
                    index to message
                } else {
                    null
                }
            }.take(8)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.52f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            color = AppSurface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            border = BorderStroke(1.dp, AppStroke),
            shadowElevation = 18.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        ) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = AppTextPrimary
                        )
                    }
                    Text(
                        text = "Chat tools",
                        color = AppTextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    AttachmentOptionCard(
                        imageRes = R.drawable.tool_camera,
                        label = "Camera",
                        onClick = onCamera,
                        modifier = Modifier.weight(1f)
                    )
                    AttachmentOptionCard(
                        imageRes = R.drawable.tool_photos,
                        label = "Photos",
                        onClick = onPhotos,
                        modifier = Modifier.weight(1f)
                    )
                    AttachmentOptionCard(
                        imageRes = R.drawable.tool_files,
                        label = "Files",
                        onClick = onFiles,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    ToolActionButton(
                        icon = Icons.Rounded.KeyboardArrowDown,
                        label = "Top",
                        iconRotation = 180f,
                        onClick = onScrollTop,
                        modifier = Modifier.weight(1f)
                    )
                    ToolActionButton(
                        icon = Icons.Rounded.KeyboardArrowDown,
                        label = "Bottom",
                        onClick = onScrollBottom,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = keywordSearch,
                    onValueChange = { keywordSearch = it },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = AppTextSecondary
                        )
                    },
                    placeholder = {
                        Text(text = "Search this chat", color = AppTextSecondary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppTextPrimary,
                        unfocusedTextColor = AppTextPrimary,
                        focusedBorderColor = AppAccent,
                        unfocusedBorderColor = AppStroke,
                        cursorColor = AppAccent,
                        focusedContainerColor = AppSurface2,
                        unfocusedContainerColor = AppSurface2
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                AnimatedVisibility(visible = keywordSearch.isNotBlank()) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        if (keywordResults.isEmpty()) {
                            Text(
                                text = "No matches",
                                color = AppTextSecondary,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                            )
                        } else {
                            keywordResults.forEach { (index, message) ->
                                SearchResultRow(
                                    message = message,
                                    onClick = { onJumpToMessage(index) }
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(AppStroke)
                        .padding(top = 18.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Public,
                        contentDescription = null,
                        tint = AppTextPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Web search",
                        color = AppTextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = webSearchEnabled,
                        onCheckedChange = onWebSearchChange,
                        enabled = webSearchAvailable
                    )
                }
                if (!webSearchAvailable) {
                    Text(
                        text = "Add a Tavily key to enable web search.",
                        color = AppTextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 44.dp, bottom = 4.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ImageSearch,
                        contentDescription = null,
                        tint = AppTextPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Anime image",
                        color = AppTextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = animeImageModeEnabled,
                        onCheckedChange = onAnimeImageModeChange
                    )
                }
                AnimatedVisibility(visible = animeImageModeEnabled) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(start = 44.dp, top = 6.dp)
                    ) {
                        items(AnimeImagePreset.entries) { preset ->
                            PresetChoiceChip(
                                label = preset.label,
                                selected = animeImagePreset == preset,
                                onClick = { onAnimeImagePresetChange(preset) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    iconRotation: Float = 0f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = AppSurface2,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppStroke),
        modifier = modifier
            .height(46.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppAccentSoft,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { rotationZ = iconRotation }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = AppTextPrimary,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun SearchResultRow(
    message: ChatMessage,
    onClick: () -> Unit
) {
    val label = if (message.role == "user") {
        "You"
    } else {
        message.speakerName ?: "AI"
    }
    val snippet = message.content.ifBlank {
        message.remoteImageUrl ?: if (message.imageUris.isNotEmpty()) "[image]" else "(empty)"
    }
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
            Text(
                text = label,
                color = AppAccentSoft,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = snippet,
                color = AppTextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PresetChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (selected) AppAccentDim else AppSurface2,
        border = BorderStroke(1.dp, if (selected) AppAccent else AppStroke),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = if (selected) AppAccentSoft else AppTextPrimary,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun AttachmentOptionCard(
    imageRes: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = AppSurface2,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, AppStroke),
        modifier = modifier
            .height(112.dp)
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = label,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun ChatInputBar(
    placeholder: String,
    draft: String,
    isSending: Boolean,
    attachedImageUris: List<android.net.Uri>,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onOpenTools: () -> Unit,
    onRemoveAttachedImage: (android.net.Uri) -> Unit,
    onOpenImage: (android.net.Uri) -> Unit
) {
    val sendScale by animateFloatAsState(
        targetValue = if ((draft.isBlank() && attachedImageUris.isEmpty()) || isSending) 0.94f else 1f,
        label = "sendScale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 6.dp),
        color = AppSurface2,
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, AppAccent.copy(alpha = 0.42f)),
        shadowElevation = 8.dp
    ) {
        Column {
            if (attachedImageUris.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(attachedImageUris, key = { it.toString() }) { uri ->
                        AttachedImageChip(
                            uri = uri,
                            onOpen = { onOpenImage(uri) },
                            onRemove = { onRemoveAttachedImage(uri) }
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .heightIn(min = 68.dp, max = 176.dp)
                    .padding(start = 10.dp, top = 8.dp, end = 10.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = AppSurface,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, AppStroke),
                    modifier = Modifier
                        .size(52.dp)
                        .clickable(onClick = onOpenTools)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Open chat tools",
                            tint = AppTextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    singleLine = false,
                    minLines = 1,
                    maxLines = 6,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp, max = 150.dp),
                    placeholder = {
                        Text(
                            text = placeholder,
                            color = AppTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppTextPrimary,
                        unfocusedTextColor = AppTextPrimary,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = AppAccent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
                Surface(
                    color = AppSurface,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, AppStroke),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = "Voice input",
                            tint = AppTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSend,
                    enabled = (draft.isNotBlank() || attachedImageUris.isNotEmpty()) && !isSending,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppAccent,
                        disabledContainerColor = AppAccent.copy(alpha = 0.35f)
                    ),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(52.dp)
                        .scale(sendScale)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachedImageChip(
    uri: android.net.Uri,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    Box {
        Image(
            painter = rememberAsyncImagePainter(uri),
            contentDescription = "Attached image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onOpen)
        )
        Surface(
            color = Color.Black.copy(alpha = 0.72f),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 5.dp, y = (-5).dp)
                .size(24.dp)
                .clickable(onClick = onRemove)
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Remove image",
                tint = Color.White,
                modifier = Modifier.padding(5.dp)
            )
        }
    }
}

@Composable
private fun MarkdownContent(
    input: String,
    modifier: Modifier = Modifier
) {
    val blocks = remember(input) { parseMarkdownBlocks(input) }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = formatMarkdownLite(block.text),
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppTextPrimary,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                    )
                }

                is MarkdownBlock.Table -> {
                    MarkdownTable(block.rows)
                }
            }
        }
    }
}

@Composable
private fun MarkdownTable(rows: List<List<String>>) {
    if (rows.isEmpty()) return
    val columnCount = rows.maxOf { it.size }.coerceAtLeast(1)
    val widths = remember(rows) {
        List(columnCount) { column ->
            val longest = rows.maxOf { row -> row.getOrNull(column)?.length ?: 0 }
            when {
                longest >= 26 -> 168.dp
                longest >= 14 -> 122.dp
                else -> 88.dp
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, AppStroke.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
    ) {
        rows.forEachIndexed { rowIndex, row ->
            Row {
                for (column in 0 until columnCount) {
                    val isHeader = rowIndex == 0
                    Box(
                        modifier = Modifier
                            .width(widths[column])
                            .heightIn(min = 42.dp)
                            .background(if (isHeader) AppSurface.copy(alpha = 0.92f) else AppBackground.copy(alpha = 0.34f))
                            .border(0.5.dp, AppStroke.copy(alpha = 0.58f))
                            .padding(horizontal = 10.dp, vertical = 9.dp)
                    ) {
                        Text(
                            text = formatMarkdownLite(row.getOrNull(column).orEmpty()),
                            color = AppTextPrimary,
                            style = if (isHeader) {
                                MaterialTheme.typography.labelLarge
                            } else {
                                MaterialTheme.typography.bodyMedium
                            },
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private sealed class MarkdownBlock {
    data class Paragraph(val text: String) : MarkdownBlock()
    data class Table(val rows: List<List<String>>) : MarkdownBlock()
}

private fun parseMarkdownBlocks(input: String): List<MarkdownBlock> {
    val lines = input.trim().lines()
    val blocks = mutableListOf<MarkdownBlock>()
    var index = 0

    while (index < lines.size) {
        if (lines[index].isBlank()) {
            index++
            continue
        }

        if (index + 1 < lines.size && isMarkdownTableRow(lines[index]) && isMarkdownTableSeparator(lines[index + 1])) {
            val rows = mutableListOf(parseMarkdownTableRow(lines[index]))
            index += 2
            while (index < lines.size && isMarkdownTableRow(lines[index])) {
                rows += parseMarkdownTableRow(lines[index])
                index++
            }
            blocks += MarkdownBlock.Table(rows)
            continue
        }

        val paragraphLines = mutableListOf<String>()
        while (index < lines.size && lines[index].isNotBlank()) {
            if (index + 1 < lines.size && isMarkdownTableRow(lines[index]) && isMarkdownTableSeparator(lines[index + 1])) {
                break
            }
            paragraphLines += lines[index]
            index++
        }
        if (paragraphLines.isNotEmpty()) {
            blocks += MarkdownBlock.Paragraph(paragraphLines.joinToString("\n"))
        }
    }

    return blocks.ifEmpty { listOf(MarkdownBlock.Paragraph(input)) }
}

private fun isMarkdownTableRow(line: String): Boolean {
    val trimmed = line.trim()
    return trimmed.startsWith("|") && trimmed.endsWith("|") && trimmed.count { it == '|' } >= 2
}

private fun isMarkdownTableSeparator(line: String): Boolean {
    if (!isMarkdownTableRow(line)) return false
    return parseMarkdownTableRow(line).all { cell ->
        cell.matches(Regex(":?-{3,}:?"))
    }
}

private fun parseMarkdownTableRow(line: String): List<String> {
    return line.trim()
        .removePrefix("|")
        .removeSuffix("|")
        .split("|")
        .map { it.trim() }
}

private fun formatMarkdownLite(input: String): AnnotatedString {
    return buildAnnotatedString {
        var index = 0
        while (index < input.length) {
            when {
                input.startsWith("$$", index) -> {
                    val end = input.indexOf("$$", index + 2)
                    if (end > index) {
                        withStyle(SpanStyle(fontFamily = FontFamily.Monospace, color = AppAccentSoft)) {
                            append(input.substring(index + 2, end))
                        }
                        index = end + 2
                    } else {
                        append(input[index])
                        index++
                    }
                }

                input.startsWith("***", index) -> {
                    index = appendDelimited(input, index, "***", SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic))
                }

                input.startsWith("**", index) -> {
                    index = appendDelimited(input, index, "**", SpanStyle(fontWeight = FontWeight.Bold))
                }

                input[index] == '*' -> {
                    index = appendDelimited(input, index, "*", SpanStyle(fontStyle = FontStyle.Italic))
                }

                input[index] == '`' -> {
                    index = appendDelimited(input, index, "`", SpanStyle(fontFamily = FontFamily.Monospace, color = AppAccentSoft))
                }

                input[index] == '$' -> {
                    index = appendDelimited(input, index, "$", SpanStyle(fontFamily = FontFamily.Monospace, color = AppAccentSoft))
                }

                else -> {
                    append(input[index])
                    index++
                }
            }
        }
    }
}

private fun AnnotatedString.Builder.appendDelimited(
    input: String,
    start: Int,
    delimiter: String,
    style: SpanStyle
): Int {
    val contentStart = start + delimiter.length
    val end = input.indexOf(delimiter, contentStart)
    return if (end > contentStart) {
        withStyle(style) {
            append(input.substring(contentStart, end))
        }
        end + delimiter.length
    } else {
        append(input[start])
        start + 1
    }
}

@Composable
private fun PersonaSettingsSheet(
    persona: PersonaUiState,
    groupMembers: List<GroupMember>,
    showSessionHeaderControls: Boolean,
    sessionHeaderName: String,
    sessionHeaderAvatarUri: android.net.Uri?,
    sessionHeaderAvatarScale: Float,
    sessionHeaderAvatarOffsetX: Float,
    sessionHeaderAvatarOffsetY: Float,
    activeMemberId: String,
    responseRounds: Int,
    memoryEnabled: Boolean,
    storyLore: String,
    levelSystemEnabled: Boolean,
    levelXp: Int,
    background: ChatBackground,
    moreOptions: Boolean,
    activeApiKeyLabel: String?,
    tavilyApiKeyLabel: String?,
    rule34UserIdLabel: String?,
    rule34ApiKeyLabel: String?,
    elevenLabsApiKeyLabel: String?,
    elevenLabsVoiceIdLabel: String?,
    elevenLabsModelIdLabel: String?,
    quotaUsage: QuotaUsageState,
    dailyRequestLimit: Int?,
    onBack: () -> Unit,
    onVendorChange: (ApiVendor) -> Unit,
    onSelectMember: (String) -> Unit,
    onAddMember: () -> Unit,
    onRemoveMember: (String) -> Unit,
    onResponseRoundsChange: (Int) -> Unit,
    onMemoryEnabledChange: (Boolean) -> Unit,
    onLevelSystemEnabledChange: (Boolean) -> Unit,
    onNameChange: (String) -> Unit,
    onSessionHeaderNameChange: (String) -> Unit,
    onInstructionModeChange: (InstructionMode) -> Unit,
    onBeginnerRoleChange: (String) -> Unit,
    onBeginnerStyleChange: (String) -> Unit,
    onBeginnerLimitsChange: (String) -> Unit,
    onStoryLoreChange: (String) -> Unit,
    onPromptChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onSafetyLevelChange: (SafetyLevel) -> Unit,
    onThinkingEffortChange: (GeminiThinkingEffort) -> Unit,
    onTemperatureChange: (Float) -> Unit,
    onAvatarChange: (android.net.Uri?) -> Unit,
    onAvatarTransform: (Float, Float, Float) -> Unit,
    onSessionHeaderAvatarChange: (android.net.Uri?) -> Unit,
    onSessionHeaderAvatarTransform: (Float, Float, Float) -> Unit,
    onBackgroundChange: (ChatBackground) -> Unit,
    onCustomBackgroundChange: (android.net.Uri?) -> Unit,
    onToggleMore: () -> Unit,
    onEditApiKey: () -> Unit,
    onClearApiKey: () -> Unit,
    onEditTavilyKey: () -> Unit,
    onClearTavilyKey: () -> Unit,
    onEditRule34UserId: () -> Unit,
    onClearRule34UserId: () -> Unit,
    onEditRule34ApiKey: () -> Unit,
    onClearRule34ApiKey: () -> Unit,
    onEditElevenLabsKey: () -> Unit,
    onClearElevenLabsKey: () -> Unit,
    onEditElevenLabsVoiceId: () -> Unit,
    onClearElevenLabsVoiceId: () -> Unit,
    onEditElevenLabsModelId: () -> Unit,
    onClearElevenLabsModelId: () -> Unit,
    onDeleteSession: () -> Unit,
    onSave: () -> Unit
) {
    var instructionPromptExpanded by remember { mutableStateOf(true) }
    var selectedSection by remember { mutableStateOf(PersonaSettingsSection.Context) }
    val imagePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
        onResult = onAvatarChange
    )
    val sessionHeaderImagePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
        onResult = onSessionHeaderAvatarChange
    )
    val backgroundPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
        onResult = onCustomBackgroundChange
    )

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.86f),
        color = AppBackground,
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Close settings",
                        tint = AppTextPrimary
                    )
                }
                Text(
                    text = "Persona Settings",
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onSave) {
                    Text(
                        text = "Save",
                        color = AppAccentSoft,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            PersonaSettingsSectionTabs(
                selected = selectedSection,
                onSelect = { selectedSection = it }
            )

            when (selectedSection) {
                PersonaSettingsSection.Context -> {
                    GroupEditor(
                        members = groupMembers,
                        activeMemberId = activeMemberId,
                        responseRounds = responseRounds,
                        onSelectMember = onSelectMember,
                        onAddMember = onAddMember,
                        onRemoveMember = onRemoveMember,
                        onResponseRoundsChange = onResponseRoundsChange
                    )

                    InstructionSection(
                        persona = persona,
                        storyLore = storyLore,
                        advancedExpanded = instructionPromptExpanded,
                        onAdvancedExpandedChange = { instructionPromptExpanded = it },
                        onModeChange = onInstructionModeChange,
                        onBeginnerRoleChange = onBeginnerRoleChange,
                        onBeginnerStyleChange = onBeginnerStyleChange,
                        onBeginnerLimitsChange = onBeginnerLimitsChange,
                        onStoryLoreChange = onStoryLoreChange,
                        onAdvancedPromptChange = onPromptChange
                    )

                    SessionMemoryToggle(
                        enabled = memoryEnabled,
                        onEnabledChange = onMemoryEnabledChange
                    )

                    SessionLevelSystem(
                        enabled = levelSystemEnabled,
                        xp = levelXp,
                        onEnabledChange = onLevelSystemEnabledChange
                    )
                }

                PersonaSettingsSection.Ui -> {
                    if (showSessionHeaderControls) {
                        Text(
                            text = "Session header",
                            color = AppTextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                        Text(
                            text = "Changes the group name and top avatar without editing any AI member.",
                            color = AppTextSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        SessionHeaderAvatarEditor(
                            fallbackPersona = persona,
                            avatarUri = sessionHeaderAvatarUri,
                            avatarScale = sessionHeaderAvatarScale,
                            avatarOffsetX = sessionHeaderAvatarOffsetX,
                            avatarOffsetY = sessionHeaderAvatarOffsetY,
                            onUpload = { sessionHeaderImagePicker.launch(arrayOf("image/*")) },
                            onTransform = onSessionHeaderAvatarTransform
                        )

                        PersonaTextField(
                            label = "Session name",
                            value = sessionHeaderName,
                            onValueChange = onSessionHeaderNameChange,
                            helper = "This only changes the group header.",
                            placeholder = groupMembers.joinToString(" + ") {
                                it.persona.displayName.ifBlank { "AI" }
                            },
                            counter = "${sessionHeaderName.length}/72",
                            singleLine = true
                        )

                        Text(
                            text = "Active AI",
                            color = AppTextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    AvatarEditor(
                        persona = persona,
                        onUpload = { imagePicker.launch(arrayOf("image/*")) },
                        onTransform = onAvatarTransform
                    )

                    PersonaTextField(
                        label = "Display name",
                        value = persona.displayName,
                        onValueChange = onNameChange,
                        helper = "This is how your AI will appear in chats.",
                        counter = "${persona.displayName.length}/32",
                        singleLine = true
                    )

                    BackgroundOptions(
                        selected = background,
                        onBackgroundChange = onBackgroundChange,
                        onPickCustomBackground = { backgroundPicker.launch(arrayOf("image/*")) }
                    )
                }

                PersonaSettingsSection.ApiVault -> {
                    MoreOptions(
                        expanded = true,
                        showHeader = false,
                        showBackground = false,
                        persona = persona,
                        background = background,
                        activeApiKeyLabel = activeApiKeyLabel,
                        tavilyApiKeyLabel = tavilyApiKeyLabel,
                        rule34UserIdLabel = rule34UserIdLabel,
                        rule34ApiKeyLabel = rule34ApiKeyLabel,
                        elevenLabsApiKeyLabel = elevenLabsApiKeyLabel,
                        elevenLabsVoiceIdLabel = elevenLabsVoiceIdLabel,
                        elevenLabsModelIdLabel = elevenLabsModelIdLabel,
                        quotaUsage = quotaUsage,
                        dailyRequestLimit = dailyRequestLimit,
                        onToggle = onToggleMore,
                        onVendorChange = onVendorChange,
                        onModelChange = onModelChange,
                        onSafetyLevelChange = onSafetyLevelChange,
                        onThinkingEffortChange = onThinkingEffortChange,
                        onTemperatureChange = onTemperatureChange,
                        onBackgroundChange = onBackgroundChange,
                        onPickCustomBackground = { backgroundPicker.launch(arrayOf("image/*")) },
                        onEditApiKey = onEditApiKey,
                        onClearApiKey = onClearApiKey,
                        onEditTavilyKey = onEditTavilyKey,
                        onClearTavilyKey = onClearTavilyKey,
                        onEditRule34UserId = onEditRule34UserId,
                        onClearRule34UserId = onClearRule34UserId,
                        onEditRule34ApiKey = onEditRule34ApiKey,
                        onClearRule34ApiKey = onClearRule34ApiKey,
                        onEditElevenLabsKey = onEditElevenLabsKey,
                        onClearElevenLabsKey = onClearElevenLabsKey,
                        onEditElevenLabsVoiceId = onEditElevenLabsVoiceId,
                        onClearElevenLabsVoiceId = onClearElevenLabsVoiceId,
                        onEditElevenLabsModelId = onEditElevenLabsModelId,
                        onClearElevenLabsModelId = onClearElevenLabsModelId
                    )
                }
            }

            TextButton(
                onClick = onDeleteSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 18.dp)
            ) {
                Text(
                    text = "Delete this session",
                    color = Color(0xFFFF7E8B)
                )
            }
        }
    }
}

@Composable
private fun PersonaSettingsSectionTabs(
    selected: PersonaSettingsSection,
    onSelect: (PersonaSettingsSection) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 12.dp)
    ) {
        PersonaSettingsSection.entries.forEach { section ->
            val isSelected = selected == section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(section) }
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = section.label,
                    color = if (isSelected) AppAccentSoft else AppTextPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(if (isSelected) AppAccent else AppStroke.copy(alpha = 0.45f))
                )
            }
        }
    }
}

@Composable
private fun GroupEditor(
    members: List<GroupMember>,
    activeMemberId: String,
    responseRounds: Int,
    onSelectMember: (String) -> Unit,
    onAddMember: () -> Unit,
    onRemoveMember: (String) -> Unit,
    onResponseRoundsChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(top = 10.dp, bottom = 18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AI MEMBERS (${members.size}/4)",
                color = AppTextPrimary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f)
            )
        }
        Surface(
            color = AppSurface,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, AppStroke),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            Column {
                members.forEachIndexed { index, member ->
                    GroupMemberRow(
                        member = member,
                        selected = member.id == activeMemberId,
                        canRemove = members.size > 1,
                        onSelect = { onSelectMember(member.id) },
                        onRemove = { onRemoveMember(member.id) }
                    )
                    if (index != members.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(AppStroke.copy(alpha = 0.48f))
                        )
                    }
                }
                if (members.size < 4) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(AppStroke.copy(alpha = 0.48f))
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onAddMember)
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = AppAccentSoft,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add AI member",
                            color = AppAccentSoft,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        Text(
            text = "AI TURNS",
            color = AppTextPrimary,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 18.dp)
        )
        Slider(
            value = responseRounds.toFloat(),
            onValueChange = { onResponseRoundsChange(it.toInt().coerceIn(1, 3)) },
            valueRange = 1f..3f,
            steps = 1,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Each turn picks one AI to answer. Multi-AI chats rotate speakers before your next turn.",
            color = AppTextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun GroupMemberRow(
    member: GroupMember,
    selected: Boolean,
    canRemove: Boolean,
    onSelect: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(persona = member.persona, size = 38)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.persona.displayName,
                color = if (selected) AppAccentSoft else AppTextPrimary,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2ECC71))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Online",
                    color = AppTextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        if (canRemove) {
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Remove AI",
                    tint = Color(0xFFFFA0AA),
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = null,
                tint = AppTextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun SessionHeaderAvatarEditor(
    fallbackPersona: PersonaUiState,
    avatarUri: android.net.Uri?,
    avatarScale: Float,
    avatarOffsetX: Float,
    avatarOffsetY: Float,
    onUpload: () -> Unit,
    onTransform: (Float, Float, Float) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 20.dp)
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .pointerInput(avatarUri) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            onTransform(zoom, pan.x, pan.y)
                        }
                    }
                    .background(
                        Brush.radialGradient(
                            colors = listOf(AppAccentSoft.copy(alpha = 0.45f), AppSurface2)
                        )
                    )
                    .border(3.dp, AppAccentSoft, CircleShape)
                    .padding(4.dp)
            ) {
                SessionHeaderAvatar(
                    fallbackPersona = fallbackPersona,
                    avatarUri = avatarUri,
                    avatarScale = avatarScale,
                    avatarOffsetX = avatarOffsetX,
                    avatarOffsetY = avatarOffsetY,
                    size = 142,
                    modifier = Modifier.fillMaxSize()
                )
            }
            IconButton(
                onClick = onUpload,
                modifier = Modifier
                    .offset(x = 2.dp, y = 2.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AppSurface2)
                    .border(1.dp, AppStroke, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Upload,
                    contentDescription = "Upload session avatar",
                    tint = AppAccentSoft
                )
            }
        }

        Button(
            onClick = onUpload,
            colors = ButtonDefaults.buttonColors(containerColor = AppSurface),
            border = BorderStroke(1.dp, AppStroke),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
                .height(54.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Upload,
                contentDescription = null,
                tint = AppAccentSoft
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Upload session avatar",
                color = AppAccentSoft,
                style = MaterialTheme.typography.labelLarge
            )
        }
        Text(
            text = "JPG, PNG or WebP. Pinch and drag to crop.",
            color = AppTextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun AvatarEditor(
    persona: PersonaUiState,
    onUpload: () -> Unit,
    onTransform: (Float, Float, Float) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 20.dp)
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .pointerInput(persona.avatarUri) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            onTransform(zoom, pan.x, pan.y)
                        }
                    }
                    .background(
                        Brush.radialGradient(
                            colors = listOf(AppAccentSoft.copy(alpha = 0.45f), AppSurface2)
                        )
                    )
                    .border(3.dp, AppAccentSoft, CircleShape)
                    .padding(4.dp)
            ) {
                Avatar(
                    persona = persona,
                    size = 142,
                    modifier = Modifier.fillMaxSize()
                )
            }
            IconButton(
                onClick = onUpload,
                modifier = Modifier
                    .offset(x = 2.dp, y = 2.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AppSurface2)
                    .border(1.dp, AppStroke, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Upload,
                    contentDescription = "Upload avatar",
                    tint = AppAccentSoft
                )
            }
        }

        Button(
            onClick = onUpload,
            colors = ButtonDefaults.buttonColors(containerColor = AppSurface),
            border = BorderStroke(1.dp, AppStroke),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
                .height(54.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Upload,
                contentDescription = null,
                tint = AppAccentSoft
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Upload avatar",
                color = AppAccentSoft,
                style = MaterialTheme.typography.labelLarge
            )
        }
        Text(
            text = "JPG, PNG or WebP. Pinch and drag to crop.",
            color = AppTextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun PersonaTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    helper: String,
    placeholder: String? = null,
    counter: String? = null,
    singleLine: Boolean,
    minLines: Int = 1
) {
    Column(modifier = Modifier.padding(bottom = 18.dp)) {
        val hasHeader = label.isNotBlank() || counter != null
        if (hasHeader) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    color = AppTextPrimary,
                    style = MaterialTheme.typography.labelLarge
                )
                counter?.let {
                    Text(
                        text = it,
                        color = AppTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            minLines = minLines,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (hasHeader) 8.dp else 0.dp),
            placeholder = placeholder?.let { placeholderText ->
                {
                    Text(
                        text = placeholderText,
                        color = AppTextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AppTextPrimary,
                unfocusedTextColor = AppTextPrimary,
                focusedBorderColor = AppAccent.copy(alpha = 0.7f),
                unfocusedBorderColor = AppStroke,
                cursorColor = AppAccent,
                focusedContainerColor = AppSurface,
                unfocusedContainerColor = AppSurface
            )
        )
        Text(
            text = helper,
            color = AppTextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun InstructionSection(
    persona: PersonaUiState,
    storyLore: String,
    advancedExpanded: Boolean,
    onAdvancedExpandedChange: (Boolean) -> Unit,
    onModeChange: (InstructionMode) -> Unit,
    onBeginnerRoleChange: (String) -> Unit,
    onBeginnerStyleChange: (String) -> Unit,
    onBeginnerLimitsChange: (String) -> Unit,
    onStoryLoreChange: (String) -> Unit,
    onAdvancedPromptChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 18.dp)) {
        Text(
            text = "Instruction mode",
            color = AppTextPrimary,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            InstructionMode.entries.forEach { mode ->
                ModeChoiceChip(
                    label = mode.label,
                    selected = persona.instructionMode == mode,
                    onClick = { onModeChange(mode) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        PersonaTextField(
            label = "Story lore",
            value = storyLore,
            onValueChange = onStoryLoreChange,
            helper = "Shared by every AI in this session: its canon, rules, locations, factions, and how the world works.",
            placeholder = "Describe the world and the rules that govern it.",
            counter = "${storyLore.length}/16000",
            singleLine = false,
            minLines = 4
        )

        AnimatedVisibility(visible = persona.instructionMode == InstructionMode.Beginner) {
            Column(modifier = Modifier.padding(top = 14.dp)) {
                PersonaTextField(
                    label = "Role",
                    value = persona.beginnerRole,
                    onValueChange = onBeginnerRoleChange,
                    helper = "Who this persona is, what they know, and how they relate to you.",
                    placeholder = "Who this persona is, what they know, and how they relate to you.",
                    singleLine = false,
                    minLines = 2
                )
                PersonaTextField(
                    label = "Style",
                    value = persona.beginnerStyle,
                    onValueChange = onBeginnerStyleChange,
                    helper = "Tone, message length, speaking quirks, and roleplay texture.",
                    placeholder = "Tone, message length, speaking quirks, and roleplay texture.",
                    singleLine = false,
                    minLines = 2
                )
                PersonaTextField(
                    label = "Limits",
                    value = persona.beginnerLimits,
                    onValueChange = onBeginnerLimitsChange,
                    helper = "Boundaries, canon rules, things to avoid, or must-follow details.",
                    singleLine = false,
                    minLines = 2
                )
            }
        }

        AnimatedVisibility(visible = persona.instructionMode == InstructionMode.Advanced) {
            Column(modifier = Modifier.padding(top = 14.dp)) {
                CollapsibleInstructionPrompt(
                    value = persona.instructionPrompt,
                    expanded = advancedExpanded,
                    onExpandedChange = onAdvancedExpandedChange,
                    onValueChange = onAdvancedPromptChange
                )
            }
        }
    }
}

@Composable
private fun ModeChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) AppAccentDim else AppSurface,
        border = BorderStroke(1.dp, if (selected) AppAccent else AppStroke),
        modifier = modifier
            .height(46.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (selected) AppAccentSoft else AppTextPrimary,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CollapsibleInstructionPrompt(
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 18.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onExpandedChange(!expanded) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Instruction Prompt",
                color = AppTextPrimary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (expanded) "Minimize" else "Expand",
                color = AppAccentSoft,
                style = MaterialTheme.typography.labelLarge
            )
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = AppTextSecondary,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .graphicsLayer(rotationZ = if (expanded) 180f else 0f)
            )
        }

        AnimatedVisibility(visible = expanded) {
            PersonaTextField(
                label = "",
                value = value,
                onValueChange = onValueChange,
                helper = "Shape the tone, boundaries, and role for this persona.",
                singleLine = false,
                minLines = 5
            )
        }

        AnimatedVisibility(visible = !expanded) {
            Surface(
                color = AppSurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, AppStroke),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clickable { onExpandedChange(true) }
            ) {
                Text(
                    text = value.ifBlank { "No instruction prompt yet." },
                    color = if (value.isBlank()) AppTextSecondary else AppTextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}

@Composable
private fun SessionMemoryToggle(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit
) {
    Surface(
        color = AppSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppStroke),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Read global memory",
                    color = AppTextPrimary,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "Let this chat use the shared memory block.",
                    color = AppTextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange
            )
        }
    }
}

@Composable
private fun SessionLevelSystem(
    enabled: Boolean,
    xp: Int,
    onEnabledChange: (Boolean) -> Unit
) {
    val state = sessionLevelState(xp)
    Surface(
        color = AppSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppStroke),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Relationship level",
                        color = AppTextPrimary,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "10 XP per text message. Progress is unique to this session.",
                        color = AppTextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange
                )
            }

            AnimatedVisibility(visible = enabled) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Level ${state.level}",
                            color = AppAccentSoft,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "${state.xp}/1500 XP",
                            color = AppTextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        text = state.label,
                        color = AppTextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(AppSurface2)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(state.progress)
                                .fillMaxHeight()
                                .background(AppAccent)
                        )
                    }
                    Text(
                        text = if (state.level >= 10) {
                            "Maximum level reached"
                        } else {
                            "${state.nextLevelXp - state.xp} XP to Level ${state.level + 1}"
                        },
                        color = AppTextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 7.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TraitChips(traits: List<String>) {
    val icons = listOf(
        Icons.Rounded.FavoriteBorder,
        Icons.Rounded.StarBorder,
        Icons.Rounded.Search,
        Icons.Rounded.Shield
    )
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 14.dp)
    ) {
        items(traits.zip(icons)) { (trait, icon) ->
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = AppSurface,
                border = BorderStroke(1.dp, AppStroke)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = AppAccentSoft,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = trait,
                        color = AppAccentSoft,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun MoreOptions(
    expanded: Boolean,
    showHeader: Boolean = true,
    showBackground: Boolean = true,
    persona: PersonaUiState,
    background: ChatBackground,
    activeApiKeyLabel: String?,
    tavilyApiKeyLabel: String?,
    rule34UserIdLabel: String?,
    rule34ApiKeyLabel: String?,
    elevenLabsApiKeyLabel: String?,
    elevenLabsVoiceIdLabel: String?,
    elevenLabsModelIdLabel: String?,
    quotaUsage: QuotaUsageState,
    dailyRequestLimit: Int?,
    onToggle: () -> Unit,
    onVendorChange: (ApiVendor) -> Unit,
    onModelChange: (String) -> Unit,
    onSafetyLevelChange: (SafetyLevel) -> Unit,
    onThinkingEffortChange: (GeminiThinkingEffort) -> Unit,
    onTemperatureChange: (Float) -> Unit,
    onBackgroundChange: (ChatBackground) -> Unit,
    onPickCustomBackground: () -> Unit,
    onEditApiKey: () -> Unit,
    onClearApiKey: () -> Unit,
    onEditTavilyKey: () -> Unit,
    onClearTavilyKey: () -> Unit,
    onEditRule34UserId: () -> Unit,
    onClearRule34UserId: () -> Unit,
    onEditRule34ApiKey: () -> Unit,
    onClearRule34ApiKey: () -> Unit,
    onEditElevenLabsKey: () -> Unit,
    onClearElevenLabsKey: () -> Unit,
    onEditElevenLabsVoiceId: () -> Unit,
    onClearElevenLabsVoiceId: () -> Unit,
    onEditElevenLabsModelId: () -> Unit,
    onClearElevenLabsModelId: () -> Unit
) {
    Surface(
        color = AppSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppStroke),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (showHeader) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggle),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "More options",
                        color = AppTextPrimary,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = AppTextSecondary,
                        modifier = Modifier.alpha(if (expanded) 1f else 0.82f)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = if (showHeader) 16.dp else 0.dp)) {
                    VendorDropdown(
                        selected = persona.vendor,
                        onVendorChange = onVendorChange
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ModelDropdown(
                        vendor = persona.vendor,
                        selected = persona.model,
                        onModelChange = onModelChange
                    )
                    if (persona.vendor == ApiVendor.Google) {
                        Spacer(modifier = Modifier.height(12.dp))
                        ThinkingEffortDropdown(
                            selected = persona.thinkingEffort,
                            onThinkingEffortChange = onThinkingEffortChange
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    SafetyDropdown(
                        selected = persona.safetyLevel,
                        enabled = persona.vendor == ApiVendor.Google,
                        onSafetyLevelChange = onSafetyLevelChange
                    )
                    Text(
                        text = "Temperature ${"%.1f".format(persona.temperature)}",
                        color = AppTextPrimary,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Slider(
                        value = persona.temperature,
                        onValueChange = onTemperatureChange,
                        valueRange = 0f..2f,
                        steps = 19,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (showBackground) {
                        BackgroundOptions(
                            selected = background,
                            onBackgroundChange = onBackgroundChange,
                            onPickCustomBackground = onPickCustomBackground
                        )
                    }
                    ApiKeySummary(
                        activeApiKeyLabel = activeApiKeyLabel,
                        tavilyApiKeyLabel = tavilyApiKeyLabel,
                        rule34UserIdLabel = rule34UserIdLabel,
                        rule34ApiKeyLabel = rule34ApiKeyLabel,
                        elevenLabsApiKeyLabel = elevenLabsApiKeyLabel,
                        elevenLabsVoiceIdLabel = elevenLabsVoiceIdLabel,
                        elevenLabsModelIdLabel = elevenLabsModelIdLabel,
                        quotaUsage = quotaUsage,
                        dailyRequestLimit = dailyRequestLimit,
                        onEditApiKey = onEditApiKey,
                        onClearApiKey = onClearApiKey,
                        onEditTavilyKey = onEditTavilyKey,
                        onClearTavilyKey = onClearTavilyKey,
                        onEditRule34UserId = onEditRule34UserId,
                        onClearRule34UserId = onClearRule34UserId,
                        onEditRule34ApiKey = onEditRule34ApiKey,
                        onClearRule34ApiKey = onClearRule34ApiKey,
                        onEditElevenLabsKey = onEditElevenLabsKey,
                        onClearElevenLabsKey = onClearElevenLabsKey,
                        onEditElevenLabsVoiceId = onEditElevenLabsVoiceId,
                        onClearElevenLabsVoiceId = onClearElevenLabsVoiceId,
                        onEditElevenLabsModelId = onEditElevenLabsModelId,
                        onClearElevenLabsModelId = onClearElevenLabsModelId
                    )
                }
            }
        }
    }
}

@Composable
private fun BackgroundOptions(
    selected: ChatBackground,
    onBackgroundChange: (ChatBackground) -> Unit,
    onPickCustomBackground: () -> Unit
) {
    Text(
        text = "Background",
        color = AppTextPrimary,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )

    BackgroundPreview(background = selected)

    val presets = listOf(
        "Darkmode" to ChatBackground.DarkMode,
        "Black" to ChatBackground.PureBlack,
        "Background white" to ChatBackground.PresetWhite,
        "Background black" to ChatBackground.PresetBlack
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 10.dp)
    ) {
        items(presets) { (label, background) ->
            BackgroundChoice(
                label = label,
                selected = selected.sameChoice(background),
                onClick = { onBackgroundChange(background) }
            )
        }
        item {
            BackgroundChoice(
                label = "Upload photo",
                selected = selected is ChatBackground.CustomImage,
                onClick = onPickCustomBackground
            )
        }
    }
}

@Composable
private fun BackgroundPreview(background: ChatBackground) {
    Surface(
        color = AppSurface2,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, AppStroke),
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ChatBackgroundLayer(background = background)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.Black.copy(alpha = 0.48f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Crop preview",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun BackgroundChoice(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (selected) AppAccentDim else AppSurface2,
        border = BorderStroke(1.dp, if (selected) AppAccent else AppStroke),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = if (selected) AppAccentSoft else AppTextPrimary,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

private fun ChatBackground.sameChoice(other: ChatBackground): Boolean {
    return this::class == other::class
}

private fun ChatSession.groupTitle(): String {
    val members = members.ifEmpty { listOf(GroupMember(persona = persona)) }
    return members.joinToString(" + ") { it.persona.displayName.ifBlank { "AI" } }
}

private fun ChatSession.displayTitle(): String {
    return title.ifBlank { groupTitle().ifBlank { "Unnamed chat" } }
}

private fun ChatSession.matchesSessionQuery(query: String): Boolean {
    val normalized = query.lowercase(Locale.getDefault())
    if (title.lowercase(Locale.getDefault()).contains(normalized)) return true
    if (groupTitle().lowercase(Locale.getDefault()).contains(normalized)) return true
    if (preview.lowercase(Locale.getDefault()).contains(normalized)) return true
    return messages.any { message ->
        message.content.lowercase(Locale.getDefault()).contains(normalized) ||
            message.speakerName.orEmpty().lowercase(Locale.getDefault()).contains(normalized)
    }
}

private fun ProjectUiState.matchesProjectQuery(query: String): Boolean {
    val normalized = query.lowercase(Locale.getDefault())
    return name.lowercase(Locale.getDefault()).contains(normalized) ||
        description.lowercase(Locale.getDefault()).contains(normalized) ||
        instruction.lowercase(Locale.getDefault()).contains(normalized)
}

@Composable
private fun ApiKeySummary(
    activeApiKeyLabel: String?,
    tavilyApiKeyLabel: String?,
    rule34UserIdLabel: String?,
    rule34ApiKeyLabel: String?,
    elevenLabsApiKeyLabel: String?,
    elevenLabsVoiceIdLabel: String?,
    elevenLabsModelIdLabel: String?,
    quotaUsage: QuotaUsageState,
    dailyRequestLimit: Int?,
    onEditApiKey: () -> Unit,
    onClearApiKey: () -> Unit,
    onEditTavilyKey: () -> Unit,
    onClearTavilyKey: () -> Unit,
    onEditRule34UserId: () -> Unit,
    onClearRule34UserId: () -> Unit,
    onEditRule34ApiKey: () -> Unit,
    onClearRule34ApiKey: () -> Unit,
    onEditElevenLabsKey: () -> Unit,
    onClearElevenLabsKey: () -> Unit,
    onEditElevenLabsVoiceId: () -> Unit,
    onClearElevenLabsVoiceId: () -> Unit,
    onEditElevenLabsModelId: () -> Unit,
    onClearElevenLabsModelId: () -> Unit
) {
    Text(
        text = "API keys",
        color = AppTextPrimary,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(top = 14.dp, bottom = 8.dp)
    )
    Surface(
        color = AppSurface2,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, AppStroke),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            ApiKeySlot(
                label = "Model key",
                keyLabel = activeApiKeyLabel,
                emptyText = "No key saved",
                onEdit = onEditApiKey,
                onClear = onClearApiKey
            )
            Spacer(modifier = Modifier.height(8.dp))
            ApiKeySlot(
                label = "Tavily search key",
                keyLabel = tavilyApiKeyLabel,
                emptyText = "No Tavily key saved",
                onEdit = onEditTavilyKey,
                onClear = onClearTavilyKey
            )
            Spacer(modifier = Modifier.height(8.dp))
            ApiKeySlot(
                label = "Rule34 User ID",
                keyLabel = rule34UserIdLabel,
                emptyText = "No Rule34 User ID saved",
                onEdit = onEditRule34UserId,
                onClear = onClearRule34UserId
            )
            Spacer(modifier = Modifier.height(8.dp))
            ApiKeySlot(
                label = "Rule34 API Key",
                keyLabel = rule34ApiKeyLabel,
                emptyText = "No Rule34 API key saved",
                onEdit = onEditRule34ApiKey,
                onClear = onClearRule34ApiKey
            )
            Spacer(modifier = Modifier.height(8.dp))
            ApiKeySlot(
                label = "ElevenLabs API key",
                keyLabel = elevenLabsApiKeyLabel,
                emptyText = "No ElevenLabs key saved",
                onEdit = onEditElevenLabsKey,
                onClear = onClearElevenLabsKey
            )
            Spacer(modifier = Modifier.height(8.dp))
            ApiKeySlot(
                label = "ElevenLabs voice ID",
                keyLabel = elevenLabsVoiceIdLabel,
                emptyText = "No ElevenLabs voice ID saved",
                onEdit = onEditElevenLabsVoiceId,
                onClear = onClearElevenLabsVoiceId
            )
            Spacer(modifier = Modifier.height(8.dp))
            ApiKeySlot(
                label = "ElevenLabs model",
                keyLabel = elevenLabsModelIdLabel,
                emptyText = "eleven_multilingual_v2",
                onEdit = onEditElevenLabsModelId,
                onClear = onClearElevenLabsModelId
            )
            QuotaMonitor(
                usage = quotaUsage,
                dailyRequestLimit = dailyRequestLimit
            )
        }
    }
}

@Composable
private fun ApiKeySlot(
    label: String,
    keyLabel: String?,
    emptyText: String,
    onEdit: () -> Unit,
    onClear: () -> Unit
) {
    Surface(
        color = AppBackground.copy(alpha = 0.32f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, AppStroke),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                color = AppTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = keyLabel ?: emptyText,
                    color = AppTextPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                if (keyLabel != null) {
                    Surface(
                        color = AppAccentDim,
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            text = "Saved",
                            color = AppAccentSoft,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Row(modifier = Modifier.padding(top = 4.dp)) {
                TextButton(onClick = onEdit) {
                    Text(
                        text = if (keyLabel == null) "Add key" else "Replace key",
                        color = AppAccentSoft
                    )
                }
                if (keyLabel != null) {
                    TextButton(onClick = onClear) {
                        Text(
                            text = "Clear",
                            color = Color(0xFFFFA0AA)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuotaMonitor(
    usage: QuotaUsageState,
    dailyRequestLimit: Int?
) {
    var resetText by remember { mutableStateOf(formatPacificResetCountdown()) }
    LaunchedEffect(Unit) {
        while (true) {
            resetText = formatPacificResetCountdown()
            delay(1_000)
        }
    }
    val requestText = if (dailyRequestLimit == null) {
        "${usage.requestsToday} requests today"
    } else {
        "${usage.requestsToday} / $dailyRequestLimit RPD"
    }
    Surface(
        color = AppBackground.copy(alpha = 0.5f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, AppStroke),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Local quota monitor",
                color = AppTextPrimary,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = requestText,
                color = AppAccentSoft,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Tokens today ${usage.totalTokensToday} · Last ${usage.lastTotalTokens}",
                color = AppTextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = resetText,
                color = AppTextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

private fun formatPacificResetCountdown(): String {
    val localZone = ZoneId.systemDefault()
    val pacificZone = ZoneId.of("America/Los_Angeles")
    val nowLocal = ZonedDateTime.now(localZone)
    val nextPacificReset = nowLocal
        .withZoneSameInstant(pacificZone)
        .toLocalDate()
        .plusDays(1)
        .atStartOfDay(pacificZone)
    val localReset = nextPacificReset.withZoneSameInstant(localZone)
    val duration = Duration.between(nowLocal, localReset).coerceAtLeast(Duration.ZERO)
    val hours = duration.toHours()
    val minutes = duration.minusHours(hours).toMinutes()
    val seconds = duration.minusHours(hours).minusMinutes(minutes).seconds
    val time = localReset.format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
    return "RPD reset $time local · %02d:%02d:%02d".format(hours, minutes, seconds)
}

@Composable
private fun AppSettingsDialog(
    selectedName: AppNameChoice,
    selectedIcon: AppIconChoice,
    globalMemoryBlock: String,
    onNameChange: (AppNameChoice) -> Unit,
    onIconChange: (AppIconChoice) -> Unit,
    onGlobalMemoryChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppSurface,
        titleContentColor = AppTextPrimary,
        textContentColor = AppTextSecondary,
        title = {
            Text(
                text = "App settings",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "App name",
                    color = AppTextPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                AppNameChoiceRow(
                    choice = AppNameChoice.Zora,
                    selected = selectedName == AppNameChoice.Zora,
                    onClick = { onNameChange(AppNameChoice.Zora) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                AppNameChoiceRow(
                    choice = AppNameChoice.SanLoVerse,
                    selected = selectedName == AppNameChoice.SanLoVerse,
                    onClick = { onNameChange(AppNameChoice.SanLoVerse) }
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "App icon",
                    color = AppTextPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                AppIconChoiceRow(
                    choice = AppIconChoice.Minimalist,
                    selected = selectedIcon == AppIconChoice.Minimalist,
                    onClick = { onIconChange(AppIconChoice.Minimalist) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                AppIconChoiceRow(
                    choice = AppIconChoice.Waifu,
                    selected = selectedIcon == AppIconChoice.Waifu,
                    onClick = { onIconChange(AppIconChoice.Waifu) }
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Memory",
                    color = AppTextPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Stored ${globalMemoryBlock.length} / 64,000 chars. Up to 24,000 chars are injected per request.",
                    color = AppTextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = globalMemoryBlock,
                    onValueChange = onGlobalMemoryChange,
                    minLines = 5,
                    maxLines = 9,
                    placeholder = {
                        Text(
                            text = "Things every chat may remember, such as your preferences, recurring roleplay details, or writing style.",
                            color = AppTextSecondary
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppTextPrimary,
                        unfocusedTextColor = AppTextPrimary,
                        focusedBorderColor = AppAccent.copy(alpha = 0.7f),
                        unfocusedBorderColor = AppStroke,
                        cursorColor = AppAccent,
                        focusedContainerColor = AppSurface2,
                        unfocusedContainerColor = AppSurface2
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Sessions only read this when their memory switch is on.",
                    color = AppTextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Done", color = AppAccentSoft)
            }
        }
    )
}

@Composable
private fun AboutAppDialog(
    appVersionLabel: String,
    onDismiss: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppSurface,
        titleContentColor = AppTextPrimary,
        textContentColor = AppTextSecondary,
        title = {
            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = appVersionLabel,
                    color = AppTextPrimary,
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Created by Phan Chí Vỹ.",
                    color = AppTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "API key setup",
                    color = AppTextPrimary,
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                AboutStep("1. Open the provider dashboard for the model you want to use.")
                AboutStep("2. Create or reveal an API key in that provider account.")
                AboutStep("3. Copy the key, then open Persona settings in this app.")
                AboutStep("4. Pick the matching API vendor and model.")
                AboutStep("5. Tap API keys > Add key, paste it, and save.")
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Provider dashboards",
                    color = AppTextPrimary,
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                AboutLinkRow(
                    label = "Google AI Studio",
                    detail = "Gemini API key",
                    url = "https://aistudio.google.com/app/apikey",
                    onOpen = uriHandler::openUri
                )
                AboutLinkRow(
                    label = "OpenAI Platform",
                    detail = "GPT API keys",
                    url = "https://platform.openai.com/api-keys",
                    onOpen = uriHandler::openUri
                )
                AboutLinkRow(
                    label = "Anthropic Console",
                    detail = "Claude API keys",
                    url = "https://console.anthropic.com/settings/keys",
                    onOpen = uriHandler::openUri
                )
                AboutLinkRow(
                    label = "xAI Console",
                    detail = "Grok API keys",
                    url = "https://console.x.ai/team/default/api-keys",
                    onOpen = uriHandler::openUri
                )
                AboutLinkRow(
                    label = "Mistral Console",
                    detail = "Mistral/Mixtral API keys",
                    url = "https://console.mistral.ai/api-keys",
                    onOpen = uriHandler::openUri
                )
                AboutLinkRow(
                    label = "Tavily Dashboard",
                    detail = "Web search API key",
                    url = "https://app.tavily.com/home",
                    onOpen = uriHandler::openUri
                )
                AboutLinkRow(
                    label = "ElevenLabs",
                    detail = "TTS API key, voice ID, and model ID",
                    url = "https://elevenlabs.io/app/settings/api-keys",
                    onOpen = uriHandler::openUri
                )
                AboutLinkRow(
                    label = "Rule34 account options",
                    detail = "Rule34 User ID and API key",
                    url = "https://rule34.xxx/index.php?page=account&s=options",
                    onOpen = uriHandler::openUri
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Rule34 setup: sign in to Rule34, open account options, copy the numeric User ID and API key, then paste them into Persona settings > API keys. Rule34 image mode is adult-only.",
                    color = AppTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "ElevenLabs setup: create an API key, copy a Voice ID from your voices, then paste both into Persona settings > API keys. Some voices return 402 unless your ElevenLabs account has the required paid plan or subscription.",
                    color = AppTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Keep API keys private. Do not post them online or commit them to GitHub.",
                    color = Color(0xFFFFA0AA),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Done", color = AppAccentSoft)
            }
        }
    )
}

@Composable
private fun AboutStep(text: String) {
    Text(
        text = text,
        color = AppTextSecondary,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 3.dp)
    )
}

@Composable
private fun AboutLinkRow(
    label: String,
    detail: String,
    url: String,
    onOpen: (String) -> Unit
) {
    Surface(
        color = AppSurface2,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, AppStroke),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onOpen(url) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = AppTextPrimary,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = detail,
                    color = AppTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "Open",
                color = AppAccentSoft,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun AppNameChoiceRow(
    choice: AppNameChoice,
    selected: Boolean,
    onClick: () -> Unit
) {
    SettingsChoiceRow(
        selected = selected,
        onClick = onClick,
        leading = null,
        label = choice.label
    )
}

@Composable
private fun AppIconChoiceRow(
    choice: AppIconChoice,
    selected: Boolean,
    onClick: () -> Unit
) {
    SettingsChoiceRow(
        selected = selected,
        onClick = onClick,
        leading = {
            Image(
                painter = painterResource(id = choice.iconResource()),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        },
        label = choice.label
    )
}

@Composable
private fun SettingsChoiceRow(
    selected: Boolean,
    onClick: () -> Unit,
    leading: (@Composable () -> Unit)?,
    label: String
) {
    Surface(
        color = if (selected) AppAccentDim else AppSurface2,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (selected) AppAccent else AppStroke),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leading?.invoke()
            if (leading != null) {
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = label,
                color = AppTextPrimary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f)
            )
            if (selected) {
                Surface(
                    color = AppAccent,
                    shape = CircleShape,
                    modifier = Modifier.size(10.dp)
                ) {}
            }
        }
    }
}

private fun AppIconChoice.iconResource(): Int {
    return when (this) {
        AppIconChoice.Minimalist -> R.drawable.app_icon_minimalist
        AppIconChoice.Waifu -> R.drawable.app_icon_waifu
    }
}

private fun createCameraImageUri(context: android.content.Context): android.net.Uri {
    val directory = File(context.cacheDir, "camera").apply { mkdirs() }
    val imageFile = File.createTempFile("zora_camera_", ".jpg", directory)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

@Composable
private fun ApiKeyDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    secureInput: Boolean = true
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppSurface,
        title = {
            Text(
                text = title,
                color = AppTextPrimary,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    visualTransformation = if (secureInput) {
                        PasswordVisualTransformation()
                    } else {
                        VisualTransformation.None
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (secureInput) KeyboardType.Password else KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { onSave() }),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppTextPrimary,
                        unfocusedTextColor = AppTextPrimary,
                        focusedBorderColor = AppAccent.copy(alpha = 0.7f),
                        unfocusedBorderColor = AppStroke,
                        cursorColor = AppAccent,
                        focusedContainerColor = AppSurface2,
                        unfocusedContainerColor = AppSurface2
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Saved only on this device.",
                    color = AppTextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = value.isNotBlank()
            ) {
                Text(
                    text = "Save",
                    color = AppAccentSoft
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = AppTextSecondary
                )
            }
        }
    )
}

@Composable
private fun RateLimitRetryDialog(
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppSurface,
        title = {
            Text(
                text = "Model limit hit",
                color = AppTextPrimary,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = "The model returned a rate-limit or quota error. Retry the last message?",
                color = AppTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onRetry) {
                Text(text = "Retry", color = AppAccentSoft)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = AppTextSecondary)
            }
        }
    )
}

@Composable
private fun FullscreenImageViewer(
    uri: android.net.Uri,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.94f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter(uri),
            contentDescription = "Full image",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp)
        )
        Text(
            text = "Tap to close",
            color = Color.White.copy(alpha = 0.72f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        )
    }
}

@Composable
private fun VendorDropdown(
    selected: ApiVendor,
    onVendorChange: (ApiVendor) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    LabeledMenuBox(
        label = "API vendor",
        value = selected.label,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        ApiVendor.entries.forEach { vendor ->
            DropdownMenuItem(
                text = { Text(text = vendor.label, color = AppTextPrimary) },
                onClick = {
                    onVendorChange(vendor)
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun SafetyDropdown(
    selected: SafetyLevel,
    enabled: Boolean,
    onSafetyLevelChange: (SafetyLevel) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    LabeledMenuBox(
        label = "Safety level",
        value = if (enabled) selected.label else "Provider default",
        expanded = expanded && enabled,
        enabled = enabled,
        onExpandedChange = { expanded = it }
    ) {
        SafetyLevel.entries.forEach { level ->
            DropdownMenuItem(
                text = { Text(text = level.label, color = AppTextPrimary) },
                onClick = {
                    onSafetyLevelChange(level)
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun ThinkingEffortDropdown(
    selected: GeminiThinkingEffort,
    onThinkingEffortChange: (GeminiThinkingEffort) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    LabeledMenuBox(
        label = "Thinking level",
        value = selected.label,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        GeminiThinkingEffort.entries.forEach { effort ->
            DropdownMenuItem(
                text = { Text(text = effort.label, color = AppTextPrimary) },
                onClick = {
                    onThinkingEffortChange(effort)
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun ModelDropdown(
    vendor: ApiVendor,
    selected: String,
    onModelChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val models = vendor.modelOptions
    LabeledMenuBox(
        label = "Model",
        value = selected,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        models.forEach { model ->
            DropdownMenuItem(
                text = { Text(text = model, color = AppTextPrimary) },
                onClick = {
                    onModelChange(model)
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun LabeledMenuBox(
    label: String,
    value: String,
    expanded: Boolean,
    enabled: Boolean = true,
    onExpandedChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Text(
        text = label,
        color = AppTextPrimary,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Box {
        Surface(
            color = if (enabled) AppSurface2 else AppSurface2.copy(alpha = 0.55f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, AppStroke),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { onExpandedChange(true) }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    color = AppTextPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = "Choose model",
                    tint = AppTextSecondary
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(AppSurface2)
        ) {
            content()
        }
    }
}

@Composable
private fun SessionHeaderAvatar(
    fallbackPersona: PersonaUiState,
    avatarUri: android.net.Uri?,
    avatarScale: Float,
    avatarOffsetX: Float,
    avatarOffsetY: Float,
    size: Int,
    modifier: Modifier = Modifier
) {
    if (avatarUri == null) {
        Avatar(
            persona = fallbackPersona,
            size = size,
            modifier = modifier
        )
        return
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .aspectRatio(1f)
            .clip(CircleShape)
    ) {
        Image(
            painter = rememberAsyncImagePainter(avatarUri),
            contentDescription = "Session avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = avatarScale,
                    scaleY = avatarScale,
                    translationX = avatarOffsetX,
                    translationY = avatarOffsetY
                )
        )
    }
}

@Composable
private fun Avatar(
    persona: PersonaUiState,
    size: Int,
    modifier: Modifier = Modifier
) {
    val painter = if (persona.avatarUri != null) {
        rememberAsyncImagePainter(persona.avatarUri)
    } else {
        painterResource(id = R.drawable.default_avatar)
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .aspectRatio(1f)
            .clip(CircleShape)
    ) {
        Image(
            painter = painter,
            contentDescription = "${persona.displayName} avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = persona.avatarScale,
                    scaleY = persona.avatarScale,
                    translationX = persona.avatarOffsetX,
                    translationY = persona.avatarOffsetY
                )
        )
    }
}
