package com.yangdai.opennote.presentation.screen

import android.content.ClipData
import android.content.Intent
import android.os.Build
import android.provider.CalendarContract
import android.widget.Toast
import androidx.activity.BackEventCompat
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.PredictiveBackHandler
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowSize
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastJoinToString
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yangdai.opennote.MainActivity
import com.yangdai.opennote.R
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.presentation.component.FindAndReplaceField
import com.yangdai.opennote.presentation.component.dialog.ExportDialog
import com.yangdai.opennote.presentation.component.dialog.FolderListDialog
import com.yangdai.opennote.presentation.component.dialog.LinkDialog
import com.yangdai.opennote.presentation.component.dialog.ListDialog
import com.yangdai.opennote.presentation.component.dialog.ProgressDialog
import com.yangdai.opennote.presentation.component.dialog.ShareDialog
import com.yangdai.opennote.presentation.component.dialog.ShareType
import com.yangdai.opennote.presentation.component.dialog.TableDialog
import com.yangdai.opennote.presentation.component.dialog.TaskDialog
import com.yangdai.opennote.presentation.component.text.LiteTextField
import com.yangdai.opennote.presentation.component.text.MarkdownEditorRow
import com.yangdai.opennote.presentation.component.text.ReadView
import com.yangdai.opennote.presentation.component.text.StandardTextField
import com.yangdai.opennote.presentation.event.DatabaseEvent
import com.yangdai.opennote.presentation.event.NoteEvent
import com.yangdai.opennote.presentation.event.UiEvent
import com.yangdai.opennote.presentation.util.Constants
import com.yangdai.opennote.presentation.util.SharedContent
import com.yangdai.opennote.presentation.util.timestampToFormatLocalDateTime
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteScreen(
    sharedViewModel: SharedViewModel = hiltViewModel(LocalActivity.current as MainActivity),
    id: Long,
    isLargeScreen: Boolean,
    sharedContent: SharedContent?,
    navigateUp: () -> Unit,
    onScanTextClick: () -> Unit
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 确保屏幕旋转等配置变更时，不会重复加载笔记
    var previousId by rememberSaveable { mutableStateOf<Long?>(null) }

    DisposableEffect(Unit) {
        if (id != previousId) {
            previousId = id
            coroutineScope.launch {
                sharedViewModel.onNoteEvent(NoteEvent.Load(id))
                if (sharedContent != null) {
                    withContext(Dispatchers.Main) {
                        sharedViewModel.onNoteEvent(
                            NoteEvent.Edit(
                                Constants.Editor.TITLE, sharedContent.fileName
                            )
                        )
                        sharedViewModel.onNoteEvent(
                            NoteEvent.Edit(
                                Constants.Editor.TEXT, sharedContent.content
                            )
                        )
                    }
                }
            }
        }

        onDispose {
            sharedViewModel.onNoteEvent(NoteEvent.Update)
        }
    }

    val noteState by sharedViewModel.noteStateFlow.collectAsStateWithLifecycle()
    val folderNoteCounts by sharedViewModel.folderWithNoteCountsFlow.collectAsStateWithLifecycle()
    val html by sharedViewModel.html.collectAsStateWithLifecycle()
    val actionState by sharedViewModel.dataActionStateFlow.collectAsStateWithLifecycle()
    val settingsState by sharedViewModel.settingsStateFlow.collectAsStateWithLifecycle()
    val scannedText by sharedViewModel.scannedTextStateFlow.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(pageCount = { 2 })
    val snackbarHostState = remember { SnackbarHostState() }
    // Switch between read mode and edit mode
    val initialReadView = settingsState.isDefaultViewForReading
    var isReadView by remember { mutableStateOf(initialReadView) }
    var isEditorAndPreviewSynced by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var searchWord by remember { mutableStateOf("") }
    var replaceWord by remember { mutableStateOf("") }
    val matchCount by remember(searchWord, sharedViewModel.contentState.text) {
        derivedStateOf {
            if (searchWord.isNotBlank()) searchWord.toRegex()
                .findAll(sharedViewModel.contentState.text).count() else 0
        }
    }

    var showFolderDialog by rememberSaveable { mutableStateOf(false) }
    var showListDialog by rememberSaveable { mutableStateOf(false) }
    var showTableDialog by rememberSaveable { mutableStateOf(false) }
    var showLinkDialog by rememberSaveable { mutableStateOf(false) }
    var showTaskDialog by rememberSaveable { mutableStateOf(false) }
    var showExportDialog by rememberSaveable { mutableStateOf(false) }
    var showShareDialog by rememberSaveable { mutableStateOf(false) }

    // Folder name, default to "All Notes", or the name of the current folder the note is in
    var folderName by rememberSaveable { mutableStateOf("") }
    var timestamp by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(noteState) {
        folderName = noteState.folderId?.let { folderId ->
            folderNoteCounts.find { it.first.id == folderId }?.first?.name
        } ?: context.getString(R.string.all_notes)
        timestamp = if (noteState.timestamp == null) System.currentTimeMillis()
            .timestampToFormatLocalDateTime()
        else noteState.timestamp!!.timestampToFormatLocalDateTime()
    }

    LaunchedEffect(scannedText) {
        if (scannedText.isNotEmpty()) {
            withContext(Dispatchers.Main) {
                sharedViewModel.onNoteEvent(NoteEvent.Edit(Constants.Editor.TEXT, scannedText))
                sharedViewModel.scannedTextStateFlow.value = ""
            }
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isReadView) {
        if (isReadView) {
            keyboardController?.hide()
            focusManager.clearFocus()
            isSearching = false
        }
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
        sharedViewModel.uiEventFlow.collect { event ->
            if (event is UiEvent.NavigateBack) navigateUp()
        }
    }

    var isTitleFocused by rememberSaveable { mutableStateOf(false) }
    var isContentFocused by rememberSaveable { mutableStateOf(false) }

    BackHandler(isTitleFocused || isContentFocused) {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    fun showSnackbar() {
        coroutineScope.launch {
            val result = snackbarHostState.showSnackbar(
                message = context.getString(R.string.confirm_msg),
                actionLabel = context.getString(R.string.confirm),
                withDismissAction = true,
                duration = SnackbarDuration.Indefinite
            )
            when (result) {
                SnackbarResult.ActionPerformed -> {
                    navigateUp()
                }

                SnackbarResult.Dismissed -> {
                    // Do nothing
                }
            }
        }
    }

    PredictiveBackHandler(
        noteState.id == null && (sharedViewModel.titleState.text.isNotBlank() || sharedViewModel.contentState.text.isNotBlank())
    ) { progress: Flow<BackEventCompat> ->
        // code for gesture back started
        try {
            progress.collect { _ ->
                // code for progress
            }
            showSnackbar()
            // code for completion
        } catch (_: CancellationException) {
            // code for cancellation
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) sharedViewModel.onDatabaseEvent(
            DatabaseEvent.ImportImages(
                context.applicationContext, context.applicationContext.contentResolver, uris
            )
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.isCtrlPressed && keyEvent.key == Key.P) {
                    isReadView = !isReadView
                    true
                } else false
            }, topBar = {
            TopAppBar(title = {
                FilledTonalButton(modifier = Modifier.sizeIn(maxWidth = 160.dp), onClick = {
                    showFolderDialog = true
                }) {
                    Text(
                        text = folderName, maxLines = 1, modifier = Modifier.basicMarquee()
                    )
                }
            }, navigationIcon = {
                IconButton(onClick = {
                    if (noteState.id != null) navigateUp()
                    else if (sharedViewModel.titleState.text.isBlank() && sharedViewModel.contentState.text.isBlank()) navigateUp()
                    else showSnackbar()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(id = R.string.navigate_back)
                    )
                }
            }, actions = {

                if (noteState.id == null) IconButton(
                    onClick = {
                        if (sharedViewModel.titleState.text.isBlank() && sharedViewModel.contentState.text.isBlank()) Toast.makeText(
                            context, context.getString(R.string.empty_note), Toast.LENGTH_SHORT
                        ).show()
                        else sharedViewModel.onNoteEvent(NoteEvent.Save)
                    }) {
                    Icon(
                        imageVector = Icons.Outlined.Save, contentDescription = "Save"
                    )
                }

                if (!isReadView) IconButton(onClick = { isSearching = !isSearching }) {
                    Icon(
                        imageVector = if (isSearching) Icons.Outlined.SearchOff
                        else Icons.Outlined.Search, contentDescription = "Search"
                    )
                }

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
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
                            else Icons.AutoMirrored.Outlined.MenuBook, contentDescription = "Mode"
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

                var showMenu by remember { mutableStateOf(false) }

                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert, contentDescription = "More"
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    offset = DpOffset(if (noteState.isStandard) 64.dp else 8.dp, 0.dp),
                    onDismissRequest = { showMenu = false }) {

                    DropdownMenuItem(leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.SwapHoriz,
                            contentDescription = "Switch Note Type"
                        )
                    }, text = {
                        Text(
                            text = if (noteState.isStandard) stringResource(R.string.lite_mode)
                            else stringResource(R.string.standard_mode)
                        )
                    }, onClick = { sharedViewModel.onNoteEvent(NoteEvent.SwitchType) })

                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Delete, contentDescription = "Delete"
                            )
                        },
                        text = { Text(text = stringResource(id = R.string.delete)) },
                        onClick = { sharedViewModel.onNoteEvent(NoteEvent.Delete) })

                    DropdownMenuItem(leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Alarm, contentDescription = "Remind"
                        )
                    }, text = { Text(text = stringResource(id = R.string.remind)) }, onClick = {

                        val intent =
                            Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
                                .putExtra(
                                    CalendarContract.Events.TITLE,
                                    sharedViewModel.titleState.text.toString()
                                ).putExtra(
                                    CalendarContract.Events.DESCRIPTION,
                                    sharedViewModel.contentState.text.toString()
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
                    })

                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Upload, contentDescription = "Export"
                            )
                        },
                        text = { Text(text = stringResource(R.string.export)) },
                        onClick = { showExportDialog = true })

                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Share, contentDescription = "Share"
                            )
                        },
                        text = { Text(text = stringResource(R.string.share)) },
                        onClick = { showShareDialog = true })
                }
            })
        }, bottomBar = {
            if (noteState.isStandard) AnimatedVisibility(
                visible = !isReadView,
                enter = slideInVertically { fullHeight -> fullHeight },
                exit = slideOutVertically { fullHeight -> fullHeight }) {
                MarkdownEditorRow(
                    canRedo = sharedViewModel.contentState.undoState.canRedo,
                    canUndo = sharedViewModel.contentState.undoState.canUndo,
                    onEdit = { sharedViewModel.onNoteEvent(NoteEvent.Edit(it)) },
                    onScanButtonClick = onScanTextClick,
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
                    })
            }
        }, snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            AnimatedContent(targetState = isSearching, contentAlignment = Alignment.TopCenter) {
                if (it) FindAndReplaceField(
                    searchWord = searchWord,
                    replaceWord = replaceWord,
                    matchCount = matchCount,
                    onSearchWordChange = { searchWord = it },
                    onReplaceWordChange = { replaceWord = it },
                    replaceFirst = {
                        sharedViewModel.replaceFirstSearchWordByReplaceWord(
                            searchWord, replaceWord
                        )
                    },
                    replaceAll = {
                        sharedViewModel.replaceSearchWordByReplaceWord(
                            searchWord, replaceWord
                        )
                    })
                else Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    // 标题文本
                    BasicTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isTitleFocused = it.isFocused },
                        state = sharedViewModel.titleState,
                        readOnly = isReadView,
                        lineLimits = TextFieldLineLimits.SingleLine,
                        textStyle = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        decorator = { innerTextField ->
                            Box {
                                if (sharedViewModel.titleState.text.isEmpty()) {
                                    Text(
                                        text = stringResource(id = R.string.title),
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        })

                    // 笔记信息
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = """${stringResource(R.string.edited)}$timestamp""",
                            style = MaterialTheme.typography.titleSmall.copy(
                                lineHeightStyle = LineHeightStyle(
                                    trim = LineHeightStyle.Trim.None,
                                    alignment = LineHeightStyle.Alignment.Proportional
                                )
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Text(
                            text = if (noteState.isStandard) stringResource(R.string.standard_mode) else stringResource(
                                R.string.lite_mode
                            ),
                            style = MaterialTheme.typography.titleSmall.copy(
                                lineHeightStyle = LineHeightStyle(
                                    trim = LineHeightStyle.Trim.None,
                                    alignment = LineHeightStyle.Alignment.Proportional
                                )
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            )

            /*-------------------------------------------------*/

            if (!noteState.isStandard) LiteTextField(
                modifier = Modifier.fillMaxSize(),
                readMode = isReadView,
                content = sharedViewModel.contentState.text.toString(),
                searchWord = searchWord,
                onTextChange = {
                    sharedViewModel.onNoteEvent(
                        NoteEvent.Edit(
                            Constants.Editor.NEW_TEXT, it
                        )
                    )
                },
                onFocusChanged = { isContentFocused = it },
                onScanButtonClick = onScanTextClick
            )
            else {

                val scrollState = rememberScrollState()

                if (isLargeScreen) {

                    var textFieldWeight by remember { mutableFloatStateOf(0.5f) }
                    val windowWidth = currentWindowSize().width.toFloat()

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StandardTextField(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(textFieldWeight),
                            readMode = isReadView,
                            state = sharedViewModel.contentState,
                            scrollState = scrollState,
                            searchWord = searchWord,
                            onScanButtonClick = onScanTextClick,
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
                            onFocusChanged = { isContentFocused = it })

                        Icon(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .draggable(state = rememberDraggableState { delta ->
                                    textFieldWeight =
                                        (textFieldWeight + delta / windowWidth).coerceIn(
                                            0.3f, 0.7f
                                        )
                                }, orientation = Orientation.Horizontal, onDragStopped = {
                                    val positions = listOf(1f / 3f, 0.5f, 2f / 3f)
                                    val closest =
                                        positions.minByOrNull { abs(it - textFieldWeight) }
                                    if (closest != null) {
                                        textFieldWeight = closest
                                    }
                                }),
                            imageVector = Icons.Default.DragIndicator,
                            contentDescription = "DragIndicator"
                        )

                        ReadView(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f - textFieldWeight),
                            html = html,
                            scrollState = scrollState,
                            scrollSynchronized = isEditorAndPreviewSynced
                        )
                    }
                } else HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    userScrollEnabled = false,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) { page: Int ->
                    when (page) {
                        0 -> {
                            StandardTextField(
                                modifier = Modifier.fillMaxSize(),
                                state = sharedViewModel.contentState,
                                readMode = isReadView,
                                scrollState = scrollState,
                                searchWord = searchWord,
                                onScanButtonClick = onScanTextClick,
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
                                onFocusChanged = { isContentFocused = it })
                        }

                        1 -> {
                            ReadView(
                                modifier = Modifier.fillMaxSize(),
                                html = html,
                                scrollSynchronized = isEditorAndPreviewSynced,
                                scrollState = scrollState
                            )
                        }
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        ExportDialog(onDismissRequest = { showExportDialog = false }, onConfirm = {
            sharedViewModel.onDatabaseEvent(
                DatabaseEvent.ExportFile(
                    context.contentResolver, listOf(
                        NoteEntity(
                            title = sharedViewModel.titleState.text.toString(),
                            content = sharedViewModel.contentState.text.toString(),
                            timestamp = System.currentTimeMillis()
                        )
                    ), it
                )
            )
            showExportDialog = false
        })
    }

    if (showShareDialog) {
        val clipboardManager = LocalClipboardManager.current
        ShareDialog(onDismissRequest = { showShareDialog = false }, onConfirm = {
            when (it) {
                ShareType.COPY -> {
                    val clipData = ClipData.newPlainText(
                        "Markdown", sharedViewModel.contentState.text.toString()
                    )
                    val clipEntry = ClipEntry(clipData)
                    clipboardManager.setClip(clipEntry)
                    // Only show a toast for Android 12 and lower.
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) Toast.makeText(
                        context, "Markdown 📋", Toast.LENGTH_SHORT
                    ).show()
                }

                ShareType.TEXT -> {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(
                            Intent.EXTRA_TITLE, sharedViewModel.titleState.text.toString()
                        )
                        putExtra(
                            Intent.EXTRA_TEXT, sharedViewModel.contentState.text.toString()
                        )
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                }

                ShareType.FILE -> {
                    val fileName =
                        sharedViewModel.titleState.text.toString() + if (noteState.isStandard) ".md" else ".txt"
                    val file = File(context.applicationContext.cacheDir, fileName)
                    val fileContent = sharedViewModel.contentState.text.toString()
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
            }
            showShareDialog = false
        })
    }

    if (showTableDialog) {
        TableDialog(onDismissRequest = { showTableDialog = false }) { row, column ->
            sharedViewModel.onNoteEvent(NoteEvent.Edit(Constants.Editor.TABLE, "$row,$column"))
        }
    }

    if (showListDialog) {
        ListDialog(onDismissRequest = { showListDialog = false }) {
            sharedViewModel.onNoteEvent(
                NoteEvent.Edit(
                    Constants.Editor.LIST, it.fastJoinToString(separator = "\n")
                )
            )
        }
    }

    if (showTaskDialog) {
        TaskDialog(onDismissRequest = { showTaskDialog = false }) {
            sharedViewModel.addTasks(it)
        }
    }

    if (showLinkDialog) {
        LinkDialog(onDismissRequest = { showLinkDialog = false }) { name, uri ->
            val insertText = "[${name}](${uri})"
            sharedViewModel.onNoteEvent(NoteEvent.Edit(Constants.Editor.TEXT, insertText))
        }
    }

    if (showFolderDialog) {
        FolderListDialog(
            hint = stringResource(R.string.destination_folder),
            oFolderId = noteState.folderId,
            folders = folderNoteCounts.map { it.first },
            onDismissRequest = { showFolderDialog = false }) {
            sharedViewModel.onNoteEvent(NoteEvent.FolderChanged(it))
        }
    }

    ProgressDialog(
        isLoading = actionState.loading,
        progress = actionState.progress,
        message = actionState.message,
        onDismissRequest = sharedViewModel::cancelDataAction
    )
}
