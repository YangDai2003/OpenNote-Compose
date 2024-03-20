package com.yangdai.opennote.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.data.local.entity.NoteEntity

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(note: NoteEntity, onNoteClick: (NoteEntity) -> Unit) =
    ElevatedCard(modifier = Modifier
        .sizeIn(minHeight = 80.dp, maxHeight = 320.dp)
        .clickable {
            onNoteClick(note)
        }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                modifier = Modifier
                    .basicMarquee()
                    .padding(bottom = 8.dp),
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
