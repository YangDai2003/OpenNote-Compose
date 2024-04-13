package com.yangdai.opennote.presentation.screen

import android.content.Intent
import android.widget.Toast
import android.provider.CalendarContract.Events
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.component.ExportDialog
import com.yangdai.opennote.presentation.component.FolderListSheet
import com.yangdai.opennote.presentation.component.HighlightedClickableText
import com.yangdai.opennote.presentation.component.HtmlView
import com.yangdai.opennote.presentation.component.LinkDialog
import com.yangdai.opennote.presentation.component.NoteEditTextField
import com.yangdai.opennote.presentation.component.NoteEditorRow
import com.yangdai.opennote.presentation.component.TaskDialog
import com.yangdai.opennote.presentation.event.NoteEvent
import com.yangdai.opennote.presentation.event.UiEvent
import com.yangdai.opennote.presentation.util.timestampToFormatLocalDateTime
import com.yangdai.opennote.presentation.viewmodel.MainRouteScreenViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    viewModel: MainRouteScreenViewModel,
    isLargeScreen: Boolean,
    id: String?,
    scannedText: String?,
    navigateUp: () -> Unit,
    onScanTextClick: () -> Unit
) {

    LaunchedEffect(id) {
        viewModel.setLoading(true)
        if (id == null) viewModel.setLoading(false)
        else {
            if (id.isNotEmpty()) viewModel.getNoteById(id.toLong())
            else viewModel.setLoading(false)
        }
    }

    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val noteState by viewModel.noteStateFlow.collectAsStateWithLifecycle()
    val listState by viewModel.listStateFlow.collectAsStateWithLifecycle()
    val html by viewModel.html.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(pageCount = { 2 })

    // Switch between read mode and edit mode
    var isReadMode by rememberSaveable {
        mutableStateOf(false)
    }

    // Whether to show the link dialog
    var showLinkDialog by rememberSaveable {
        mutableStateOf(false)
    }

    // Whether to show the task dialog
    var showTaskDialog by rememberSaveable {
        mutableStateOf(false)
    }

    // Whether to show the export dialog
    var showExportDialog by rememberSaveable {
        mutableStateOf(false)
    }

    // Whether to show the overflow menu
    var showMenu by rememberSaveable {
        mutableStateOf(false)
    }

    // Folder name, default to "All Notes", or the name of the current folder the note is in
    var folderName by rememberSaveable {
        mutableStateOf("")
    }

    LaunchedEffect(noteState.folderId) {
        folderName = if (noteState.folderId == null) {
            context.getString(R.string.all_notes)
        } else {
            val matchingFolder = listState.folders.find { it.id == noteState.folderId }
            matchingFolder?.name ?: ""
        }
    }

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(scannedText) {
        if (scannedText != null) {
            if (scannedText.isNotEmpty()) {
                viewModel.addScannedText(scannedText)
            }
        }
    }

    LaunchedEffect(isReadMode) {
        if (isReadMode) {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
        if (!isLargeScreen) {
            scope.launch {
                pagerState.animateScrollToPage(if (isReadMode) 1 else 0)
            }
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.event.collect { event ->
            when (event) {
                is UiEvent.NavigateBack -> navigateUp()
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            viewModel.onNoteEvent(NoteEvent.NavigateBack)
        }
    }

    var time by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(noteState.timestamp) {
        time =
            if (noteState.timestamp == null) timestampToFormatLocalDateTime(System.currentTimeMillis())
            else timestampToFormatLocalDateTime(noteState.timestamp!!)
    }

    var isTitleFocused by rememberSaveable { mutableStateOf(false) }
    var isContentFocused by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = isTitleFocused || isContentFocused) {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    BackHandler(isReadMode) {
        if (isReadMode) {
            focusManager.clearFocus()
            isReadMode = false
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    FilledTonalButton(
                        modifier = Modifier.sizeIn(maxWidth = 160.dp),
                        onClick = {
                            showBottomSheet = true
                        }) {
                        Text(text = folderName, maxLines = 1, modifier = Modifier.basicMarquee())
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        navigateUp()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {

                    IconButton(onClick = { viewModel.onNoteEvent(NoteEvent.SwitchType) }) {
                        Icon(
                            imageVector = Icons.Outlined.SwapHoriz,
                            contentDescription = "Switch Note Type"
                        )
                    }

                    IconButton(onClick = { isReadMode = !isReadMode }) {
                        Icon(
                            imageVector = if (!isReadMode) Icons.AutoMirrored.Outlined.MenuBook
                            else Icons.Outlined.EditNote,
                            contentDescription = "Mode"
                        )
                    }

                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "More"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {

                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete"
                                )
                            },
                            text = { Text(text = stringResource(id = R.string.delete)) },
                            onClick = { viewModel.onNoteEvent(NoteEvent.Delete) })

                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Alarm,
                                    contentDescription = "Remind"
                                )
                            },
                            text = { Text(text = stringResource(id = R.string.remind)) },
                            onClick = {
                                val intent = Intent(Intent.ACTION_INSERT).apply {
                                    data = Events.CONTENT_URI
                                    putExtra(Events.TITLE, viewModel.titleState.text.toString())
                                    putExtra(
                                        Events.DESCRIPTION,
                                        viewModel.contentState.text.toString()
                                    )
                                }
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                } else {
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
                                    imageVector = Icons.Outlined.FileDownload,
                                    contentDescription = "Export"
                                )
                            },
                            text = { Text(text = stringResource(R.string.export)) },
                            onClick = {
                                showExportDialog = true
                            })

                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.IosShare,
                                    contentDescription = "Share"
                                )
                            },
                            text = { Text(text = stringResource(R.string.share)) },
                            onClick = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TITLE,
                                        viewModel.titleState.text.toString()
                                    )
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        viewModel.contentState.text.toString()
                                    )
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                            })
                    }
                })
        },
        bottomBar = {
            AnimatedVisibility(
                visible = !isReadMode,
                enter = slideInVertically { fullHeight -> fullHeight },
                exit = slideOutVertically { fullHeight -> fullHeight }) {
                NoteEditorRow(
                    isMarkdown = noteState.isMarkdown,
                    canRedo = viewModel.canRedo(),
                    canUndo = viewModel.canUndo(),
                    onEdit = { viewModel.onNoteEvent(NoteEvent.Edit(it)) },
                    onScanButtonClick = onScanTextClick,
                    onTaskButtonClick = { showTaskDialog = true },
                    onLinkButtonClick = { showLinkDialog = true })
            }
        }
    ) { paddingValues ->

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.TopCenter
            ) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {

                BasicTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            isTitleFocused = it.isFocused
                        },
                    state = viewModel.titleState,
                    readOnly = isReadMode,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    textStyle = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    decorator = { innerTextField ->
                        Box {
                            if (viewModel.titleState.text.isEmpty()) {
                                Text(
                                    text = stringResource(id = R.string.title),
                                    style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = """${stringResource(R.string.edited)}$time""",
                        style = MaterialTheme.typography.titleSmall.copy(
                            lineHeightStyle = LineHeightStyle(
                                trim = LineHeightStyle.Trim.None,
                                alignment = LineHeightStyle.Alignment.Proportional
                            )
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Text(
                        text = if (noteState.isMarkdown) "MARKDOWN" else stringResource(R.string.rich_text),
                        style = MaterialTheme.typography.titleSmall.copy(
                            lineHeightStyle = LineHeightStyle(
                                trim = LineHeightStyle.Trim.None,
                                alignment = LineHeightStyle.Alignment.Proportional
                            )
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                if (isLargeScreen) {
                    Row(Modifier.fillMaxSize()) {
                        NoteEditTextField(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                            state = viewModel.contentState,
                            readMode = isReadMode
                        ) {
                            isContentFocused = it
                        }

                        Box(
                            Modifier
                                .fillMaxHeight()
                                .weight(1f)
                        ) {
                            if (noteState.isMarkdown) {
                                HtmlView(html = html)
                            } else {
                                HighlightedClickableText(viewModel.contentState.text.toString())
                            }
                        }
                    }

                } else {
                    HorizontalPager(
                        state = pagerState,
                        beyondViewportPageCount = 1,
                        userScrollEnabled = false,
                        modifier = Modifier.fillMaxSize()
                    ) { page: Int ->
                        when (page) {
                            0 -> {
                                NoteEditTextField(
                                    modifier = Modifier.fillMaxSize(),
                                    state = viewModel.contentState,
                                    readMode = isReadMode
                                ) {
                                    isContentFocused = it
                                }
                            }

                            1 -> {
                                if (noteState.isMarkdown) {
                                    HtmlView(html = html)
                                } else {
                                    HighlightedClickableText(viewModel.contentState.text.toString())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (showExportDialog) {
        ExportDialog(
            html = html,
            title = viewModel.titleState.text.toString(),
            content = viewModel.contentState.text.toString(),
            onDismissRequest = { showExportDialog = false }
        )
    }

    if (showTaskDialog) {
        TaskDialog(onDismissRequest = { showTaskDialog = false }) {
            viewModel.addTask(it.task, it.checked)
        }
    }

    if (showLinkDialog) {
        LinkDialog(onDismissRequest = { showLinkDialog = false }) {
            val insertText = "[${it.title}](${it.uri})"
            viewModel.addLink(insertText)
        }
    }

    if (showBottomSheet) {
        FolderListSheet(
            oFolderId = noteState.folderId,
            folders = listState.folders.toImmutableList(),
            sheetState = sheetState,
            onDismissRequest = { showBottomSheet = false },
            onCloseClick = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        showBottomSheet = false
                    }
                }
            }) {
            viewModel.onNoteEvent(NoteEvent.FolderChanged(it))
        }
    }

}
