package com.yangdai.opennote.presentation.component.text

import android.content.ClipData
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.content.MediaType
import androidx.compose.foundation.content.ReceiveContentListener
import androidx.compose.foundation.content.consume
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.content.hasMediaType
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import com.yangdai.opennote.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StandardTextField(
    modifier: Modifier,
    state: TextFieldState,
    scrollState: ScrollState = rememberScrollState(),
    readMode: Boolean,
    searchWord: String,
    onScanButtonClick: () -> Unit,
    onListButtonClick: () -> Unit,
    onTableButtonClick: () -> Unit,
    onTaskButtonClick: () -> Unit,
    onLinkButtonClick: () -> Unit,
    onImageButtonClick: () -> Unit,
    onFocusChanged: (Boolean) -> Unit
) {

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val receiveContentListener = remember {
        ReceiveContentListener { transferableContent ->
            when {
                transferableContent.hasMediaType(MediaType.Text) -> {
                    transferableContent.consume { item: ClipData.Item ->
                        val hasText = item.text.isNotEmpty()
                        if (hasText) {
                            state.edit { addInNewLine(item.text.toString()) }
                        }
                        hasText
                    }
                }

                else -> transferableContent
            }
        }
    }

    BasicTextField(
        // The contentReceiver modifier is used to receive text content from the clipboard or drag-and-drop operations.
        modifier = modifier
            .contentReceiver(receiveContentListener)
            .onFocusChanged { onFocusChanged(it.isFocused) }
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    if (keyEvent.isCtrlPressed) {
                        if (keyEvent.isShiftPressed) {
                            when (keyEvent.key) {
                                Key.K -> {
                                    state.edit { inlineCode() }
                                    true
                                }

                                Key.M -> {
                                    state.edit { inlineMath() }
                                    true
                                }

                                Key.Q -> {
                                    state.edit { quote() }
                                    true
                                }

                                Key.R -> {
                                    state.edit { addRule() }
                                    true
                                }

                                Key.L -> {
                                    onListButtonClick()
                                    true
                                }

                                Key.T -> {
                                    onTaskButtonClick()
                                    true
                                }

                                Key.D -> {
                                    state.edit { addMermaid() }
                                    true
                                }

                                Key.S -> {
                                    onScanButtonClick()
                                    true
                                }

                                Key.I -> {
                                    onImageButtonClick()
                                    true
                                }

                                else -> false
                            }
                        } else {
                            when (keyEvent.key) {
                                Key.B -> {
                                    state.edit { bold() }
                                    true
                                }

                                Key.I -> {
                                    state.edit { italic() }
                                    true
                                }

                                Key.U -> {
                                    state.edit { underline() }
                                    true
                                }

                                Key.D -> {
                                    state.edit { strikeThrough() }
                                    true
                                }

                                Key.M -> {
                                    state.edit { mark() }
                                    true
                                }

                                Key.T -> {
                                    onTableButtonClick()
                                    true
                                }

                                Key.K -> {
                                    onLinkButtonClick()
                                    true
                                }

                                Key.NumPad1 -> {
                                    state.edit { addHeader(1) }
                                    true
                                }

                                Key.NumPad2 -> {
                                    state.edit { addHeader(2) }
                                    true
                                }

                                Key.NumPad3 -> {
                                    state.edit { addHeader(3) }
                                    true
                                }

                                Key.NumPad4 -> {
                                    state.edit { addHeader(4) }
                                    true
                                }

                                Key.NumPad5 -> {
                                    state.edit { addHeader(5) }
                                    true
                                }

                                Key.NumPad6 -> {
                                    state.edit { addHeader(6) }
                                    true
                                }

                                else -> false
                            }
                        }

                    } else {
                        when (keyEvent.key) {
                            Key.DirectionLeft -> {
                                state.edit { moveCursorLeft() }
                                true
                            }

                            Key.DirectionRight -> {
                                state.edit { moveCursorRight() }
                                true
                            }

                            else -> false
                        }
                    }
                } else {
                    false
                }
            },
        scrollState = scrollState,
        readOnly = readMode,
        state = state,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        onTextLayout = { result ->
            textLayoutResult = result.invoke()
        },
        decorator = { innerTextField ->
            Box(
                modifier = Modifier
                    .clipToBounds()
                    .drawBehind {
                        textLayoutResult?.let { layoutResult ->
                            if (searchWord.isNotEmpty()) {
                                val text = state.text.toString()

                                findAllIndices(text, searchWord).forEach { (start, end) ->
                                    if (start < end && end <= text.length) {
                                        // 获取整个范围的Path
                                        val path: Path = layoutResult.getPathForRange(start, end)
                                        // 调整滚动偏移
                                        val scrollOffset = scrollState.value.toFloat()
                                        path.translate(Offset(0f, -scrollOffset))

                                        drawPath(
                                            path = path,
                                            color = Color.Cyan,
                                            alpha = 0.5f,
                                        )
                                    }
                                }
                            }
                        }
                    }
            ) {
                if (state.text.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.content),
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                innerTextField()
            }
        }
    )
}

private fun findAllIndices(text: String, word: String): List<Pair<Int, Int>> {
    val indices = mutableListOf<Pair<Int, Int>>()
    if (word.isBlank()) return indices

    var startIndex = 0
    while (startIndex <= text.length) {
        val index = text.indexOf(word, startIndex)
        if (index == -1) break
        indices.add(index to (index + word.length))
        startIndex = index + word.length
    }
    return indices
}
