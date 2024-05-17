package com.yangdai.opennote.presentation.component

import android.content.ClipData
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.content.MediaType
import androidx.compose.foundation.content.consume
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.content.hasMediaType
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.util.addHeader
import com.yangdai.opennote.presentation.util.addInNewLine
import com.yangdai.opennote.presentation.util.addMermaid
import com.yangdai.opennote.presentation.util.addRule
import com.yangdai.opennote.presentation.util.bold
import com.yangdai.opennote.presentation.util.inlineCode
import com.yangdai.opennote.presentation.util.inlineMath
import com.yangdai.opennote.presentation.util.italic
import com.yangdai.opennote.presentation.util.mark
import com.yangdai.opennote.presentation.util.moveCursorLeft
import com.yangdai.opennote.presentation.util.moveCursorRight
import com.yangdai.opennote.presentation.util.quote
import com.yangdai.opennote.presentation.util.strikeThrough
import com.yangdai.opennote.presentation.util.underline

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteEditTextField(
    modifier: Modifier,
    state: TextFieldState,
    readMode: Boolean,
    onScanButtonClick: () -> Unit,
    onTableButtonClick: () -> Unit,
    onTaskButtonClick: () -> Unit,
    onLinkButtonClick: () -> Unit,
    onPreviewButtonClick: () -> Unit,
    onFocusChanged: (Boolean) -> Unit
) = BasicTextField(
    // The contentReceiver modifier is used to receive text content from the clipboard or drag-and-drop operations.
    modifier = modifier
        .contentReceiver { transferableContent ->
            if (transferableContent.hasMediaType(MediaType.Text)) {
                transferableContent.consume { item: ClipData.Item ->
                    val hasText = item.text.isNotEmpty()
                    if (hasText) {
                        state.edit { addInNewLine(item.text.toString()) }
                    }
                    hasText
                }
            }
            null
        }
        .onFocusChanged {
            onFocusChanged(it.isFocused)
        }
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

                            Key.T -> {
                                onTaskButtonClick()
                                true
                            }

                            Key.D -> {
                                state.edit { addMermaid() }
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

                            Key.S -> {
                                onScanButtonClick()
                                true
                            }

                            Key.P -> {
                                onPreviewButtonClick()
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
    readOnly = readMode,
    state = state,
    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
    decorator = { innerTextField ->
        Box {
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
