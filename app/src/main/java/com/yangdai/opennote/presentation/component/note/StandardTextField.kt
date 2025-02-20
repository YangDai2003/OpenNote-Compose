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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

    var indices by remember { mutableStateOf(emptyList<Pair<Int, Int>>()) }
    var lintErrors by remember { mutableStateOf(emptyList<Pair<Int, Int>>()) }

    LaunchedEffect(state.text, findAndReplaceState.searchWord, isLintActive, readMode) {
        withContext(Dispatchers.Default) {
            lintErrors =
                if (isLintActive) MarkdownLint().validate(state.text.toString())
                else emptyList()
            indices =
                if (!readMode) findAllIndices(state.text.toString(), findAndReplaceState.searchWord)
                else emptyList()
        }
    }

    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(findAndReplaceState.searchWord, indices, findAndReplaceState.scrollDirection) {
        if (indices.isNotEmpty() && textLayoutResult != null && findAndReplaceState.scrollDirection != null) {
            // 更新当前索引
            currentIndex = when (findAndReplaceState.scrollDirection) {
                ScrollDirection.NEXT -> (currentIndex + 1) % indices.size
                ScrollDirection.PREVIOUS -> if (currentIndex <= 0) indices.size - 1 else currentIndex - 1
            }

            // 获取目标匹配项的位置
            val targetMatch = indices[currentIndex]
            // 获取该位置的文本边界
            val bounds = textLayoutResult!!.getBoundingBox(targetMatch.first)
            // 计算滚动位置
            val scrollPosition = (bounds.top - 50f).toInt().coerceAtLeast(0)
            // 执行滚动
            scrollState.animateScrollTo(scrollPosition)
            // 通知滚动完成
            onFindAndReplaceUpdate(findAndReplaceState.copy(scrollDirection = null))
        }
    }

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

    LaunchedEffect(findAndReplaceState.replaceType) {
        if (findAndReplaceState.replaceType != null) {
            if (findAndReplaceState.replaceType == ReplaceType.ALL) {
                if (findAndReplaceState.searchWord.isBlank()) return@LaunchedEffect
                val currentText = state.text.toString()
                val newText = currentText.replace(
                    findAndReplaceState.searchWord,
                    findAndReplaceState.replaceWord,
                    ignoreCase = false
                )
                state.setTextAndPlaceCursorAtEnd(newText)
            } else if (findAndReplaceState.replaceType == ReplaceType.CURRENT) {
                if (findAndReplaceState.searchWord.isBlank()) return@LaunchedEffect
                // 获取所有匹配位置
                val indices = findAllIndices(state.text.toString(), findAndReplaceState.searchWord)
                // 检查索引是否有效
                if (indices.isEmpty() || currentIndex >= indices.size) return@LaunchedEffect
                // 获取要替换的位置
                val (startIndex, endIndex) = indices[currentIndex]
                // 执行替换
                state.edit {
                    replace(startIndex, endIndex, findAndReplaceState.replaceWord)
                }
            }
            onFindAndReplaceUpdate(findAndReplaceState.copy(replaceType = null))
        }
    }

    val cursorState = rememberCursorState()

    BasicTextField(
        // The contentReceiver modifier is used to receive text content from the clipboard or drag-and-drop operations.
        modifier = modifier
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
                            // 提前计算滚动偏移,避免重复计算
                            val scrollOffset = Offset(0f, -scrollState.value.toFloat())
                            val text = state.text.toString()

                            // 批量绘制搜索高亮
                            if (findAndReplaceState.searchWord.isNotBlank()) {
                                // 使用 withTransform 避免多次 translate
                                withTransform({ translate(scrollOffset.x, scrollOffset.y) }) {
                                    indices.forEachIndexed { index, (start, end) ->
                                        if (start < end && end <= text.length) {
                                            val path = layoutResult.getPathForRange(start, end)
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
                                withTransform({ translate(scrollOffset.x, scrollOffset.y) }) {
                                    lintErrors.forEach { (start, end) ->
                                        if (start < end && end <= text.length) {
                                            val path = layoutResult.getPathForRange(start, end)
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

private fun findAllIndices(text: String, word: String): List<Pair<Int, Int>> {
    if (word.isBlank()) return emptyList()

    return buildList {
        var index = text.indexOf(word)
        while (index != -1) {
            add(index to (index + word.length))
            index = text.indexOf(word, index + 1)
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
