package com.yozora.aichat.ui

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
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.Upload
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.yozora.aichat.ui.chat.GroupMember
import com.yozora.aichat.ui.chat.InstructionMode
import com.yozora.aichat.ui.chat.PersonaUiState
import com.yozora.aichat.ui.chat.QuotaUsageState
import com.yozora.aichat.ui.chat.SafetyLevel
import com.yozora.aichat.ui.chat.TtsPreviewState
import com.yozora.aichat.ui.theme.AIChatTheme
import com.yozora.aichat.ui.theme.AppAccent
import com.yozora.aichat.ui.theme.AppAccentDim
import com.yozora.aichat.ui.theme.AppAccentSoft
import com.yozora.aichat.ui.theme.AppBackground
import com.yozora.aichat.ui.theme.AppStroke
import com.yozora.aichat.ui.theme.AppSurface
import com.yozora.aichat.ui.theme.AppSurface2
import com.yozora.aichat.ui.theme.AppTextPrimary
import com.yozora.aichat.ui.theme.AppTextSecondary
import androidx.core.content.FileProvider
import java.io.File
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

private const val APP_VERSION_NAME = "2.1.0"

@Composable
fun CompanionChatApp(
    viewModel: ChatViewModel = viewModel()
) {
    var viewedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var actionMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var exportTargetSessionId by remember { mutableStateOf<String?>(null) }
    var aboutDialogVisible by remember { mutableStateOf(false) }
    val appVersionLabel = "${viewModel.appNameChoice.label} v$APP_VERSION_NAME"
    val clipboardManager = LocalClipboardManager.current
    val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val targetId = exportTargetSessionId
        exportTargetSessionId = null
        if (uri != null && targetId != null) {
            viewModel.exportSessionToUri(targetId, uri)
        }
    }
    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importSessionFromUri(uri)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        ChatScreen(
            persona = viewModel.persona,
            groupMembers = viewModel.groupMembers,
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
            onOpenPersona = viewModel::openPersonaSheet
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
            activeSessionId = viewModel.activeSessionId,
            persona = viewModel.persona,
            appVersionLabel = appVersionLabel,
            onNewSession = viewModel::createSession,
            onSelectSession = viewModel::selectSession,
            onDeleteSession = viewModel::deleteSession,
            onExportSession = { session ->
                exportTargetSessionId = session.id
                exportLauncher.launch(viewModel.sessionExportFileName(session.id))
            },
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
                activeMemberId = viewModel.activeMemberId,
                responseRounds = viewModel.responseRounds,
                memoryEnabled = viewModel.memoryEnabled,
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
                onNameChange = viewModel::updatePersonaName,
                onInstructionModeChange = viewModel::updateInstructionMode,
                onBeginnerRoleChange = viewModel::updateBeginnerRole,
                onBeginnerStyleChange = viewModel::updateBeginnerStyle,
                onBeginnerLimitsChange = viewModel::updateBeginnerLimits,
                onPromptChange = viewModel::updatePersonaPrompt,
                onModelChange = viewModel::updatePersonaModel,
                onSafetyLevelChange = viewModel::updateSafetyLevel,
                onTemperatureChange = viewModel::updateTemperature,
                onAvatarChange = viewModel::updateAvatar,
                onAvatarTransform = viewModel::transformAvatar,
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
    onOpenPersona: () -> Unit
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
                isOnline = isOnline,
                onOpenSessions = onOpenSessions,
                onOpenPersona = onOpenPersona
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
                            onCreatePersona = onOpenPersona
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
                ChatInputBar(
                    draft = draft,
                    isSending = isSending,
                    attachedImageUris = attachedImageUris,
                    onDraftChange = onDraftChange,
                    onSend = {
                        focusManager.clearFocus()
                        onSend()
                    },
                    onOpenTools = { toolsSheetVisible = true },
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
    isOnline: Boolean,
    onOpenSessions: () -> Unit,
    onOpenPersona: () -> Unit
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
        Avatar(
            persona = persona,
            size = 54,
            modifier = Modifier.clickable(onClick = onOpenPersona)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = groupMembers.joinToString(" + ") { it.persona.displayName.ifBlank { "AI" } },
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
    activeSessionId: String,
    persona: PersonaUiState,
    appVersionLabel: String,
    onNewSession: () -> Unit,
    onSelectSession: (String) -> Unit,
    onDeleteSession: (String) -> Unit,
    onExportSession: (ChatSession) -> Unit,
    onImportSession: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onClose: () -> Unit
) {
    var deleteTarget by remember { mutableStateOf<ChatSession?>(null) }
    var sessionSearch by remember { mutableStateOf("") }
    val filteredSessions = remember(sessions, sessionSearch) {
        val query = sessionSearch.trim()
        if (query.isBlank()) {
            sessions
        } else {
            sessions.filter { session -> session.matchesSessionQuery(query) }
        }
    }

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
                    .height(58.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(persona = persona, size = 42)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Chats",
                        color = AppTextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = persona.displayName,
                        color = AppTextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "App settings",
                        tint = AppTextPrimary
                    )
                }
                IconButton(onClick = onOpenAbout) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "About app",
                        tint = AppTextPrimary
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

            Button(
                onClick = onNewSession,
                colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(top = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "New chat",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Surface(
                color = AppSurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, AppStroke),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(top = 10.dp)
                    .clickable(onClick = onImportSession)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.InsertDriveFile,
                        contentDescription = null,
                        tint = AppAccentSoft,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Import session",
                        color = AppAccentSoft,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            OutlinedTextField(
                value = sessionSearch,
                onValueChange = { sessionSearch = it },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = AppTextSecondary
                    )
                },
                placeholder = {
                    Text(text = "Search sessions", color = AppTextSecondary)
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSessions, key = { it.id }) { session ->
                    SessionRow(
                        session = session,
                        selected = session.id == activeSessionId,
                        onClick = { onSelectSession(session.id) },
                        onExport = { onExportSession(session) },
                        onDelete = { deleteTarget = session }
                    )
                }
            }
            Text(
                text = appVersionLabel,
                color = AppTextSecondary.copy(alpha = 0.78f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp)
            )
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
                    text = session.groupTitle().ifBlank { "This chat" },
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
}

@Composable
private fun SessionRow(
    session: ChatSession,
    selected: Boolean,
    onClick: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = if (selected) AppAccentDim.copy(alpha = 0.82f) else AppSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (selected) AppAccent.copy(alpha = 0.6f) else AppStroke),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                Icon(
                    imageVector = Icons.Rounded.ChatBubbleOutline,
                    contentDescription = null,
                    tint = if (selected) Color.White else AppAccentSoft,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.groupTitle().ifBlank { "Unnamed chat" },
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
            }
            Text(
                text = session.updatedAt,
                color = AppTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(onClick = onExport) {
                Icon(
                    imageVector = Icons.Rounded.Upload,
                    contentDescription = "Export session",
                    tint = AppAccentSoft,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Delete session",
                    tint = Color(0xFFFF8E9A),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyChatState(
    persona: PersonaUiState,
    onCreatePersona: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 76.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Avatar(persona = persona, size = 96)
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Create your custom AI",
            color = AppTextPrimary,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = "No messages yet",
            color = AppTextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 6.dp)
        )
        Button(
            onClick = onCreatePersona,
            colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .padding(top = 22.dp)
                .height(52.dp)
        ) {
            Text(
                text = "Edit persona",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge
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
    onOpenImage: (android.net.Uri) -> Unit,
    onMessageLongPress: (ChatMessage) -> Unit
) {
    var visible by remember(message.id) { mutableStateOf(false) }

    LaunchedEffect(message.id) {
        visible = true
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
                    if (message.content.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                val showText = message.content.isNotBlank() && !message.isImageLoading && message.remoteImageUrl == null
                if (showText) {
                    Text(
                        text = formatMarkdownLite(message.content),
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppTextPrimary,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
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
                        icon = Icons.Rounded.CameraAlt,
                        label = "Camera",
                        onClick = onCamera,
                        modifier = Modifier.weight(1f)
                    )
                    AttachmentOptionCard(
                        icon = Icons.Rounded.Upload,
                        label = "Photos",
                        onClick = onPhotos,
                        modifier = Modifier.weight(1f)
                    )
                    AttachmentOptionCard(
                        icon = Icons.Rounded.InsertDriveFile,
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppTextPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                color = AppTextPrimary,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ChatInputBar(
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
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, AppAccent.copy(alpha = 0.65f)),
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
                IconButton(
                    onClick = onOpenTools,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AttachFile,
                        contentDescription = "Open chat tools",
                        tint = AppTextPrimary,
                        modifier = Modifier.size(27.dp)
                    )
                }
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
    activeMemberId: String,
    responseRounds: Int,
    memoryEnabled: Boolean,
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
    onNameChange: (String) -> Unit,
    onInstructionModeChange: (InstructionMode) -> Unit,
    onBeginnerRoleChange: (String) -> Unit,
    onBeginnerStyleChange: (String) -> Unit,
    onBeginnerLimitsChange: (String) -> Unit,
    onPromptChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onSafetyLevelChange: (SafetyLevel) -> Unit,
    onTemperatureChange: (Float) -> Unit,
    onAvatarChange: (android.net.Uri?) -> Unit,
    onAvatarTransform: (Float, Float, Float) -> Unit,
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
    val imagePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
        onResult = onAvatarChange
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
                Spacer(modifier = Modifier.size(48.dp))
            }

            GroupEditor(
                members = groupMembers,
                activeMemberId = activeMemberId,
                responseRounds = responseRounds,
                onSelectMember = onSelectMember,
                onAddMember = onAddMember,
                onRemoveMember = onRemoveMember,
                onResponseRoundsChange = onResponseRoundsChange
            )

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

            InstructionSection(
                persona = persona,
                advancedExpanded = instructionPromptExpanded,
                onAdvancedExpandedChange = { instructionPromptExpanded = it },
                onModeChange = onInstructionModeChange,
                onBeginnerRoleChange = onBeginnerRoleChange,
                onBeginnerStyleChange = onBeginnerStyleChange,
                onBeginnerLimitsChange = onBeginnerLimitsChange,
                onAdvancedPromptChange = onPromptChange
            )

            SessionMemoryToggle(
                enabled = memoryEnabled,
                onEnabledChange = onMemoryEnabledChange
            )

            MoreOptions(
                expanded = moreOptions,
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

            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = "Save Persona",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
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
                text = "AI members",
                color = AppTextPrimary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${members.size}/4",
                color = AppTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 10.dp)
        ) {
            items(members, key = { it.id }) { member ->
                GroupMemberChip(
                    member = member,
                    selected = member.id == activeMemberId,
                    canRemove = members.size > 1,
                    onSelect = { onSelectMember(member.id) },
                    onRemove = { onRemoveMember(member.id) }
                )
            }
            if (members.size < 4) {
                item {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = AppSurface2,
                        border = BorderStroke(1.dp, AppStroke),
                        modifier = Modifier
                            .height(44.dp)
                            .clickable(onClick = onAddMember)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = AppAccentSoft,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Add AI",
                                color = AppAccentSoft,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = "AI turns $responseRounds",
            color = AppTextPrimary,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 16.dp)
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
private fun GroupMemberChip(
    member: GroupMember,
    selected: Boolean,
    canRemove: Boolean,
    onSelect: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (selected) AppAccentDim else AppSurface2,
        border = BorderStroke(1.dp, if (selected) AppAccent else AppStroke),
        modifier = Modifier
            .height(44.dp)
            .clickable(onClick = onSelect)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp, end = if (canRemove) 4.dp else 12.dp)
        ) {
            Avatar(persona = member.persona, size = 28)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = member.persona.displayName,
                color = if (selected) AppAccentSoft else AppTextPrimary,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (canRemove) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Remove AI",
                        tint = Color(0xFFFFA0AA),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
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
    advancedExpanded: Boolean,
    onAdvancedExpandedChange: (Boolean) -> Unit,
    onModeChange: (InstructionMode) -> Unit,
    onBeginnerRoleChange: (String) -> Unit,
    onBeginnerStyleChange: (String) -> Unit,
    onBeginnerLimitsChange: (String) -> Unit,
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

        AnimatedVisibility(visible = persona.instructionMode == InstructionMode.Beginner) {
            Column(modifier = Modifier.padding(top = 14.dp)) {
                PersonaTextField(
                    label = "Role",
                    value = persona.beginnerRole,
                    onValueChange = onBeginnerRoleChange,
                    helper = "Who this persona is, what they know, and how they relate to you.",
                    singleLine = false,
                    minLines = 2
                )
                PersonaTextField(
                    label = "Style",
                    value = persona.beginnerStyle,
                    onValueChange = onBeginnerStyleChange,
                    helper = "Tone, message length, speaking quirks, and roleplay texture.",
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

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
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
                    BackgroundOptions(
                        selected = background,
                        onBackgroundChange = onBackgroundChange,
                        onPickCustomBackground = onPickCustomBackground
                    )
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

private fun ChatSession.matchesSessionQuery(query: String): Boolean {
    val normalized = query.lowercase(Locale.getDefault())
    if (groupTitle().lowercase(Locale.getDefault()).contains(normalized)) return true
    if (preview.lowercase(Locale.getDefault()).contains(normalized)) return true
    return messages.any { message ->
        message.content.lowercase(Locale.getDefault()).contains(normalized) ||
            message.speakerName.orEmpty().lowercase(Locale.getDefault()).contains(normalized)
    }
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

@Preview
@Composable
private fun CompanionChatPreview() {
    AIChatTheme {
        CompanionChatApp()
    }
}
