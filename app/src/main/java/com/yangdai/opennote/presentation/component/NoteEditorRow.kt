package com.yangdai.opennote.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.AddChart
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DataArray
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatPaint
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.HorizontalRule
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.util.Constants

@Composable
fun NoteEditorRow(
    isMarkdown: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onEdit: (String) -> Unit,
    onTableButtonClick: () -> Unit,
    onScanButtonClick: () -> Unit,
    onTaskButtonClick: () -> Unit,
    onLinkButtonClick: () -> Unit
) {

    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {

        HorizontalDivider(
            Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            thickness = 2.dp
        )

        Row(
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onEdit(Constants.Editor.UNDO) },
                enabled = canUndo
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Undo,
                    contentDescription = "Undo"
                )
            }

            IconButton(
                onClick = { onEdit(Constants.Editor.REDO) },
                enabled = canRedo
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Redo,
                    contentDescription = "Redo"
                )
            }

            if (isMarkdown) {

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = Icons.Outlined.Title,
                        contentDescription = "Heading Level"
                    )
                }

                AnimatedVisibility(visible = isExpanded) {
                    Row(
                        modifier = Modifier.fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(onClick = { onEdit(Constants.Editor.H1) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.format_h1),
                                contentDescription = "H1"
                            )
                        }
                        IconButton(onClick = { onEdit(Constants.Editor.H2) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.format_h2),
                                contentDescription = "H2"
                            )
                        }
                        IconButton(onClick = { onEdit(Constants.Editor.H3) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.format_h3),
                                contentDescription = "H3"
                            )
                        }
                        IconButton(onClick = { onEdit(Constants.Editor.H4) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.format_h4),
                                contentDescription = "H4"
                            )
                        }
                        IconButton(onClick = { onEdit(Constants.Editor.H5) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.format_h5),
                                contentDescription = "H5"
                            )
                        }
                        IconButton(onClick = { onEdit(Constants.Editor.H6) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.format_h6),
                                contentDescription = "H6"
                            )
                        }
                    }
                }

                IconButton(onClick = { onEdit(Constants.Editor.BOLD) }) {
                    Icon(
                        imageVector = Icons.Outlined.FormatBold,
                        contentDescription = "Bold"
                    )
                }

                IconButton(onClick = { onEdit(Constants.Editor.ITALIC) }) {
                    Icon(
                        imageVector = Icons.Outlined.FormatItalic,
                        contentDescription = "Italic"
                    )
                }

                IconButton(onClick = { onEdit(Constants.Editor.UNDERLINE) }) {
                    Icon(
                        imageVector = Icons.Outlined.FormatUnderlined,
                        contentDescription = "Underline"
                    )
                }

                IconButton(onClick = { onEdit(Constants.Editor.STRIKETHROUGH) }) {
                    Icon(
                        imageVector = Icons.Outlined.FormatStrikethrough,
                        contentDescription = "Strikethrough"
                    )
                }

                IconButton(onClick = { onEdit(Constants.Editor.MARK) }) {
                    Icon(
                        imageVector = Icons.Outlined.FormatPaint,
                        contentDescription = "Mark"
                    )
                }

                IconButton(onClick = { onEdit(Constants.Editor.INLINE_CODE) }) {
                    Icon(
                        imageVector = Icons.Outlined.Code,
                        contentDescription = "Code"
                    )
                }

                IconButton(onClick = { onEdit(Constants.Editor.INLINE_BRACKETS) }) {
                    Icon(
                        imageVector = Icons.Outlined.DataArray,
                        contentDescription = "Brackets"
                    )
                }

                IconButton(onClick = { onEdit(Constants.Editor.INLINE_BRACES) }) {
                    Icon(
                        imageVector = Icons.Outlined.DataObject,
                        contentDescription = "Braces"
                    )
                }

                IconButton(onClick = { onEdit(Constants.Editor.INLINE_FUNC) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.function),
                        contentDescription = "Function"
                    )
                }

                IconButton(onClick = { onEdit(Constants.Editor.QUOTE) }) {
                    Icon(
                        imageVector = Icons.Outlined.FormatQuote,
                        contentDescription = "Quote"
                    )
                }

                IconButton(onClick = { onEdit(Constants.Editor.RULE) }) {
                    Icon(
                        imageVector = Icons.Outlined.HorizontalRule,
                        contentDescription = "Horizontal Rule"
                    )
                }

                IconButton(onClick = onTableButtonClick) {
                    Icon(
                        imageVector = Icons.Outlined.TableChart,
                        contentDescription = "Table"
                    )
                }

                IconButton(onClick = { onEdit(Constants.Editor.DIAGRAM) }) {
                    Icon(
                        imageVector = Icons.Outlined.AddChart,
                        contentDescription = "Diagram"
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

            IconButton(onClick = onScanButtonClick) {
                Icon(
                    imageVector = Icons.Outlined.DocumentScanner,
                    contentDescription = "OCR"
                )
            }
        }
    }
}
