package com.yangdai.opennote.presentation.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.yangdai.opennote.presentation.util.timestampToFormatLocalDateTime

@Composable
fun AdaptiveNoteCard(
    modifier: Modifier = Modifier,
    isListView: Boolean,
    note: NoteEntity,
    maxLines: Int,
    textOverflow: TextOverflow,
    isEnabled: Boolean,
    isSelected: Boolean,
    onNoteClick: (NoteEntity) -> Unit,
    onEnableChange: (Boolean) -> Unit
) = if (isListView) ColumnNoteCard(
    modifier = modifier,
    note = note,
    maxLines = maxLines,
    textOverflow = textOverflow,
    isEnabled = isEnabled,
    isSelected = isSelected,
    onNoteClick = onNoteClick,
    onEnableChange = onEnableChange
)
else GridNoteCard(
    modifier = modifier,
    note = note,
    maxLines = maxLines,
    textOverflow = textOverflow,
    isEnabled = isEnabled,
    isSelected = isSelected,
    onNoteClick = onNoteClick,
    onEnableChange = onEnableChange
)

@Composable
private fun NoteCardContent(
    modifier: Modifier = Modifier,
    note: NoteEntity,
    textOverflow: TextOverflow,
    maxLines: Int
) = Column(
    modifier = modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp, horizontal = 10.dp)
) {
    if (note.title.isNotEmpty()) {
        Text(
            modifier = Modifier.basicMarquee(),
            text = note.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1
        )

        if (note.content.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (note.content.isNotEmpty()) {
        Text(
            text = note.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            overflow = textOverflow,
            maxLines = maxLines
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridNoteCard(
    modifier: Modifier = Modifier,
    note: NoteEntity,
    maxLines: Int,
    textOverflow: TextOverflow,
    isEnabled: Boolean,
    isSelected: Boolean,
    onNoteClick: (NoteEntity) -> Unit,
    onEnableChange: (Boolean) -> Unit
) = Card(
    modifier = modifier, colors = CardDefaults.elevatedCardColors()
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onLongClick = { onEnableChange(true) },
                onClick = { onNoteClick(note) })
    ) {
        if (isEnabled) Checkbox(
            checked = isSelected,
            onCheckedChange = null,
            modifier = Modifier
                .padding(10.dp)
                .align(Alignment.TopEnd)
        )
        NoteCardContent(note = note, textOverflow = textOverflow, maxLines = maxLines)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColumnNoteCard(
    modifier: Modifier = Modifier,
    note: NoteEntity,
    maxLines: Int,
    textOverflow: TextOverflow,
    isEnabled: Boolean,
    isSelected: Boolean,
    onNoteClick: (NoteEntity) -> Unit,
    onEnableChange: (Boolean) -> Unit
) = Column(modifier = modifier.padding(bottom = 12.dp)) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {

        Icon(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(8.dp),
            imageVector = Icons.Default.Circle,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = ""
        )

        Text(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp),
            text = note.timestamp.timestampToFormatLocalDateTime(),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            modifier = Modifier.align(Alignment.CenterEnd),
            text = stringResource(if (note.isMarkdown) R.string.standard_mode else R.string.lite_mode),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp),
        colors = CardDefaults.elevatedCardColors()
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onLongClick = {
                    onEnableChange(true)
                }, onClick = { onNoteClick(note) })
        ) {
            if (isEnabled) Checkbox(
                checked = isSelected,
                onCheckedChange = null,
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.TopEnd)
            )
            NoteCardContent(note = note, textOverflow = textOverflow, maxLines = maxLines)
        }
    }
}
