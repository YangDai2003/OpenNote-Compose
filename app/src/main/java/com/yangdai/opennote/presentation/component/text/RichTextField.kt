package com.yangdai.opennote.presentation.component.text

import android.annotation.SuppressLint
import android.content.ClipData
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R

@SuppressLint("UnrememberedMutableState")
@Preview(showBackground = true)
@Composable
fun RichTextFieldPreview() {
    RichTextField(
        initialText = mutableStateOf(
            """
            **bold**
            _italic_
            ++underline++
            ~~delete~~
            `code`

            **_i in b_**
            _**b in i**_
            ++~~d in u~~++
            ~~++u in d++~~

            `~~++_**aabbccddee**_++~~`
        """.trimIndent()
        ),
        onTextChange = {},
        onFocusChanged = {},
        onPreviewButtonClick = {}
    )
}

/*
    数据传递的逻辑是这样的：NoteScreen中通过监听viewmodel中的contentState获取初始化文本，
    然后传递给RichTextField， RichTextField中通过initialText接收，
    然后通过onTextChange将变化传递给viewmodel中的contentState，实现实时保存。
    这样实现了两个模式共用一套viewmodel中的数据流。
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RichTextField(
    modifier: Modifier = Modifier,
    readMode: Boolean = false,
    initialText: MutableState<String>,
    onTextChange: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onPreviewButtonClick: () -> Unit,
) {
    var initialized by rememberSaveable { mutableStateOf(false) }
    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue()
        )
    }

    LaunchedEffect(initialText.value) {
        if (!initialized) {
            textFieldValue = textFieldValue.copy(text = initialText.value)
            if (initialText.value.isNotEmpty()) {
                initialized = true
            }
        }
    }

    fun applyChange(tfv: TextFieldValue) {
        textFieldValue = tfv
        onTextChange(textFieldValue.text)
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

    Column(modifier = modifier) {
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(1f)
                .contentReceiver(receiveContentListener)
                .onFocusChanged { onFocusChanged(it.isFocused) }
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

                                    Key.S -> {
                                        applyChange(textFieldValue.strikeThrough())
                                        true
                                    }

                                    Key.P -> {
                                        onPreviewButtonClick()
                                        true
                                    }

                                    Key.NumPad1 -> {
                                        applyChange(textFieldValue.header(1))
                                        true
                                    }

                                    Key.NumPad2 -> {
                                        applyChange(textFieldValue.header(2))
                                        true
                                    }

                                    Key.NumPad3 -> {
                                        applyChange(textFieldValue.header(3))
                                        true
                                    }

                                    Key.NumPad4 -> {
                                        applyChange(textFieldValue.header(4))
                                        true
                                    }

                                    Key.NumPad5 -> {
                                        applyChange(textFieldValue.header(5))
                                        true
                                    }

                                    Key.NumPad6 -> {
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
            visualTransformation = remember(readMode) { RichTextVisualTransformation(readMode) },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
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
            }
        )

        AnimatedVisibility(
            visible = !readMode,
            enter = slideInVertically { fullHeight -> fullHeight },
            exit = slideOutVertically { fullHeight -> fullHeight }) {
            RichTextEditorRow(
                onTitle1Click = { selected ->
                    applyChange(textFieldValue.add(if (!selected) "# " else "\n"))
                },
                onTitle2Click = { selected ->
                    applyChange(textFieldValue.add(if (!selected) "## " else "\n"))
                },
                onTitle3Click = { selected ->
                    applyChange(textFieldValue.add(if (!selected) "### " else "\n"))
                },
                onTitle4Click = { selected ->
                    applyChange(textFieldValue.add(if (!selected) "#### " else "\n"))
                },
                onTitle5Click = { selected ->
                    applyChange(textFieldValue.add(if (!selected) "##### " else "\n"))
                },
                onTitle6Click = { selected ->
                    applyChange(textFieldValue.add(if (!selected) "###### " else "\n"))
                },
                onBoldClick = {
                    applyChange(textFieldValue.add("**"))
                },
                onItalicClick = {
                    applyChange(textFieldValue.add("_"))
                },
                onUnderlineClick = {
                    applyChange(textFieldValue.add("++"))
                },
                onStrikeThroughClick = {
                    applyChange(textFieldValue.add("~~"))
                },
                onCodeClick = {
                    applyChange(textFieldValue.add("`"))
                },
                onBracketsClick = { selected ->
                    applyChange(textFieldValue.add(if (!selected) "[" else "]"))
                },
                onBracesClick = { selected ->
                    applyChange(textFieldValue.add(if (!selected) "{" else "}"))
                },
                onScanButtonClick = {
                    // 扫描二维码
                }
            )
        }
    }
}
