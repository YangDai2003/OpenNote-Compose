package com.yangdai.opennote.presentation.component.main

import android.icu.text.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandIn
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R
import com.yangdai.opennote.data.local.entity.NoteEntity

private object NoteCardDefaults {
    val MIN_INTERACTION_HEIGHT = 48.dp
    val CARD_PADDING = 16.dp
    val ICON_SIZE = 8.dp
    val CONTENT_PADDING = 10.dp
    val VERTICAL_PADDING = 8.dp
    val HORIZONTAL_PADDING = 10.dp
}

@Composable
private fun NoteCardHeader(
    formattedTimestamp: String,
    isMarkdown: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = NoteCardDefaults.VERTICAL_PADDING)
    ) {
        Icon(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(NoteCardDefaults.ICON_SIZE),
            imageVector = Icons.Default.Circle,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = null
        )

        Text(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = NoteCardDefaults.CARD_PADDING),
            text = formattedTimestamp,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            modifier = Modifier.align(Alignment.CenterEnd),
            text = stringResource(if (isMarkdown) R.string.standard_mode else R.string.lite_mode),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun NoteCardContent(
    displayedNote: NoteEntity,
    contentTextOverflow: TextOverflow,
    contentMaxLines: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = NoteCardDefaults.VERTICAL_PADDING,
                horizontal = NoteCardDefaults.HORIZONTAL_PADDING
            )
    ) {
        if (displayedNote.title.isNotEmpty()) {
            Text(
                modifier = Modifier.basicMarquee(),
                text = displayedNote.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )

            if (displayedNote.content.isNotEmpty()) {
                Spacer(modifier = Modifier.height(NoteCardDefaults.VERTICAL_PADDING))
            }
        }

        Text(
            text = displayedNote.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            overflow = contentTextOverflow,
            maxLines = contentMaxLines
        )
    }
}

@Composable
fun AdaptiveNoteCard(
    modifier: Modifier = Modifier,
    isListView: Boolean,
    displayedNote: NoteEntity,
    dateFormatter: DateFormat,
    contentMaxLines: Int,
    contentTextOverflow: TextOverflow,
    isEditMode: Boolean,
    isNoteSelected: Boolean,
    onSelectNote: (NoteEntity) -> Unit,
    onEditModeChange: (Boolean) -> Unit
) = Column(modifier) {
    AnimatedVisibility(
        visible = isListView,
        enter = expandIn(expandFrom = Alignment.CenterStart),
        exit = ExitTransition.None
    ) {
        NoteCardHeader(
            formattedTimestamp = dateFormatter.format(displayedNote.timestamp),
            isMarkdown = displayedNote.isMarkdown
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isListView) Modifier.padding(start = NoteCardDefaults.CARD_PADDING) else Modifier),
        colors = if (isNoteSelected) CardDefaults.outlinedCardColors() else CardDefaults.elevatedCardColors(),
        border = if (isNoteSelected) CardDefaults.outlinedCardBorder() else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = NoteCardDefaults.MIN_INTERACTION_HEIGHT)
                .combinedClickable(
                    onLongClick = { onEditModeChange(true) },
                    onClick = { onSelectNote(displayedNote) }
                )
        ) {
            if (isEditMode) {
                Checkbox(
                    checked = isNoteSelected,
                    onCheckedChange = null,
                    modifier = Modifier
                        .padding(NoteCardDefaults.CONTENT_PADDING)
                        .align(Alignment.TopEnd)
                )
            }
            NoteCardContent(
                displayedNote = displayedNote,
                contentTextOverflow = contentTextOverflow,
                contentMaxLines = contentMaxLines
            )
        }
    }
}
