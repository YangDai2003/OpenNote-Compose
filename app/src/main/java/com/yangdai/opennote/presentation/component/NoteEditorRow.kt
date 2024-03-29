package com.yangdai.opennote.presentation.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatPaint
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
    onScanButtonClick: () -> Unit,
    onTaskButtonClick: () -> Unit,
    onLinkButtonClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {

        HorizontalDivider(
            Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceDim,
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
                onClick = { onEdit(Constants.EDITOR_UNDO) },
                enabled = canUndo
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Undo,
                    contentDescription = "Undo"
                )
            }

            IconButton(
                onClick = { onEdit(Constants.EDITOR_REDO) },
                enabled = canRedo
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Redo,
                    contentDescription = "Redo"
                )
            }

            if (isMarkdown) {

                IconButton(onClick = { onEdit(Constants.EDITOR_TITLE) }) {
                    Icon(
                        imageVector = Icons.Outlined.Title,
                        contentDescription = "Title"
                    )
                }

                IconButton(onClick = { onEdit(Constants.EDITOR_BOLD) }) {
                    Icon(
                        imageVector = Icons.Outlined.FormatBold,
                        contentDescription = "Bold"
                    )
                }

                IconButton(onClick = { onEdit(Constants.EDITOR_ITALIC) }) {
                    Icon(
                        imageVector = Icons.Outlined.FormatItalic,
                        contentDescription = "Italic"
                    )
                }

                IconButton(onClick = { onEdit(Constants.EDITOR_UNDERLINE) }) {
                    Icon(
                        imageVector = Icons.Outlined.FormatUnderlined,
                        contentDescription = "Underline"
                    )
                }

                IconButton(onClick = { onEdit(Constants.EDITOR_STRIKETHROUGH) }) {
                    Icon(
                        imageVector = Icons.Default.FormatStrikethrough,
                        contentDescription = "Strikethrough"
                    )
                }

                IconButton(onClick = { onEdit(Constants.EDITOR_MARK) }) {
                    Icon(
                        imageVector = Icons.Outlined.FormatPaint,
                        contentDescription = "Mark"
                    )
                }

                IconButton(onClick = { onEdit(Constants.EDITOR_INLINE_CODE) }) {
                    Icon(
                        imageVector = Icons.Outlined.Code,
                        contentDescription = "Code"
                    )
                }

                IconButton(onClick = { onEdit(Constants.EDITOR_INLINE_FUNC) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.function),
                        contentDescription = "Function"
                    )
                }

                IconButton(onClick = { onEdit(Constants.EDITOR_QUOTE) }) {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = "Quote"
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
