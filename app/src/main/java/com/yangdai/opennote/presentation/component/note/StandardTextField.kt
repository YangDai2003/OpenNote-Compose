package com.yangdai.opennote.presentation.component.note

import android.content.ClipData
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.content.MediaType
import androidx.compose.foundation.content.ReceiveContentListener
import androidx.compose.foundation.content.consume
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.content.hasMediaType
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.sin

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StandardTextField(
    modifier: Modifier,
    state: TextFieldState,
    scrollState: ScrollState,
    readMode: Boolean,
    showLineNumbers: Boolean,
    isLintActive: Boolean,
    headerRange: IntRange?,
    findAndReplaceState: FindAndReplaceState,
    onFindAndReplaceUpdate: (FindAndReplaceState) -> Unit,
    onListButtonClick: () -> Unit,
    onTableButtonClick: () -> Unit,
    onTaskButtonClick: () -> Unit,
    onLinkButtonClick: () -> Unit,
    onImageButtonClick: () -> Unit,
    onAudioButtonClick: () -> Unit,
    onVideoButtonClick: () -> Unit,
    onImageReceived: (List<Uri>) -> Unit
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

                transferableContent.hasMediaType(MediaType.Image) -> {
                    val receivedImages = mutableListOf<Uri>()
                    transferableContent.consume { item: ClipData.Item ->
                        item.uri.let {
                            receivedImages.add(it)
                        }
                        true
                    }.also {
                        onImageReceived(receivedImages)
                    }
                }

                else -> transferableContent
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "wavy-line")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave-phase"
    )
    var lintErrors by remember { mutableStateOf(emptyList<Pair<Int, Int>>()) }

    LaunchedEffect(state.text, isLintActive) {
        withContext(Dispatchers.Default) {
            lintErrors =
                if (isLintActive) MarkdownLint().validate(state.text.toString())
                else emptyList()
        }
    }

    val cursorState = rememberCursorState()

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
        val currentLine by remember(state.selection, actualLinePositions) {
            derivedStateOf {
                actualLinePositions.indexOfLast { (startIndex, _) ->
                    startIndex <= state.selection.start
                }.coerceAtLeast(0)
            }
        }
        Row(modifier) {
            if (showLineNumbers) {
                LineNumbersColumn(
                    currentLine = currentLine,
                    actualLinePositions = actualLinePositions,
                    scrollProvider = { scrollState.value }
                )
                VerticalDivider()
            }

            BasicTextField(
                // The contentReceiver modifier is used to receive text content from the clipboard or drag-and-drop operations.
                modifier = Modifier
                    .padding(start = if (showLineNumbers) 4.dp else 16.dp, end = 16.dp)
                    .fillMaxSize()
                    .contentReceiver(receiveContentListener)
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

                                        Key.I -> {
                                            onImageButtonClick()
                                            true
                                        }

                                        Key.A -> {
                                            onAudioButtonClick()
                                            true
                                        }

                                        Key.V -> {
                                            onVideoButtonClick()
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

                                        Key.NumPad1, Key.One -> {
                                            state.edit { addHeader(1) }
                                            true
                                        }

                                        Key.NumPad2, Key.Two -> {
                                            state.edit { addHeader(2) }
                                            true
                                        }

                                        Key.NumPad3, Key.Three -> {
                                            state.edit { addHeader(3) }
                                            true
                                        }

                                        Key.NumPad4, Key.Four -> {
                                            state.edit { addHeader(4) }
                                            true
                                        }

                                        Key.NumPad5, Key.Five -> {
                                            state.edit { addHeader(5) }
                                            true
                                        }

                                        Key.NumPad6, Key.Six -> {
                                            state.edit { addHeader(6) }
                                            true
                                        }

                                        else -> false
                                    }
                                }

                            } else {
                                when (keyEvent.key) {
                                    Key.DirectionLeft -> {
                                        state.edit { moveCursorLeft(cursorState) }
                                        true
                                    }

                                    Key.DirectionRight -> {
                                        state.edit { moveCursorRight(cursorState) }
                                        true
                                    }

                                    Key.DirectionUp -> {
                                        state.edit { moveCursorUpWithState(cursorState) }
                                        true
                                    }

                                    Key.DirectionDown -> {
                                        state.edit { moveCursorDownWithState(cursorState) }
                                        true
                                    }

                                    Key.Enter, Key.NumPadEnter -> { // 改进换行键行为
                                        val currentText = state.text.toString()
                                        val selection = state.selection

                                        // 安全地获取当前行的内容
                                        val currentLineStart = currentText.lastIndexOf(
                                            '\n',
                                            (selection.start - 1).coerceIn(0, currentText.length)
                                        ).let {
                                            if (it == -1) 0 else it + 1
                                        }
                                        val currentLineEnd =
                                            currentText.indexOf('\n', selection.start).let {
                                                if (it == -1) currentText.length else it
                                            }

                                        // 确保起始位置小于结束位置
                                        if (currentLineStart >= currentLineEnd) {
                                            state.edit {
                                                add("\n")
                                            }
                                            return@onPreviewKeyEvent true
                                        }

                                        val currentLine =
                                            currentText.substring(currentLineStart, currentLineEnd)
                                        val trimmedLine = currentLine.trim()

                                        // 获取行首的缩进
                                        val indentation =
                                            currentLine.takeWhile { it.isWhitespace() }

                                        // 处理空列表项
                                        if (selection.start == currentLineEnd &&
                                            (trimmedLine == "- [ ]" || trimmedLine == "-" || trimmedLine == "*" || trimmedLine == "+"
                                                    || trimmedLine.matches(Regex("^\\d+\\.$")) || trimmedLine.matches(
                                                Regex("^\\d+\\)$")
                                            ))
                                        ) {
                                            state.edit {
                                                delete(currentLineStart, currentLineEnd)
                                            }
                                            return@onPreviewKeyEvent true
                                        }

                                        val newLinePrefix = when {
                                            trimmedLine.startsWith("- [ ] ") || trimmedLine.startsWith(
                                                "- [x] "
                                            ) -> "- [ ] " // 任务列表
                                            trimmedLine.matches(Regex("^\\d+\\.\\s.*")) -> {
                                                val nextNumber =
                                                    trimmedLine.substringBefore(".").toIntOrNull()
                                                        ?.plus(1)
                                                        ?: 1
                                                "$nextNumber. " // 有序列表
                                            }

                                            trimmedLine.matches(Regex("^\\d+\\)\\s.*")) -> {
                                                val nextNumber =
                                                    trimmedLine.substringBefore(")").toIntOrNull()
                                                        ?.plus(1)
                                                        ?: 1
                                                "$nextNumber) " // 有序列表
                                            }

                                            trimmedLine.startsWith("- ") -> "- " // 无序列表
                                            trimmedLine.startsWith("* ") -> "* "
                                            trimmedLine.startsWith("+ ") -> "+ "
                                            else -> ""
                                        }

                                        state.edit {
                                            add("\n")
                                            if (newLinePrefix.isNotEmpty()) {
                                                add(indentation + newLinePrefix)
                                            }
                                        }
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
                onTextLayout = { result -> textLayoutResult = result.invoke() },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.None
                ),
                decorator = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .clipToBounds()
                            .drawBehind {
                                textLayoutResult?.let { layoutResult ->
                                    // 提前计算滚动偏移,避免重复计算
                                    val scrollOffset = Offset(0f, -scrollState.value.toFloat())
                                    val text = state.text.toString()

                                    // 批量绘制搜索高亮
                                    if (findAndReplaceState.searchWord.isNotBlank()) {
                                        // 使用 withTransform 避免多次 translate
                                        withTransform({
                                            translate(
                                                scrollOffset.x,
                                                scrollOffset.y
                                            )
                                        }) {
                                            indices.forEachIndexed { index, (start, end) ->
                                                if (start < end && end <= text.length) {
                                                    val path =
                                                        layoutResult.getPathForRange(start, end)
                                                    drawPath(
                                                        path = path,
                                                        color = if (index == currentIndex) Color.Green else Color.Cyan,
                                                        alpha = 0.5f,
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // 批量绘制波浪线
                                    if (isLintActive) {
                                        withTransform({
                                            translate(
                                                scrollOffset.x,
                                                scrollOffset.y
                                            )
                                        }) {
                                            lintErrors.forEach { (start, end) ->
                                                if (start < end && end <= text.length) {
                                                    val path =
                                                        layoutResult.getPathForRange(start, end)
                                                    drawWavyUnderline(this, path, phase)
                                                }
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
    }
}

private fun drawWavyUnderline(
    drawScope: DrawScope,
    path: Path,
    phase: Float,
    amplitude: Float = 3f,
    wavelength: Float = 25f
) {
    val bounds = path.getBounds()

    // 预计算正弦波点数
    val pointCount = ((bounds.right - bounds.left) * 2).toInt()
    if (pointCount <= 0) return

    val points = FloatArray(pointCount * 2)
    val startX = bounds.left
    val y = bounds.bottom + 2f

    // 批量计算波浪点
    for (i in 0 until pointCount) {
        val x = startX + i * 0.5f
        val yOffset = amplitude * sin((x * (2f * PI / wavelength)) + phase).toFloat()
        points[i * 2] = x
        points[i * 2 + 1] = y + yOffset
    }

    // 单次绘制所有点
    drawScope.drawPoints(
        points = points.toList().chunked(2).map { (x, y) -> Offset(x, y) },
        pointMode = PointMode.Polygon,
        color = Color.Red,
        strokeWidth = 1.5f,
        cap = StrokeCap.Round
    )
}
