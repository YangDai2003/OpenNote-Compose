package com.yangdai.opennote.presentation.screen

import android.content.ClipData
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AddAlert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.LocalPrintshop
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.currentWindowSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastJoinToString
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yangdai.opennote.MainActivity
import com.yangdai.opennote.R
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.presentation.component.dialog.AudioSelectionDialog
import com.yangdai.opennote.presentation.component.dialog.ExportDialog
import com.yangdai.opennote.presentation.component.dialog.FolderListDialog
import com.yangdai.opennote.presentation.component.dialog.LinkDialog
import com.yangdai.opennote.presentation.component.dialog.ListDialog
import com.yangdai.opennote.presentation.component.dialog.ProgressDialog
import com.yangdai.opennote.presentation.component.dialog.ShareDialog
import com.yangdai.opennote.presentation.component.dialog.ShareType
import com.yangdai.opennote.presentation.component.dialog.TableDialog
import com.yangdai.opennote.presentation.component.dialog.TaskDialog
import com.yangdai.opennote.presentation.component.note.FindAndReplaceField
import com.yangdai.opennote.presentation.component.note.FindAndReplaceState
import com.yangdai.opennote.presentation.component.note.LiteTextField
import com.yangdai.opennote.presentation.component.note.MarkdownEditorRow
import com.yangdai.opennote.presentation.component.note.NoteSideSheet
import com.yangdai.opennote.presentation.component.note.NoteSideSheetItem
import com.yangdai.opennote.presentation.component.note.ReadView
import com.yangdai.opennote.presentation.component.note.StandardTextField
import com.yangdai.opennote.presentation.component.note.TemplateFilesList
import com.yangdai.opennote.presentation.event.DatabaseEvent
import com.yangdai.opennote.presentation.event.NoteEvent
import com.yangdai.opennote.presentation.event.UiEvent
import com.yangdai.opennote.presentation.util.Constants
import com.yangdai.opennote.presentation.util.SharedContent
import com.yangdai.opennote.presentation.util.TemplateProcessor
import com.yangdai.opennote.presentation.util.getOrCreateDirectory
import com.yangdai.opennote.presentation.util.rememberDateTimeFormatter
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteScreen(
    viewModel: SharedViewModel = hiltViewModel(LocalActivity.current as MainActivity),
    noteId: Long,
    isLargeScreen: Boolean,
    sharedContent: SharedContent?,
    navigateUp: () -> Unit
) {
    val noteState by viewModel.noteStateFlow.collectAsStateWithLifecycle()
    val folderNoteCounts by viewModel.folderWithNoteCountsFlow.collectAsStateWithLifecycle()
    val html by viewModel.html.collectAsStateWithLifecycle()
    val outline by viewModel.outline.collectAsStateWithLifecycle()
    val noteTextDetails by viewModel.textState.collectAsStateWithLifecycle()
    val dataAction by viewModel.dataActionStateFlow.collectAsStateWithLifecycle()
    val appSettings by viewModel.settingsStateFlow.collectAsStateWithLifecycle()

    // 确保屏幕旋转等配置变更时，不会重复加载笔记
    var lastLoadedNoteId by rememberSaveable { mutableStateOf<Long?>(null) }

    DisposableEffect(Unit) {
        if (noteId != lastLoadedNoteId) {
            lastLoadedNoteId = noteId
            viewModel.onNoteEvent(NoteEvent.Load(noteId, sharedContent))
        }
        onDispose {
            if (!viewModel.shouldShowSnackbar())
                viewModel.onNoteEvent(NoteEvent.SaveOrUpdate)
        }
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val isReadViewAtStart = appSettings.isDefaultViewForReading && noteId != -1L
    var isReadView by rememberSaveable { mutableStateOf(isReadViewAtStart) }
    var isEditorAndPreviewSynced by rememberSaveable { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var selectedHeader by remember { mutableStateOf<IntRange?>(null) }
    var searchState by remember { mutableStateOf(FindAndReplaceState()) }
    LaunchedEffect(searchState.searchWord, viewModel.contentState.text) {
        withContext(Dispatchers.Default) {
            searchState = searchState.copy(
                matchCount = if (searchState.searchWord.isNotBlank())
                    searchState.searchWord.toRegex()
                        .findAll(viewModel.contentState.text)
                        .count()
                else 0
            )
        }
    }

    var isSideSheetOpen by rememberSaveable { mutableStateOf(false) }
    var showFolderDialog by rememberSaveable { mutableStateOf(false) }
    var showListDialog by rememberSaveable { mutableStateOf(false) }
    var showTableDialog by rememberSaveable { mutableStateOf(false) }
    var showLinkDialog by rememberSaveable { mutableStateOf(false) }
    var showTaskDialog by rememberSaveable { mutableStateOf(false) }
    var showExportDialog by rememberSaveable { mutableStateOf(false) }
    var showShareDialog by rememberSaveable { mutableStateOf(false) }
    var showAudioDialog by rememberSaveable { mutableStateOf(false) }
    var showTemplateBottomSheet by remember { mutableStateOf(false) }
    val triggerPrint = remember { mutableStateOf(false) }
    val launchShareIntent = remember { mutableStateOf(false) }

    // Folder name, default to "All Notes", or the name of the current folder the note is in
    var folderName by rememberSaveable { mutableStateOf("") }
    var timestamp by rememberSaveable { mutableStateOf("") }
    val dateTimeFormatter = rememberDateTimeFormatter()

    LaunchedEffect(noteState) {
        withContext(Dispatchers.Default) {
            folderName = noteState.folderId?.let { folderId ->
                folderNoteCounts.firstOrNull { it.first.id == folderId }?.first?.name
            } ?: context.getString(R.string.all_notes)
            timestamp =
                if (noteState.timestamp == null) dateTimeFormatter.format(System.currentTimeMillis())
                else dateTimeFormatter.format(noteState.timestamp)
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isReadView) {
        keyboardController?.hide()
        focusManager.clearFocus()
        isSearching = false
        if (!isLargeScreen) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(if (isReadView) 1 else 0)
            }
        }
    }

    BackHandler(isReadView) {
        if (isReadView) {
            focusManager.clearFocus()
            isReadView = false
        }
    }

    LaunchedEffect(true) {
        viewModel.uiEventFlow.collect { event ->
            if (event is UiEvent.NavigateBack) navigateUp()
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) viewModel.onDatabaseEvent(
            DatabaseEvent.ImportImages(
                context.applicationContext, uris
            )
        )
    }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.onDatabaseEvent(
            DatabaseEvent.ImportVideo(
                context.applicationContext, uri
            )
        )
    }

    val messageBar = remember { SnackbarHostState() }

    BackHandler(viewModel.shouldShowSnackbar()) {
        if (viewModel.shouldShowSnackbar())
            coroutineScope.launch {
                messageBar.showSnackbar(
                    "",
                    duration = SnackbarDuration.Short
                )
            }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.isCtrlPressed) {
                    if (keyEvent.isShiftPressed) {
                        when (keyEvent.key) {

                            Key.P -> {
                                showTemplateBottomSheet = true
                                true
                            }

                            else -> false
                        }
                    } else {
                        when (keyEvent.key) {
                            Key.P -> {
                                isReadView = !isReadView
                                true
                            }

                            Key.F -> {
                                isSearching = !isSearching
                                true
                            }

                            else -> false
                        }
                    }
                } else false
            },
        topBar = {
            TopAppBar(
                title = {
                    FilledTonalButton(
                        modifier = Modifier.sizeIn(maxWidth = 160.dp),
                        onClick = { showFolderDialog = true }
                    ) {
                        Text(
                            text = folderName, maxLines = 1, modifier = Modifier.basicMarquee()
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (viewModel.shouldShowSnackbar())
                            coroutineScope.launch {
                                messageBar.showSnackbar(
                                    "",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        else
                            navigateUp()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(id = R.string.navigate_back)
                        )
                    }
                },
                actions = {

                    if (!isReadView) TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                        tooltip = {
                            PlainTooltip(content = { Text("Ctrl + F") })
                        },
                        state = rememberTooltipState(),
                        focusable = false,
                        enableUserInput = true
                    ) {
                        IconButton(onClick = { isSearching = !isSearching }) {
                            Icon(
                                imageVector = if (isSearching) Icons.Outlined.SearchOff
                                else Icons.Outlined.Search, contentDescription = "Search"
                            )
                        }
                    }

                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                        tooltip = {
                            PlainTooltip(content = { Text("Ctrl + P") })
                        },
                        state = rememberTooltipState(),
                        focusable = false,
                        enableUserInput = true
                    ) {
                        IconButton(onClick = { isReadView = !isReadView }) {
                            Icon(
                                imageVector = if (isReadView) Icons.Outlined.EditNote
                                else Icons.AutoMirrored.Outlined.MenuBook,
                                contentDescription = "Mode"
                            )
                        }
                    }

                    if (noteState.isStandard) IconButton(onClick = {
                        isEditorAndPreviewSynced = !isEditorAndPreviewSynced
                    }) {
                        Icon(
                            painter = painterResource(if (isEditorAndPreviewSynced) R.drawable.link_off else R.drawable.link),
                            contentDescription = "Mode"
                        )
                    }

                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            isSideSheetOpen = true
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.right_panel_open),
                            contentDescription = "Open Drawer"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (noteState.isStandard) AnimatedVisibility(
                visible = !isReadView,
                enter = slideInVertically { fullHeight -> fullHeight },
                exit = slideOutVertically { fullHeight -> fullHeight }) {
                MarkdownEditorRow(
                    canRedo = viewModel.contentState.undoState.canRedo,
                    canUndo = viewModel.contentState.undoState.canUndo,
                    onEdit = { viewModel.onNoteEvent(NoteEvent.Edit(it)) },
                    onTableButtonClick = { showTableDialog = true },
                    onListButtonClick = { showListDialog = true },
                    onTaskButtonClick = { showTaskDialog = true },
                    onLinkButtonClick = { showLinkDialog = true },
                    onImageButtonClick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    onAudioButtonClick = {
                        showAudioDialog = true
                    },
                    onVideoButtonClick = {
                        videoPicker.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.VideoOnly
                            )
                        )
                    },
                    onTemplateClick = {
                        showTemplateBottomSheet = true
                    })
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = messageBar) {
                Snackbar(
                    content = { Text(stringResource(R.string.ask_save_note)) },
                    action = {
                        IconButton(onClick = {
                            viewModel.onNoteEvent(NoteEvent.SaveOrUpdate)
                            navigateUp()
                        }) {
                            Icon(
                                Icons.Outlined.Save,
                                contentDescription = "Save"
                            )
                        }
                    },
                    dismissAction = {
                        IconButton(onClick = navigateUp) {
                            Icon(
                                Icons.AutoMirrored.Outlined.ExitToApp,
                                contentDescription = "Exit"
                            )
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            AnimatedContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                targetState = isSearching,
                contentAlignment = Alignment.TopCenter
            ) {
                if (it) FindAndReplaceField(
                    isStandard = noteState.isStandard,
                    state = searchState,
                    onStateUpdate = { searchState = it })
                else BasicTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    state = viewModel.titleState,
                    readOnly = isReadView,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = when (appSettings.titleAlignment) {
                            1 -> TextAlign.Center
                            2 -> TextAlign.Right
                            else -> TextAlign.Left
                        }
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    decorator = { innerTextField ->
                        TextFieldDefaults.DecorationBox(
                            value = viewModel.titleState.text.toString(),
                            innerTextField = innerTextField,
                            enabled = true,
                            singleLine = true,
                            visualTransformation = VisualTransformation.None,
                            interactionSource = remember { MutableInteractionSource() },
                            placeholder = {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = stringResource(id = R.string.title),
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = when (appSettings.titleAlignment) {
                                            1 -> TextAlign.Center
                                            2 -> TextAlign.Right
                                            else -> TextAlign.Left
                                        }
                                    )
                                )
                            },
                            contentPadding = PaddingValues(0.dp),
                            container = {}
                        )
                    }
                )
            }

            /*-------------------------------------------------*/
            val scrollState = rememberScrollState()

            if (!noteState.isStandard) LiteTextField(
                modifier = Modifier.fillMaxSize(),
                readMode = isReadView,
                state = viewModel.contentState,
                scrollState = scrollState,
                headerRange = selectedHeader,
                searchWord = searchState.searchWord,
                onTemplateClick = { showTemplateBottomSheet = true })
            else {

                if (isLargeScreen) {

                    val interactionSource = remember { MutableInteractionSource() }
                    var editorWeight by remember { mutableFloatStateOf(0.5f) }
                    val windowWidth = currentWindowSize().width.toFloat()

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StandardTextField(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(editorWeight)
                                .padding(start = 16.dp, end = 8.dp),
                            readMode = isReadView,
                            state = viewModel.contentState,
                            scrollState = scrollState,
                            isLintActive = appSettings.isLintActive,
                            headerRange = selectedHeader,
                            findAndReplaceState = searchState,
                            onFindAndReplaceUpdate = { searchState = it },
                            onTableButtonClick = { showTableDialog = true },
                            onListButtonClick = { showListDialog = true },
                            onTaskButtonClick = { showTaskDialog = true },
                            onLinkButtonClick = { showLinkDialog = true },
                            onImageButtonClick = {
                                photoPicker.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            onAudioButtonClick = {
                                showAudioDialog = true
                            },
                            onVideoButtonClick = {
                                videoPicker.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.VideoOnly
                                    )
                                )
                            },
                            onImageReceived = {
                                viewModel.onDatabaseEvent(
                                    DatabaseEvent.ImportImages(
                                        context.applicationContext, it
                                    )
                                )
                            })

                        // 按下宽12dp，正常宽4dp，有默认边距，所以使用sizeIn()限制宽度
                        VerticalDragHandle(
                            modifier = Modifier
                                .sizeIn(maxWidth = 12.dp, minWidth = 4.dp)
                                .draggable(
                                    interactionSource = interactionSource,
                                    state = rememberDraggableState { delta ->
                                        editorWeight =
                                            (editorWeight + delta / windowWidth).coerceIn(
                                                0.3f, 0.7f
                                            )
                                    },
                                    orientation = Orientation.Horizontal,
                                    onDragStopped = {
                                        val positions = listOf(1f / 3f, 0.5f, 2f / 3f)
                                        val closest =
                                            positions.minByOrNull { abs(it - editorWeight) }
                                        if (closest != null) {
                                            editorWeight = closest
                                        }
                                    }
                                ),
                            interactionSource = interactionSource
                        )

                        ReadView(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f - editorWeight),
                            html = html,
                            noteName = viewModel.titleState.text.toString(),
                            printEnabled = triggerPrint,
                            launchShareIntent = launchShareIntent,
                            rootUri = appSettings.storagePath.toUri(),
                            scrollState = scrollState,
                            scrollSynchronized = isEditorAndPreviewSynced,
                            settingsState = appSettings
                        )
                    }
                } else HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    userScrollEnabled = false
                ) { currentPage: Int ->
                    when (currentPage) {
                        0 -> {
                            StandardTextField(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                state = viewModel.contentState,
                                readMode = isReadView,
                                scrollState = scrollState,
                                isLintActive = appSettings.isLintActive,
                                headerRange = selectedHeader,
                                findAndReplaceState = searchState,
                                onFindAndReplaceUpdate = { searchState = it },
                                onTableButtonClick = { showTableDialog = true },
                                onListButtonClick = { showListDialog = true },
                                onTaskButtonClick = { showTaskDialog = true },
                                onLinkButtonClick = { showLinkDialog = true },
                                onImageButtonClick = {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                                onAudioButtonClick = {
                                    showAudioDialog = true
                                },
                                onVideoButtonClick = {
                                    videoPicker.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.VideoOnly
                                        )
                                    )
                                },
                                onImageReceived = {
                                    viewModel.onDatabaseEvent(
                                        DatabaseEvent.ImportImages(
                                            context.applicationContext, it
                                        )
                                    )
                                })
                        }

                        1 -> {
                            ReadView(
                                modifier = Modifier.fillMaxSize(),
                                html = html,
                                noteName = viewModel.titleState.text.toString(),
                                printEnabled = triggerPrint,
                                launchShareIntent = launchShareIntent,
                                rootUri = appSettings.storagePath.toUri(),
                                scrollSynchronized = isEditorAndPreviewSynced,
                                scrollState = scrollState,
                                settingsState = appSettings
                            )
                        }
                    }
                }
            }
        }
    }

    NoteSideSheet(
        modifier = Modifier.fillMaxSize(),
        isDrawerOpen = isSideSheetOpen,
        onDismiss = { isSideSheetOpen = false },
        isLargeScreen = isLargeScreen,
        outline = outline,
        onHeaderClick = { selectedHeader = it },
        actionContent = {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip(content = {
                        Text(
                            text = if (noteState.isStandard) stringResource(R.string.lite_mode)
                            else stringResource(R.string.standard_mode)
                        )
                    })
                },
                state = rememberTooltipState(),
                focusable = false,
                enableUserInput = true
            ) {
                IconButton(onClick = { viewModel.onNoteEvent(NoteEvent.SwitchType) }) {
                    Icon(
                        imageVector = Icons.Outlined.SwapHoriz,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = "Switch Note Type"
                    )
                }
            }

            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip(content = { Text(text = stringResource(id = R.string.delete)) })
                },
                state = rememberTooltipState(),
                focusable = false,
                enableUserInput = true
            ) {
                IconButton(onClick = { viewModel.onNoteEvent(NoteEvent.Delete) }) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = "Delete"
                    )
                }
            }

            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip(content = { Text(text = stringResource(id = R.string.remind)) })
                },
                state = rememberTooltipState(),
                focusable = false,
                enableUserInput = true
            ) {
                IconButton(onClick = {
                    val intent =
                        Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
                            .putExtra(
                                CalendarContract.Events.TITLE,
                                viewModel.titleState.text.toString()
                            ).putExtra(
                                CalendarContract.Events.DESCRIPTION,
                                viewModel.contentState.text.toString()
                            )

                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.no_calendar_app_found),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Outlined.AddAlert,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = "Remind"
                    )
                }
            }

            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip(content = { Text(text = stringResource(R.string.export)) })
                },
                state = rememberTooltipState(),
                focusable = false,
                enableUserInput = true
            ) {
                IconButton(onClick = { showExportDialog = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Upload,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = "Export"
                    )
                }
            }

            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip(content = { Text(text = stringResource(R.string.share)) })
                },
                state = rememberTooltipState(),
                focusable = false,
                enableUserInput = true
            ) {
                IconButton(onClick = { showShareDialog = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = "Share"
                    )
                }
            }
            AnimatedVisibility(
                noteState.isStandard
            ) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip(content = { Text(text = stringResource(R.string.print)) })
                    },
                    state = rememberTooltipState(),
                    focusable = false,
                    enableUserInput = true
                ) {
                    IconButton(onClick = { triggerPrint.value = true }) {
                        Icon(
                            imageVector = Icons.Outlined.LocalPrintshop,
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = "Print"
                        )
                    }
                }
            }
        },
        drawerContent = {
            NoteSideSheetItem(
                key = "UUID",
                value = noteState.id.toString(),
                shouldFormat = false
            )

            NoteSideSheetItem(
                key = stringResource(R.string.edited),
                value = timestamp
            )

            NoteSideSheetItem(
                key = stringResource(R.string.mode),
                value = if (noteState.isStandard) stringResource(R.string.standard_mode)
                else stringResource(R.string.lite_mode)
            )

            NoteSideSheetItem(
                key = stringResource(R.string.char_count),
                value = noteTextDetails.charCount.toString()
            )

            NoteSideSheetItem(
                key = stringResource(R.string.word_count),
                value = noteTextDetails.wordCountWithPunctuation.toString()
            )

            NoteSideSheetItem(
                key = stringResource(R.string.word_count_without_punctuation),
                value = noteTextDetails.wordCountWithoutPunctuation.toString()
            )

            NoteSideSheetItem(
                key = stringResource(R.string.line_count),
                value = noteTextDetails.lineCount.toString()
            )

            NoteSideSheetItem(
                key = stringResource(R.string.paragraph_count),
                value = noteTextDetails.paragraphCount.toString()
            )
        })

    if (showAudioDialog) {
        AudioSelectionDialog(
            rootUri = appSettings.storagePath.toUri(),
            onDismiss = { showAudioDialog = false },
            onAudioSelected = {
                viewModel.onNoteEvent(
                    NoteEvent.Edit(
                        Constants.Editor.TEXT, "<audio src=\"$it\" controls></audio>"
                    )
                )
                showAudioDialog = false
            })
    }

    val templateSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    if (showTemplateBottomSheet) {
        ModalBottomSheet(
            modifier = Modifier.statusBarsPadding(),
            sheetGesturesEnabled = false,
            onDismissRequest = {
                showTemplateBottomSheet = false
            },
            dragHandle = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        modifier = Modifier.padding(end = 8.dp, top = 8.dp), onClick = {
                            coroutineScope.launch {
                                templateSheetState.hide()
                            }.invokeOnCompletion {
                                if (!templateSheetState.isVisible) {
                                    showTemplateBottomSheet = false
                                }
                            }
                        }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close sheet")
                    }
                }
            },
            sheetState = templateSheetState
        ) {
            TemplateFilesList(
                rootUri = appSettings.storagePath.toUri(), // 传入根URI
                context = context.applicationContext, saveCurrentNoteAsTemplate = {
                    val noteName = if (viewModel.titleState.text.isBlank()) "Untitled"
                    else viewModel.titleState.text.toString()
                    val fileName = "$noteName.md"
                    val fileContent = viewModel.contentState.text.toString()
                    val rootUri = appSettings.storagePath.toUri()

                    val openNoteDir = getOrCreateDirectory(
                        context.applicationContext, rootUri, Constants.File.OPENNOTE
                    )
                    val templatesFolder = openNoteDir?.let { dir ->
                        getOrCreateDirectory(
                            context.applicationContext, dir.uri, Constants.File.OPENNOTE_TEMPLATES
                        )
                    }

                    templatesFolder?.createFile("text/*", fileName)?.let { newFile ->
                        context.applicationContext.contentResolver.openOutputStream(newFile.uri)
                            .use { output ->
                                output?.write(fileContent.toByteArray())
                            }
                    }

                    coroutineScope.launch {
                        templateSheetState.hide()
                    }.invokeOnCompletion {
                        if (!templateSheetState.isVisible) {
                            showTemplateBottomSheet = false
                        }
                    }
                }, onFileSelected = { content ->
                    val temple = TemplateProcessor(
                        appSettings.dateFormatter, appSettings.timeFormatter
                    ).process(content)
                    // 处理选中的模板内容
                    coroutineScope.launch {
                        templateSheetState.hide()
                    }.invokeOnCompletion {
                        if (!templateSheetState.isVisible) {
                            showTemplateBottomSheet = false
                            viewModel.onNoteEvent(
                                NoteEvent.Edit(
                                    Constants.Editor.TEXT, temple
                                )
                            )
                        }
                    }
                })
        }
    }

    if (showExportDialog) {
        ExportDialog(onDismissRequest = { showExportDialog = false }, onConfirm = {
            viewModel.onDatabaseEvent(
                DatabaseEvent.ExportFiles(
                    context.applicationContext, listOf(
                        NoteEntity(
                            title = viewModel.titleState.text.toString(),
                            content = viewModel.contentState.text.toString(),
                            timestamp = System.currentTimeMillis()
                        )
                    ), it
                )
            )
            showExportDialog = false
        })
    }

    if (showShareDialog) {
        val clipboard = LocalClipboard.current
        ShareDialog(
            isStandard = noteState.isStandard,
            onDismissRequest = { showShareDialog = false },
            onConfirm = {
                when (it) {
                    ShareType.COPY -> {
                        val clipData = ClipData.newPlainText(
                            "Markdown", viewModel.contentState.text.toString()
                        )
                        val clipEntry = ClipEntry(clipData)
                        coroutineScope.launch {
                            clipboard.setClipEntry(clipEntry)
                        }
                        Toast.makeText(context, "Markdown 📋", Toast.LENGTH_SHORT).show()
                    }

                    ShareType.TEXT -> {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TITLE, viewModel.titleState.text.toString()
                            )
                            putExtra(
                                Intent.EXTRA_TEXT, viewModel.contentState.text.toString()
                            )
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }

                    ShareType.FILE -> {
                        val fileName =
                            viewModel.titleState.text.toString() + if (noteState.isStandard) ".md" else ".txt"
                        val file = File(context.applicationContext.cacheDir, fileName)
                        val fileContent = viewModel.contentState.text.toString()
                        file.writeText(fileContent)
                        val fileUri = FileProvider.getUriForFile(
                            context.applicationContext,
                            "${context.applicationContext.packageName}.fileprovider",
                            file
                        )
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_STREAM, fileUri)
                            type = "text/*"
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }

                    ShareType.IMAGE -> {
                        launchShareIntent.value = true
                    }
                }
                showShareDialog = false
            })
    }

    if (showTableDialog) {
        TableDialog(onDismissRequest = { showTableDialog = false }) { row, column ->
            viewModel.onNoteEvent(NoteEvent.Edit(Constants.Editor.TABLE, "$row,$column"))
        }
    }

    if (showListDialog) {
        ListDialog(onDismissRequest = { showListDialog = false }) {
            viewModel.onNoteEvent(
                NoteEvent.Edit(
                    Constants.Editor.LIST, it.fastJoinToString(separator = "\n")
                )
            )
        }
    }

    if (showTaskDialog) {
        TaskDialog(onDismissRequest = { showTaskDialog = false }) {
            viewModel.addTasks(it)
        }
    }

    if (showLinkDialog) {
        LinkDialog(onDismissRequest = { showLinkDialog = false }) { linkName, linkUri ->
            val insertText = "[${linkName}](${linkUri})"
            viewModel.onNoteEvent(NoteEvent.Edit(Constants.Editor.TEXT, insertText))
        }
    }

    if (showFolderDialog) {
        FolderListDialog(
            hint = stringResource(R.string.destination_folder),
            oFolderId = noteState.folderId,
            folders = folderNoteCounts.map { it.first },
            onDismissRequest = { showFolderDialog = false }) {
            viewModel.onNoteEvent(NoteEvent.FolderChanged(it))
        }
    }

    ProgressDialog(
        isLoading = dataAction.loading,
        progress = dataAction.progress,
        message = dataAction.message,
        onDismissRequest = viewModel::cancelDataAction
    )
}
