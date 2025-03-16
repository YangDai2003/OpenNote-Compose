package com.yangdai.opennote.presentation.component.setting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yangdai.opennote.R
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.presentation.component.main.AdaptiveNoteCard
import com.yangdai.opennote.presentation.component.main.Timeline
import com.yangdai.opennote.presentation.state.ListNoteContentDisplayMode
import com.yangdai.opennote.presentation.state.ListNoteContentDisplayMode.Companion.toInt
import com.yangdai.opennote.presentation.state.ListNoteContentOverflowStyle
import com.yangdai.opennote.presentation.state.ListNoteContentOverflowStyle.Companion.toInt
import com.yangdai.opennote.presentation.state.ListNoteContentSize
import com.yangdai.opennote.presentation.state.ListNoteContentSize.Companion.toInt
import com.yangdai.opennote.presentation.util.Constants
import com.yangdai.opennote.presentation.util.rememberDateTimeFormatter
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel

@Composable
fun ListPane(sharedViewModel: SharedViewModel) {

    val settingsState by sharedViewModel.settingsStateFlow.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {

        AnimatedVisibility(
            visible = settingsState.isListView,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Timeline(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .padding(start = 8.dp),
                thickness = 2.dp
            )
        }

        val contentPadding = remember(settingsState.isListView) {
            if (!settingsState.isListView) PaddingValues(
                start = 16.dp,
                end = 16.dp
            ) else PaddingValues(
                start = 5.dp,
                end = 16.dp
            )
        }

        val state = rememberLazyStaggeredGridState()
        val textOverflow = remember(settingsState.enumOverflowStyle) {
            when (settingsState.enumOverflowStyle) {
                ListNoteContentOverflowStyle.CLIP -> TextOverflow.Clip
                else -> TextOverflow.Ellipsis
            }
        }
        val maxLines = remember(settingsState.enumContentSize) {
            when (settingsState.enumContentSize) {
                ListNoteContentSize.DEFAULT -> 12
                ListNoteContentSize.COMPACT -> 6
                else -> Int.MAX_VALUE
            }
        }
        val isRaw by remember {
            derivedStateOf {
                settingsState.enumDisplayMode == ListNoteContentDisplayMode.RAW
            }
        }
        val dateTimeFormatter = rememberDateTimeFormatter()
        LazyVerticalStaggeredGrid(
            modifier = Modifier.fillMaxSize(),
            state = state,
            // The staggered grid layout is adaptive, with a minimum column width of 160dp(mdpi)
            columns = if (!settingsState.isListView) StaggeredGridCells.Adaptive(160.dp)
            else StaggeredGridCells.Fixed(1),
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            // for better edgeToEdge experience
            contentPadding = contentPadding,
            content = {
                items(
                    items = noteSamples,
                    contentType = { item: NoteEntity -> item }
                ) { note ->
                    AdaptiveNoteCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(),
                        isListView = settingsState.isListView,
                        displayedNote = note,
                        dateFormatter = dateTimeFormatter,
                        contentMaxLines = maxLines,
                        contentTextOverflow = textOverflow,
                        isRaw = isRaw,
                        isEditMode = false,
                        isNoteSelected = false,
                        onEditModeChange = { },
                        onSelectNote = { }
                    )
                }
            }
        )

        val displayStyles = listOf(
            stringResource(R.string.raw),
            stringResource(R.string.preview)
        )
        val overflowStyles = listOf(
            stringResource(R.string.ellipsis),
            stringResource(R.string.clip)
        )
        val sizeStyles = listOf(
            stringResource(R.string.default_size),
            stringResource(R.string.compact),
            stringResource(R.string.flat)
        )

        AnimatedVisibility(
            !state.isScrollInProgress,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val hapticFeedback = LocalHapticFeedback.current
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)
                        .copy(alpha = 0.6f)
                )
            ) {
                SettingsHeader(stringResource(R.string.display_style))
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                ) {
                    displayStyles.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = displayStyles.size
                            ),
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                sharedViewModel.putPreferenceValue(
                                    Constants.Preferences.ENUM_DISPLAY_MODE,
                                    index
                                )
                            },
                            selected = index == settingsState.enumDisplayMode.toInt(),
                            label = {
                                Text(label)
                            },
                        )
                    }
                }
                SettingsHeader(stringResource(R.string.text_overflow))
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                ) {
                    overflowStyles.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = overflowStyles.size
                            ),
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                sharedViewModel.putPreferenceValue(
                                    Constants.Preferences.ENUM_OVERFLOW_STYLE,
                                    index
                                )
                            },
                            selected = index == settingsState.enumOverflowStyle.toInt(),
                            label = {
                                Text(label)
                            },
                        )
                    }
                }
                SettingsHeader(stringResource(R.string.card_size))
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .padding(horizontal = 8.dp),
                ) {
                    sizeStyles.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = sizeStyles.size
                            ),
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                sharedViewModel.putPreferenceValue(
                                    Constants.Preferences.ENUM_CONTENT_SIZE,
                                    index
                                )
                            },
                            selected = index == settingsState.enumContentSize.toInt(),
                            label = {
                                Text(label)
                            },
                        )
                    }
                }
            }
        }
    }
}

private val noteSamples = listOf(
    // 笔记内容很长, 英语
    NoteEntity(
        title = "Sample Note 1",
        content = """This is a **very long** note content for sample note 1. 
            |It's designed to _test_ the display of long text when it _overflows_. 
            |> Note: ++**"The quick brown fox jumps over the lazy dog"**++ is a pangram, a sentence that uses every letter of the English alphabet at least once. 
            |This makes it ideal for testing text rendering, as it ensures that all characters are displayed ==correctly==.
            |It's also useful for evaluating how the UI handles different letter shapes and sizes.
            |Repeated content for testing text overflow: 
            |- The quick brown fox jumps over the lazy dog. 
            |- The quick brown fox jumps over the lazy dog. 
            |- The quick brown fox jumps over the lazy dog. 
            |- The quick brown fox jumps over the lazy dog. 
            |- The quick brown fox jumps over the lazy dog. 
            |- The quick brown fox jumps over the lazy dog. 
            |- The quick brown fox jumps over the lazy dog. 
            |- The quick brown fox jumps over the lazy dog. 
            |- The quick brown fox jumps over the lazy dog. 
            |- The quick brown fox jumps over the lazy dog. 
            |- The quick brown fox jumps over the lazy dog. 
            |- The quick brown fox jumps over the lazy dog. 
            |- The quick brown fox jumps over the lazy dog.
            |The End.""".trimMargin(),
        isMarkdown = true,
        timestamp = System.currentTimeMillis()
    ),
    // 笔记标题为空, 中文
    NoteEntity(
        title = "",
        content = "这是第二个笔记的示例内容，用来展示当~~笔记标题为空~~时的情况。",
        isMarkdown = true,
        timestamp = System.currentTimeMillis() - 1000
    ),
    // 笔记isMarkdown为false, 德语
    NoteEntity(
        title = "Beispielnotiz 3",
        content = "Dies ist der Inhalt der Beispielnotiz 3. Hier wird getestet, was passiert, wenn isMarkdown auf false gesetzt ist.",
        isMarkdown = false,
        timestamp = System.currentTimeMillis() - 2000
    ),
    // 笔记内容为空, 土耳其语
    NoteEntity(
        title = "Örnek Not 4",
        content = "",
        isMarkdown = true,
        timestamp = System.currentTimeMillis() - 3000,
    )
)
