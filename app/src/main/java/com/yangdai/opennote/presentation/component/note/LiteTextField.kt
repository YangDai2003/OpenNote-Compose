package com.yangdai.opennote.presentation.component.note

import android.content.ClipData
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.content.MediaType
import androidx.compose.foundation.content.ReceiveContentListener
import androidx.compose.foundation.content.consume
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.content.hasMediaType
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R

/* LiteTextField采用TextFieldValue作为内部状态，初始化文本仍旧采用StandardMode所使用的contentState:TextFieldState(),
 * applyChange函数用于更新文本内容，达到了同步contentState的目的，循环往复。
 * 虽然造成了额外的内存开销，但是在实现上更加简洁，易于维护，确保了数据一致性和单一数据来源，也便于模式之间的切换。
 * 不过也是无奈之举，TextFieldState无法实现富文本样式，只能采用TextFieldValue作为替代，而TextFieldValue提升状态很困难，只能两者配合使用。
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LiteTextField(
    modifier: Modifier = Modifier,
    readMode: Boolean = false,
    state: TextFieldState,
    scrollState: ScrollState,
    searchWord: String,
    headerRange: IntRange?,
    onTemplateClick: () -> Unit
) {

    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(state.text.toString())
        )
    }

    fun applyChange(tfv: TextFieldValue) {
        textFieldValue = tfv
        state.setTextAndPlaceCursorAtEnd(textFieldValue.text)
    }

    LaunchedEffect(state.text) {
        textFieldValue = textFieldValue.copy(text = state.text.toString())
    }

    val receiveContentListener = remember {
        ReceiveContentListener { transferableContent ->
            when {
                transferableContent.hasMediaType(MediaType.Text) -> {
                    transferableContent.consume { item: ClipData.Item ->
                        val hasText = item.text.isNotEmpty()
                        if (hasText) {
                            applyChange(textFieldValue.add(item.text.toString()))
                        }
                        hasText
                    }
                }

                else -> transferableContent
            }
        }
    }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    LaunchedEffect(headerRange) {
        headerRange?.let {
            // 获取该位置的文本边界
            val bounds = textLayoutResult!!.getBoundingBox(headerRange.first)
            // 计算滚动位置
            val scrollPosition = (bounds.top - 50f).toInt().coerceAtLeast(0)
            // 执行滚动
            scrollState.animateScrollTo(scrollPosition)
        }
    }

    Column(modifier = modifier) {
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(1f)
                .contentReceiver(receiveContentListener)
                .verticalScroll(scrollState)
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        if (keyEvent.isCtrlPressed) {
                            if (keyEvent.isShiftPressed) {
                                when (keyEvent.key) {
                                    Key.K -> {
                                        applyChange(textFieldValue.inlineCode())
                                        true
                                    }

                                    else -> false
                                }
                            } else {
                                when (keyEvent.key) {
                                    Key.B -> {
                                        applyChange(textFieldValue.bold())
                                        true
                                    }

                                    Key.I -> {
                                        applyChange(textFieldValue.italic())
                                        true
                                    }

                                    Key.U -> {
                                        applyChange(textFieldValue.underline())
                                        true
                                    }

                                    Key.D -> {
                                        applyChange(textFieldValue.strikeThrough())
                                        true
                                    }

                                    Key.M -> {
                                        applyChange(textFieldValue.highlight())
                                        true
                                    }

                                    Key.NumPad1, Key.One -> {
                                        applyChange(textFieldValue.header(1))
                                        true
                                    }

                                    Key.NumPad2, Key.Two -> {
                                        applyChange(textFieldValue.header(2))
                                        true
                                    }

                                    Key.NumPad3, Key.Three -> {
                                        applyChange(textFieldValue.header(3))
                                        true
                                    }

                                    Key.NumPad4, Key.Four -> {
                                        applyChange(textFieldValue.header(4))
                                        true
                                    }

                                    Key.NumPad5, Key.Five -> {
                                        applyChange(textFieldValue.header(5))
                                        true
                                    }

                                    Key.NumPad6, Key.Six -> {
                                        applyChange(textFieldValue.header(6))
                                        true
                                    }

                                    else -> false
                                }
                            }

                        } else {
                            when (keyEvent.key) {
                                Key.DirectionLeft -> {
                                    textFieldValue = textFieldValue.moveCursorLeft()
                                    true
                                }

                                Key.DirectionRight -> {
                                    textFieldValue = textFieldValue.moveCursorRight()
                                    true
                                }

                                else -> false
                            }
                        }
                    } else {
                        false
                    }
                },
            value = textFieldValue,
            onValueChange = { newText ->
                applyChange(newText)
            },
            readOnly = readMode,
            visualTransformation = remember(readMode, searchWord, textFieldValue.selection) {
                LiteTextVisualTransformation(
                    readMode, searchWord, textFieldValue.selection
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            onTextLayout = { result ->
                textLayoutResult = result
            },
            decorationBox = { innerTextField ->
                Box {
                    if (textFieldValue.text.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.content),
                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    innerTextField()
                }
            })

        AnimatedVisibility(
            visible = !readMode,
            enter = slideInVertically { fullHeight -> fullHeight },
            exit = slideOutVertically { fullHeight -> fullHeight }) {
            RichTextEditorRow(
                onTabIClick = {
                    applyChange(textFieldValue.tab())
                }, onTabDClick = {
                    applyChange(textFieldValue.unTab())
                }, onHeaderClick = { level ->
                    applyChange(textFieldValue.header(level))
                }, onBoldClick = {
                    applyChange(textFieldValue.bold())
                }, onItalicClick = {
                    applyChange(textFieldValue.italic())
                }, onUnderlineClick = {
                    applyChange(textFieldValue.underline())
                }, onStrikeThroughClick = {
                    applyChange(textFieldValue.strikeThrough())
                }, onHighlightClick = {
                    applyChange(textFieldValue.highlight())
                }, onCodeClick = {
                    applyChange(textFieldValue.inlineCode())
                }, onBracketsClick = {
                    applyChange(textFieldValue.brackets())
                }, onBracesClick = {
                    applyChange(textFieldValue.braces())
                }, onTemplateClick = onTemplateClick
            )
        }
    }
}
