package com.yangdai.opennote.presentation.screen

import android.content.Intent
import android.widget.Toast
import android.provider.CalendarContract.Events
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
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
import com.yangdai.opennote.presentation.navigation.Route
import com.yangdai.opennote.presentation.util.timestampToFormatLocalDateTime
import com.yangdai.opennote.presentation.viewmodel.NoteScreenViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    viewModel: NoteScreenViewModel = hiltViewModel(),
    navController: NavHostController,
    isLargeScreen: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val html by viewModel.html.collectAsStateWithLifecycle()

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

    LaunchedEffect(state.folderId) {
        folderName = if (state.folderId == null) {
            context.getString(R.string.all_notes)
        } else {
            val matchingFolder = state.folders.find { it.id == state.folderId }
            matchingFolder?.name ?: ""
        }
    }

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }

    // Get the scanned text from the camerax screen
    val scannedText =
        navController.currentBackStackEntry?.savedStateHandle?.get<String>("scannedText")

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
                is UiEvent.NavigateBack -> navController.navigateUp()
            }
        }
    }

    // Save the note when the lifecycle owner is destroyed
    DisposableEffect(key1 = lifecycleOwner) {

        onDispose {
            viewModel.onEvent(NoteEvent.NavigateBack)
        }
    }

    val time by lazy {
        if (state.timestamp == null) timestampToFormatLocalDateTime(System.currentTimeMillis())
        else timestampToFormatLocalDateTime(state.timestamp!!)
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
                        navController.navigateUp()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {

                    IconButton(onClick = { viewModel.onEvent(NoteEvent.SwitchType) }) {
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
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
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
                            onClick = { viewModel.onEvent(NoteEvent.Delete) })

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
                                    putExtra(Events.TITLE, state.title)
                                    putExtra(Events.DESCRIPTION, state.content)
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
                                    putExtra(Intent.EXTRA_TITLE, state.title)
                                    putExtra(Intent.EXTRA_TEXT, state.content)
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
                    isMarkdown = state.isMarkdown,
                    canRedo = viewModel.canRedo(),
                    canUndo = viewModel.canUndo(),
                    onEdit = { viewModel.onEvent(NoteEvent.Edit(it)) },
                    onScanButtonClick = { navController.navigate(Route.CAMERAX) },
                    onTaskButtonClick = { showTaskDialog = true },
                    onLinkButtonClick = { showLinkDialog = true })
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {

            BasicTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.title,
                readOnly = isReadMode,
                onValueChange = {
                    viewModel.onEvent(NoteEvent.TitleChanged(it))
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                decorationBox = { innerTextField ->
                    Box {
                        if (state.title.isEmpty()) {
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
                    text = if (state.isMarkdown) "MARKDOWN" else stringResource(R.string.rich_text),
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
                        state = viewModel.textFieldState,
                        readMode = isReadMode
                    )

                    Box(
                        Modifier
                            .fillMaxHeight()
                            .weight(1f)
                    ) {
                        if (state.isMarkdown) {
                            HtmlView(html = html)
                        } else {
                            HighlightedClickableText(viewModel.textFieldState.text.toString())
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
                                state = viewModel.textFieldState,
                                readMode = isReadMode
                            )
                        }

                        1 -> {
                            if (state.isMarkdown) {
                                HtmlView(html = html)
                            } else {
                                HighlightedClickableText(viewModel.textFieldState.text.toString())
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
            title = state.title,
            content = viewModel.textFieldState.text.toString(),
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
            oFolderId = state.folderId,
            folders = state.folders.toImmutableList(),
            sheetState = sheetState,
            onDismissRequest = { showBottomSheet = false },
            onCloseClick = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        showBottomSheet = false
                    }
                }
            }) {
            viewModel.onEvent(NoteEvent.FolderChanged(it))
        }
    }
}
