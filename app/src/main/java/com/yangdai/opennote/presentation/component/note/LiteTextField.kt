package com.yangdai.opennote.presentation.component.note

import android.content.ClipData
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
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
    readMode: Boolean,
    showLineNumbers: Boolean,
    state: TextFieldState,
    scrollState: ScrollState,
    headerRange: IntRange?,
    findAndReplaceState: FindAndReplaceState,
    onFindAndReplaceUpdate: (FindAndReplaceState) -> Unit,
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

    TextEditorBase(
        state = state,
        scrollState = scrollState,
        readMode = readMode,
        showLineNumbers = showLineNumbers,
        headerRange = headerRange,
        findAndReplaceState = findAndReplaceState,
        onFindAndReplaceUpdate = onFindAndReplaceUpdate,
        textLayoutResult = textLayoutResult
    ) { indices, currentIndex, actualLinePositions ->
        val currentLine by remember(textFieldValue.selection, actualLinePositions) {
            derivedStateOf {
                actualLinePositions.indexOfLast { (startIndex, _) ->
                    startIndex <= textFieldValue.selection.start
                }.coerceAtLeast(0)
            }
        }
        Column(modifier) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AnimatedContent(showLineNumbers && !readMode) {
                    if (it)
                        Row {
                            LineNumbersColumn(
                                currentLine = currentLine,
                                actualLinePositions = actualLinePositions,
                                scrollProvider = { scrollState.value }
                            )
                            VerticalDivider()
                        }
                    else
                        Spacer(
                            Modifier
                                .fillMaxHeight()
                                .width(12.dp)
                        )
                }
                BasicTextField(
                    modifier = Modifier
                        .padding(start = 4.dp, end = 16.dp)
                        .fillMaxHeight()
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

                                        Key.DirectionUp -> {
                                            textFieldValue = textFieldValue.moveCursorUp()
                                            true
                                        }

                                        Key.DirectionDown -> {
                                            textFieldValue = textFieldValue.moveCursorDown()
                                            true
                                        }

                                        Key.Enter, Key.NumPadEnter -> {
                                            val currentText = textFieldValue.text
                                            val selection = textFieldValue.selection

                                            val currentLineStart = currentText.lastIndexOf(
                                                '\n',
                                                (selection.start - 1).coerceIn(
                                                    0,
                                                    currentText.length
                                                )
                                            ).let {
                                                if (it == -1) 0 else it + 1
                                            }

                                            val currentLineEnd =
                                                currentText.indexOf('\n', selection.start).let {
                                                    if (it == -1) currentText.length else it
                                                }

                                            if (currentLineStart >= currentLineEnd) {
                                                applyChange(textFieldValue.add("\n"))
                                                return@onPreviewKeyEvent true
                                            }

                                            val currentLine =
                                                currentText.substring(
                                                    currentLineStart,
                                                    currentLineEnd
                                                )
                                            val trimmedLine = currentLine.trim()

                                            val indentation =
                                                currentLine.takeWhile { it.isWhitespace() }

                                            if (selection.start == currentLineEnd &&
                                                (trimmedLine == "- [ ]" || trimmedLine == "-" || trimmedLine == "*" || trimmedLine == "+"
                                                        || trimmedLine.matches(Regex("^\\d+\\.$")) || trimmedLine.matches(
                                                    Regex("^\\d+\\)$")
                                                ))
                                            ) {
                                                val newText = StringBuilder(currentText)
                                                    .delete(currentLineStart, currentLineEnd)
                                                    .toString()

                                                applyChange(
                                                    textFieldValue.copy(
                                                        text = newText,
                                                        selection = TextRange(currentLineStart)
                                                    )
                                                )
                                                return@onPreviewKeyEvent true
                                            }

                                            val newLinePrefix = when {
                                                trimmedLine.startsWith("- [ ] ") || trimmedLine.startsWith(
                                                    "- [x] "
                                                ) -> "- [ ] "

                                                trimmedLine.matches(Regex("^\\d+\\.\\s.*")) -> {
                                                    val nextNumber =
                                                        trimmedLine.substringBefore(".")
                                                            .toIntOrNull()
                                                            ?.plus(1) ?: 1
                                                    "$nextNumber. "
                                                }

                                                trimmedLine.matches(Regex("^\\d+\\)\\s.*")) -> {
                                                    val nextNumber =
                                                        trimmedLine.substringBefore(")")
                                                            .toIntOrNull()
                                                            ?.plus(1) ?: 1
                                                    "$nextNumber) "
                                                }

                                                trimmedLine.startsWith("- ") -> "- "
                                                trimmedLine.startsWith("* ") -> "* "
                                                trimmedLine.startsWith("+ ") -> "+ "
                                                else -> ""
                                            }

                                            val newText = StringBuilder(currentText)
                                                .insert(
                                                    selection.start,
                                                    "\n" + (if (newLinePrefix.isNotEmpty()) indentation + newLinePrefix else "")
                                                )
                                                .toString()

                                            val newCursorPosition = selection.start + 1 +
                                                    (if (newLinePrefix.isNotEmpty()) indentation.length + newLinePrefix.length else 0)

                                            applyChange(
                                                textFieldValue.copy(
                                                    text = newText,
                                                    selection = TextRange(newCursorPosition)
                                                )
                                            )
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
                    onValueChange = { newText -> applyChange(newText) },
                    readOnly = readMode,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.None
                    ),
                    visualTransformation = remember(
                        readMode,
                        indices,
                        currentIndex,
                        textFieldValue.selection
                    ) {
                        LiteTextVisualTransformation(
                            readMode, indices, currentIndex, textFieldValue.selection
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    onTextLayout = { result -> textLayoutResult = result },
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
            }

            AnimatedVisibility(
                visible = !readMode,
                enter = slideInVertically { fullHeight -> fullHeight },
                exit = slideOutVertically { fullHeight -> fullHeight * 2 }) {
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
                    }, onTemplateClick = onTemplateClick,
                    onAlertClick = {
                        applyChange(textFieldValue.alert(it.name))
                    }
                )
            }
        }
    }
}
