package com.yangdai.opennote.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text2.BasicTextField2
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.MoreVert
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
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.yangdai.opennote.R
import com.yangdai.opennote.Route
import com.yangdai.opennote.exportNote
import com.yangdai.opennote.ui.event.NoteEvent
import com.yangdai.opennote.timestampToFormatLocalDateTime
import com.yangdai.opennote.ui.component.FolderListSheet
import com.yangdai.opennote.ui.component.HighlightedClickableText
import com.yangdai.opennote.ui.component.LinkDialog
import com.yangdai.opennote.ui.viewmodel.NoteScreenViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteScreen(
    navController: NavHostController,
    viewModel: NoteScreenViewModel,
    onEvent: (NoteEvent) -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    // 阅读和编辑模式切换
    var isReadMode by rememberSaveable {
        mutableStateOf(false)
    }

    var showLinkDialog by rememberSaveable {
        mutableStateOf(false)
    }

    // 控制顶栏菜单显示状态
    var showMenu by rememberSaveable {
        mutableStateOf(false)
    }

    BackHandler {
        onEvent(NoteEvent.NavigateBack)
    }

    // 记录所属文件夹名
    var folderName by rememberSaveable {
        mutableStateOf("")
    }

    folderName = if (state.folderId == null) {
        stringResource(id = R.string.all_notes)
    } else {
        val matchingFolder = state.folders.find { it.id == state.folderId }
        matchingFolder?.name ?: ""
    }

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }

    val scannedText =
        navController.currentBackStackEntry?.savedStateHandle?.get<String>("scannedText")

    LaunchedEffect(scannedText) {
        if (scannedText != null) {
            if (scannedText.isNotEmpty()) {
                viewModel.addScannedText(scannedText)
            }
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
                        Icon(imageVector = Icons.Outlined.FolderOpen, contentDescription = "Folder")
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(text = folderName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onEvent(NoteEvent.NavigateBack)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
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

                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {

                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete"
                                )
                            },
                            text = { Text(text = stringResource(id = R.string.delete)) },
                            onClick = { onEvent(NoteEvent.Delete) })

                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Alarm,
                                    contentDescription = "Remind"
                                )
                            },
                            text = { Text(text = stringResource(id = R.string.remind)) },
                            onClick = {
                                val intent: Intent = Intent(Intent.ACTION_INSERT)
                                    .setData(Uri.parse("content://com.android.calendar/events"))
                                    .putExtra("title", state.title)
                                    .putExtra("description", state.content)
                                context.startActivity(intent)
                            })

                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.FileDownload,
                                    contentDescription = "Export"
                                )
                            },
                            text = { Text(text = stringResource(R.string.export_txt)) },
                            onClick = {
                                exportNote(
                                    context.applicationContext,
                                    state.title,
                                    state.content
                                )
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
            if (!isReadMode) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .height(48.dp)
                ) {
                    HorizontalDivider()
                    Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.undo() },
                            enabled = viewModel.canUndo()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Undo,
                                contentDescription = "Undo"
                            )
                        }

                        IconButton(
                            onClick = { viewModel.redo() },
                            enabled = viewModel.canRedo()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Redo,
                                contentDescription = "Redo"
                            )
                        }
                        IconButton(onClick = {
                            showLinkDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Link,
                                contentDescription = "Link"
                            )
                        }
                        IconButton(onClick = {
                            navController.navigate(Route.CAMERAX)
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.DocumentScanner,
                                contentDescription = "OCR"
                            )
                        }
                    }
                }
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
                    onEvent(NoteEvent.TitleChanged(it))
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

            val time =
                if (state.timestamp == null) timestampToFormatLocalDateTime(System.currentTimeMillis())
                else timestampToFormatLocalDateTime(state.timestamp!!)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = """${stringResource(R.string.edited)}$time""",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = "Date",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            if (isReadMode) {
                HighlightedClickableText(text = viewModel.textFieldState.text.toString())
            } else {
                BasicTextField2(
                    state = viewModel.textFieldState,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorator = { innerTextField ->
                        Box {
                            if (viewModel.textFieldState.text.isEmpty()) {
                                Text(
                                    text = stringResource(id = R.string.content),
                                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
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
                    folders = state.folders,
                    sheetState = sheetState,
                    onDismissRequest = { showBottomSheet = false },
                    onCloseClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    }) {
                    onEvent(NoteEvent.FolderChanged(it))
                }
            }
        }
    }
}
