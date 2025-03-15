package com.yangdai.opennote.presentation.component.main

import android.icu.text.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandIn
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.presentation.util.parseMarkdownContent

private object NoteCardDefaults {
    val MIN_INTERACTION_HEIGHT = 48.dp
    val CARD_START_PADDING = 16.dp
    val ICON_SIZE = 8.dp
    val CHECKBOX_PADDING = 10.dp
    val HEADER_BOTTOM_PADDING = 8.dp
    val HEADER_TOP_PADDING = 4.dp
    val HORIZONTAL_PADDING = 10.dp
}

@Composable
private fun NoteCardHeader(
    formattedTimestamp: String,
    isStandard: Boolean,
    colorScheme: ColorScheme = MaterialTheme.colorScheme,
    typography: Typography = MaterialTheme.typography
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                bottom = NoteCardDefaults.HEADER_BOTTOM_PADDING,
                top = NoteCardDefaults.HEADER_TOP_PADDING
            )
    ) {
        Canvas(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(NoteCardDefaults.ICON_SIZE)
        ) {
            drawCircle(
                color = colorScheme.primary,
                radius = size.minDimension / 2
            )
        }

        BasicText(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = NoteCardDefaults.CARD_START_PADDING),
            text = formattedTimestamp,
            style = typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = ColorProducer { colorScheme.onSurfaceVariant }
        )

        BasicText(
            modifier = Modifier.align(Alignment.CenterEnd),
            text = stringResource(if (isStandard) R.string.standard_mode else R.string.lite_mode),
            style = typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = ColorProducer { colorScheme.onSurfaceVariant }
        )
    }
}

@Composable
private fun NoteCardContent(
    displayedNote: NoteEntity,
    contentTextOverflow: TextOverflow,
    contentMaxLines: Int,
    isRaw: Boolean,
    colorScheme: ColorScheme = MaterialTheme.colorScheme
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = NoteCardDefaults.HEADER_BOTTOM_PADDING,
                horizontal = NoteCardDefaults.HORIZONTAL_PADDING
            )
    ) {
        if (displayedNote.title.isNotEmpty()) {
            BasicText(
                modifier = Modifier.basicMarquee(),
                text = displayedNote.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                color = ColorProducer { colorScheme.onSurface }
            )

            if (displayedNote.content.isNotEmpty()) {
                Spacer(modifier = Modifier.height(NoteCardDefaults.HEADER_BOTTOM_PADDING))
            }
        }

        val annotatedString = remember(displayedNote.content, isRaw) {
            if (!isRaw) parseMarkdownContent(displayedNote.content)
            else AnnotatedString(displayedNote.content)
        }

        BasicText(
            text = annotatedString,
            style = MaterialTheme.typography.bodyMedium,
            overflow = contentTextOverflow,
            maxLines = contentMaxLines,
            color = ColorProducer { colorScheme.onSurfaceVariant }
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
    isRaw: Boolean,
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
            isStandard = displayedNote.isMarkdown
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isListView) Modifier.padding(start = NoteCardDefaults.CARD_START_PADDING) else Modifier),
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
                        .padding(NoteCardDefaults.CHECKBOX_PADDING)
                        .align(Alignment.TopEnd)
                )
            }
            NoteCardContent(
                displayedNote = displayedNote,
                contentTextOverflow = contentTextOverflow,
                contentMaxLines = contentMaxLines,
                isRaw = isRaw
            )
        }
    }
}
