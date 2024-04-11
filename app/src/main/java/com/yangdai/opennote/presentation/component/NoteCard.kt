package com.yangdai.opennote.presentation.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.data.local.entity.NoteEntity

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    modifier: Modifier = Modifier,
    note: NoteEntity,
    isEnabled: Boolean,
    isSelected: Boolean,
    onNoteClick: (NoteEntity) -> Unit,
    onEnableChange: (Boolean) -> Unit
) =
    ElevatedCard(
        modifier = modifier
            .sizeIn(minHeight = 80.dp, maxHeight = 360.dp)
            .combinedClickable(
                onLongClick = {
                    onEnableChange(true)
                },
                onClick = { onNoteClick(note) }
            ),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (isEnabled)
                Checkbox(
                    checked = isSelected, onCheckedChange = null, modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopEnd)
                )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 10.dp)
            ) {
                Text(
                    modifier = Modifier
                        .basicMarquee()
                        .padding(bottom = 12.dp),
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
