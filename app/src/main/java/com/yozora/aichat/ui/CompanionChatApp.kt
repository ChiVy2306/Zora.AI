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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.yozora.aichat.R
import com.yozora.aichat.ui.chat.AppIconChoice
import com.yozora.aichat.ui.chat.AppNameChoice
import com.yozora.aichat.ui.chat.ApiVendor
import com.yozora.aichat.ui.chat.ChatBackground
import com.yozora.aichat.ui.chat.ChatMessage
import com.yozora.aichat.ui.chat.ChatSession
import com.yozora.aichat.ui.chat.ChatViewModel
import com.yozora.aichat.ui.chat.PersonaUiState
import com.yozora.aichat.ui.chat.QuotaUsageState
import com.yozora.aichat.ui.chat.SafetyLevel
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val APP_VERSION_NAME = "1.0.4"

@Composable
fun CompanionChatApp(
    viewModel: ChatViewModel = viewModel()
) {
    var viewedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var actionMessage by remember { mutableStateOf<ChatMessage?>(null) }
    val appVersionLabel = "${viewModel.appNameChoice.label} v$APP_VERSION_NAME"
    val clipboardManager = LocalClipboardManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        ChatScreen(
            persona = viewModel.persona,
            background = viewModel.background,
            messages = viewModel.messages,
            draft = viewModel.draft,
            isOnline = viewModel.activeApiKeyLabel != null,
            isSending = viewModel.isSending,
            chatError = viewModel.chatError,
            attachedImageUris = viewModel.attachedImageUris,
            webSearchEnabled = viewModel.webSearchEnabled,
            canUseWebSearch = viewModel.canUseWebSearch,
            onDraftChange = viewModel::updateDraft,
            onSend = viewModel::sendDraft,
            onAttachImages = viewModel::attachImages,
            onRemoveAttachedImage = viewModel::removeAttachedImage,
            onWebSearchChange = viewModel::updateWebSearchEnabled,
            onRequestAnimeImage = viewModel::requestAnimeImage,
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
            onOpenSettings = viewModel::openAppSettings,
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
                background = viewModel.background,
                moreOptions = viewModel.morePersonaOptions,
                activeApiKeyLabel = viewModel.activeApiKeyLabel,
                tavilyApiKeyLabel = viewModel.tavilyApiKeyLabel,
                waifuApiTokenLabel = viewModel.waifuApiTokenLabel,
                quotaUsage = viewModel.quotaUsage,
                dailyRequestLimit = viewModel.dailyRequestLimit,
                onBack = viewModel::closePersonaSheet,
                onVendorChange = viewModel::updateVendor,
                onNameChange = viewModel::updatePersonaName,
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
                onEditWaifuToken = viewModel::openWaifuTokenDialog,
                onClearWaifuToken = viewModel::clearWaifuApiToken,
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
            onSave = viewModel::saveApiKey
        )
    }

    if (viewModel.appSettingsVisible) {
        AppSettingsDialog(
            selectedName = viewModel.appNameChoice,
            selectedIcon = viewModel.appIconChoice,
            onNameChange = viewModel::updateAppName,
            onIconChange = viewModel::updateAppIcon,
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
                clipboardManager.setText(AnnotatedString(message.content))
                actionMessage = null
            },
            onEdit = {
                viewModel.beginEditMessage(message.id)
                actionMessage = null
            },
            onRetry = {
                viewModel.retryMessage(message.id)
                actionMessage = null
            }
        )
    }

    when {
        viewModel.appSettingsVisible -> BackHandler(onBack = viewModel::closeAppSettings)
        actionMessage != null -> BackHandler { actionMessage = null }
        viewModel.personaSheetVisible -> BackHandler(onBack = viewModel::closePersonaSheet)
        viewModel.sessionDrawerVisible -> BackHandler(onBack = viewModel::closeSessionDrawer)
    }
}

@Composable
private fun ChatScreen(
    persona: PersonaUiState,
    background: ChatBackground,
    messages: List<ChatMessage>,
    draft: String,
    isOnline: Boolean,
    isSending: Boolean,
    chatError: String?,
    attachedImageUris: List<android.net.Uri>,
    webSearchEnabled: Boolean,
    canUseWebSearch: Boolean,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachImages: (List<android.net.Uri>) -> Unit,
    onRemoveAttachedImage: (android.net.Uri) -> Unit,
    onWebSearchChange: (Boolean) -> Unit,
    onRequestAnimeImage: (String) -> Unit,
    onOpenImage: (android.net.Uri) -> Unit,
    onMessageLongPress: (ChatMessage) -> Unit,
    onOpenSessions: () -> Unit,
    onOpenPersona: () -> Unit
) {
    var toolsSheetVisible by remember { mutableStateOf(false) }
    var animeImageDialogVisible by remember { mutableStateOf(false) }
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
    val scrollTarget = messages.size + if (isSending) 1 else 0
    val isNearLatest by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems == 0 || lastVisible >= totalItems - 3
        }
    }
    val showJumpToLatest by remember {
        derivedStateOf {
            scrollTarget > 0 && (listState.canScrollForward || !isNearLatest)
        }
    }

    LaunchedEffect(scrollTarget, messages.lastOrNull()?.id) {
        if (scrollTarget > 0 && isNearLatest) {
            listState.animateScrollToItem(scrollTarget - 1)
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
                isOnline = isOnline,
                onOpenSessions = onOpenSessions,
                onOpenPersona = onOpenPersona
            )
            LazyColumn(
                state = listState,
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
                    items(messages, key = { it.id }) { message ->
                        AnimatedMessageBubble(
                            message = message,
                            persona = persona,
                            onOpenImage = onOpenImage,
                            onMessageLongPress = onMessageLongPress
                        )
                    }
                }
                if (isSending) {
                    item(key = "typing") {
                        TypingBubble(persona = persona)
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
                            if (scrollTarget > 0) {
                                listState.animateScrollToItem(scrollTarget - 1)
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

    if (toolsSheetVisible) {
        ChatToolsSheet(
            onDismiss = { toolsSheetVisible = false },
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
            onAnimeImage = {
                toolsSheetVisible = false
                animeImageDialogVisible = true
            },
            webSearchEnabled = webSearchEnabled,
            webSearchAvailable = canUseWebSearch,
            onWebSearchChange = onWebSearchChange
        )
    }

    if (animeImageDialogVisible) {
        AnimeImageRequestDialog(
            onDismiss = { animeImageDialogVisible = false },
            onSubmit = { request ->
                animeImageDialogVisible = false
                onRequestAnimeImage(request)
            }
        )
    }

    when {
        animeImageDialogVisible -> BackHandler { animeImageDialogVisible = false }
        toolsSheetVisible -> BackHandler { toolsSheetVisible = false }
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
                text = persona.displayName,
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
    onOpenSettings: () -> Unit,
    onClose: () -> Unit
) {
    var deleteTarget by remember { mutableStateOf<ChatSession?>(null) }

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
                        text = "Sessions",
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
                    text = "New session",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sessions, key = { it.id }) { session ->
                    SessionRow(
                        session = session,
                        selected = session.id == activeSessionId,
                        onClick = { onSelectSession(session.id) },
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
                    text = "Delete session?",
                    color = AppTextPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = session.persona.displayName.ifBlank { "This session" },
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
                    text = session.persona.displayName.ifBlank { "Unnamed persona" },
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
    onOpenImage: (android.net.Uri) -> Unit,
    onMessageLongPress: (ChatMessage) -> Unit
) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Avatar(persona = persona, size = 40)
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
    onRetry: () -> Unit
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
                    label = "Copy",
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
                    label = "Retry",
                    onClick = onRetry
                )
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
    onCamera: () -> Unit,
    onPhotos: () -> Unit,
    onFiles: () -> Unit,
    onAnimeImage: () -> Unit,
    webSearchEnabled: Boolean,
    webSearchAvailable: Boolean,
    onWebSearchChange: (Boolean) -> Unit
) {
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

                ActionSheetRow(
                    icon = Icons.Rounded.Search,
                    label = "Anime image",
                    onClick = onAnimeImage
                )

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
                        text = "Add a Tavily key and use Google as the API vendor.",
                        color = AppTextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 44.dp, bottom = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimeImageRequestDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var request by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppSurface,
        title = {
            Text(
                text = "Describe the image",
                color = AppTextPrimary,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = request,
                    onValueChange = { request = it },
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (request.isNotBlank()) {
                                onSubmit(request)
                            }
                        }
                    ),
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
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(request) },
                enabled = request.isNotBlank()
            ) {
                Text(text = "Fetch", color = AppAccentSoft)
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
                    .padding(start = 8.dp, top = 8.dp, end = 10.dp, bottom = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                IconButton(onClick = onOpenTools) {
                    Icon(
                        imageVector = Icons.Rounded.Menu,
                        contentDescription = "Open chat tools",
                        tint = AppTextPrimary
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
    background: ChatBackground,
    moreOptions: Boolean,
    activeApiKeyLabel: String?,
    tavilyApiKeyLabel: String?,
    waifuApiTokenLabel: String?,
    quotaUsage: QuotaUsageState,
    dailyRequestLimit: Int?,
    onBack: () -> Unit,
    onVendorChange: (ApiVendor) -> Unit,
    onNameChange: (String) -> Unit,
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
    onEditWaifuToken: () -> Unit,
    onClearWaifuToken: () -> Unit,
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

            CollapsibleInstructionPrompt(
                value = persona.instructionPrompt,
                expanded = instructionPromptExpanded,
                onExpandedChange = { instructionPromptExpanded = it },
                onValueChange = onPromptChange
            )

            MoreOptions(
                expanded = moreOptions,
                persona = persona,
                background = background,
                activeApiKeyLabel = activeApiKeyLabel,
                tavilyApiKeyLabel = tavilyApiKeyLabel,
                waifuApiTokenLabel = waifuApiTokenLabel,
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
                onEditWaifuToken = onEditWaifuToken,
                onClearWaifuToken = onClearWaifuToken
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
    waifuApiTokenLabel: String?,
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
    onEditWaifuToken: () -> Unit,
    onClearWaifuToken: () -> Unit
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
                        waifuApiTokenLabel = waifuApiTokenLabel,
                        quotaUsage = quotaUsage,
                        dailyRequestLimit = dailyRequestLimit,
                        onEditApiKey = onEditApiKey,
                        onClearApiKey = onClearApiKey,
                        onEditTavilyKey = onEditTavilyKey,
                        onClearTavilyKey = onClearTavilyKey,
                        onEditWaifuToken = onEditWaifuToken,
                        onClearWaifuToken = onClearWaifuToken
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

@Composable
private fun ApiKeySummary(
    activeApiKeyLabel: String?,
    tavilyApiKeyLabel: String?,
    waifuApiTokenLabel: String?,
    quotaUsage: QuotaUsageState,
    dailyRequestLimit: Int?,
    onEditApiKey: () -> Unit,
    onClearApiKey: () -> Unit,
    onEditTavilyKey: () -> Unit,
    onClearTavilyKey: () -> Unit,
    onEditWaifuToken: () -> Unit,
    onClearWaifuToken: () -> Unit
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
                label = "Waifu.im token",
                keyLabel = waifuApiTokenLabel,
                emptyText = "No Waifu.im token saved",
                onEdit = onEditWaifuToken,
                onClear = onClearWaifuToken
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
    onNameChange: (AppNameChoice) -> Unit,
    onIconChange: (AppIconChoice) -> Unit,
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
            Column {
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
    onSave: () -> Unit
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
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
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
