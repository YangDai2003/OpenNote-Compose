package com.yangdai.opennote.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatPaint
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yangdai.opennote.ui.navigation.Route
import com.yangdai.opennote.ui.state.NoteState
import com.yangdai.opennote.ui.viewmodel.NoteScreenViewModel

@Composable
fun NoteEditorRow(
    isReadMode: Boolean,
    noteState: NoteState,
    viewModel: NoteScreenViewModel,
    navController: NavController,
    onTaskButtonClick: () -> Unit,
    onLinkButtonClick: () -> Unit
) {
    if (!isReadMode) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .height(48.dp)
                .horizontalScroll(rememberScrollState())
        ) {

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

                if (noteState.isMarkdown) {
                    IconButton(onClick = { viewModel.bold() }) {
                        Icon(
                            imageVector = Icons.Outlined.FormatBold,
                            contentDescription = "Bold"
                        )
                    }

                    IconButton(onClick = { viewModel.italic() }) {
                        Icon(
                            imageVector = Icons.Outlined.FormatItalic,
                            contentDescription = "Italic"
                        )
                    }

                    IconButton(onClick = { viewModel.underline() }) {
                        Icon(
                            imageVector = Icons.Outlined.FormatUnderlined,
                            contentDescription = "Underline"
                        )
                    }

                    IconButton(onClick = { viewModel.mark() }) {
                        Icon(
                            imageVector = Icons.Outlined.FormatPaint,
                            contentDescription = "Mark"
                        )
                    }

                    IconButton(onClick = { viewModel.inlineCode() }) {
                        Icon(
                            imageVector = Icons.Outlined.Code,
                            contentDescription = "Code"
                        )
                    }
                }

                IconButton(onClick = onTaskButtonClick) {
                    Icon(
                        imageVector = Icons.Outlined.CheckBox,
                        contentDescription = "Task"
                    )
                }

                IconButton(onClick = onLinkButtonClick) {
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