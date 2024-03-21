package com.yangdai.opennote.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R
import com.yangdai.opennote.note.NoteEvent
import com.yangdai.opennote.note.NoteState
import com.yangdai.opennote.timestampToFormatLocalDateTime
import com.yangdai.opennote.ui.components.FolderListSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    state: NoteState,
    onEvent: (NoteEvent) -> Unit
) {
    val context = LocalContext.current

    // 阅读和编辑模式切换
    var isReadMode by remember {
        mutableStateOf(false)
    }

    // 控制顶栏菜单显示状态
    var showMenu by remember {
        mutableStateOf(false)
    }

    BackHandler {
        onEvent(NoteEvent.NavigateBack)
    }

    // 记录所属文件夹名
    var folderName by remember {
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
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedButton(
                        modifier = Modifier.sizeIn(maxWidth = 160.dp),
                        onClick = {
                            showBottomSheet = true
                        }) {
                        Text(text = folderName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(NoteEvent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isReadMode = !isReadMode }) {
                        Icon(
                            imageVector = if (!isReadMode) Icons.AutoMirrored.Outlined.MenuBook
                            else Icons.Default.EditNote,
                            contentDescription = "Mode"
                        )
                    }

                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More"
                        )
                    }

                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
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
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete"
                                )
                            },
                            text = { Text(text = stringResource(id = R.string.delete)) },
                            onClick = { onEvent(NoteEvent.Delete) })
                    }
                })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {

            BasicTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.title,
                readOnly = isReadMode,
                onValueChange = { onEvent(NoteEvent.TitleChanged(it)) },
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
                else timestampToFormatLocalDateTime(state.timestamp)

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
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Date",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            BasicTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.content,
                readOnly = isReadMode,
                onValueChange = { onEvent(NoteEvent.ContentChanged(it)) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box {
                        if (state.content.isEmpty()) {
                            Text(
                                text = stringResource(id = R.string.content),
                                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        innerTextField()
                    }
                }
            )

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
