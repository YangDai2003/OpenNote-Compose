package com.yangdai.opennote.presentation.component.note

import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextRange

// 用于存储行信息的数据类
data class LineInfo(
    val startOffset: Int,    // 行起始位置
    val endOffset: Int,      // 行结束位置
    val content: String      // 行内容
)

// 优化 1: 缓存行信息
class TextLinesCache {
    private var text: String = ""
    private var lines: List<LineInfo> = emptyList()

    fun getLines(buffer: TextFieldBuffer): List<LineInfo> {
        val currentText = buffer.toString()
        // 只有当文本变化时才重新计算行信息
        if (currentText != text) {
            text = currentText
            lines = calculateLines(currentText)
        }
        return lines
    }

    private fun calculateLines(text: String): List<LineInfo> {
        val result = mutableListOf<LineInfo>()
        var startOffset = 0

        // 优化 2: 使用 lineSequence() 避免创建中间 List
        text.lineSequence().forEach { line ->
            val endOffset = startOffset + line.length
            result.add(LineInfo(startOffset, endOffset, line))
            startOffset = endOffset + 1
        }

        return result
    }
}

// 优化 3: 使用二分查找找到光标所在行
fun List<LineInfo>.findLineByOffset(offset: Int): LineInfo {
    var left = 0
    var right = size - 1

    while (left <= right) {
        val mid = (left + right) / 2
        val line = get(mid)

        when {
            offset < line.startOffset -> right = mid - 1
            offset > line.endOffset -> left = mid + 1
            else -> return line
        }
    }

    return last()
}

fun TextFieldBuffer.moveCursorLeftStateless() {
    if (selection.min > 0) {
        selection = TextRange(selection.min - 1, selection.min - 1)
    }
}

fun TextFieldBuffer.moveCursorRightStateless() {
    if (selection.max < length) {
        selection = TextRange(selection.max + 1, selection.max + 1)
    }
}

class CursorState {
    var preferredRelativeX: Int? = null

    // 优化 4: 添加行信息缓存
    val linesCache = TextLinesCache()
}

@Composable
fun rememberCursorState(): CursorState {
    return remember { CursorState() }
}

// 优化 5: 提取获取行信息的扩展函数
fun TextFieldBuffer.getLineInfoWithCache(state: CursorState): Pair<List<LineInfo>, LineInfo> {
    val lines = state.linesCache.getLines(this)
    val currentLine = lines.findLineByOffset(selection.min)
    return Pair(lines, currentLine)
}

fun TextFieldBuffer.moveCursorLeft(state: CursorState) {
    if (selection.min > 0) {
        selection = TextRange(selection.min - 1, selection.min - 1)
        val (_, currentLine) = getLineInfoWithCache(state)
        state.preferredRelativeX = selection.min - currentLine.startOffset
    }
}

fun TextFieldBuffer.moveCursorRight(state: CursorState) {
    if (selection.max < length) {
        selection = TextRange(selection.max + 1, selection.max + 1)
        val (_, currentLine) = getLineInfoWithCache(state)
        state.preferredRelativeX = selection.max - currentLine.startOffset
    }
}

fun TextFieldBuffer.moveCursorUpWithState(state: CursorState) {
    val (lines, currentLine) = getLineInfoWithCache(state)
    val relativePosition = selection.min - currentLine.startOffset
    state.preferredRelativeX = state.preferredRelativeX ?: relativePosition

    // 优化 6: 使用 binarySearch 找到当前行索引
    val currentLineIndex = lines.binarySearch {
        it.startOffset.compareTo(currentLine.startOffset)
    }

    if (currentLineIndex > 0) {
        val previousLine = lines[currentLineIndex - 1]
        val newPosition = previousLine.startOffset + minOf(
            state.preferredRelativeX ?: relativePosition,
            previousLine.content.length
        )
        selection = TextRange(newPosition, newPosition)
    }
}

fun TextFieldBuffer.moveCursorDownWithState(state: CursorState) {
    val (lines, currentLine) = getLineInfoWithCache(state)
    val relativePosition = selection.min - currentLine.startOffset
    state.preferredRelativeX = state.preferredRelativeX ?: relativePosition

    val currentLineIndex = lines.binarySearch {
        it.startOffset.compareTo(currentLine.startOffset)
    }

    if (currentLineIndex < lines.size - 1) {
        val nextLine = lines[currentLineIndex + 1]
        val newPosition = nextLine.startOffset + minOf(
            state.preferredRelativeX ?: relativePosition,
            nextLine.content.length
        )
        selection = TextRange(newPosition, newPosition)
    }
}

private fun TextFieldBuffer.inlineWrap(
    startWrappedString: String,
    endWrappedString: String = startWrappedString,
    initialSelection: TextRange = selection
) = if (initialSelection.collapsed) {
    // No text selected, insert at cursor position and place cursor in the middle
    replace(initialSelection.min, initialSelection.min, startWrappedString + endWrappedString)
    selection = TextRange(
        initialSelection.min + startWrappedString.length,
        initialSelection.min + startWrappedString.length
    )
} else {
    replace(initialSelection.min, initialSelection.min, startWrappedString)
    replace(
        initialSelection.max + startWrappedString.length,
        initialSelection.max + startWrappedString.length,
        endWrappedString
    )
    selection = TextRange(
        initialSelection.min,
        initialSelection.max + startWrappedString.length + endWrappedString.length
    )
}

fun TextFieldBuffer.bold() = inlineWrap("**")

fun TextFieldBuffer.italic() = inlineWrap("_")

fun TextFieldBuffer.underline() = inlineWrap("++")

fun TextFieldBuffer.strikeThrough() = inlineWrap("~~")

fun TextFieldBuffer.mark() = inlineWrap("<mark>", "</mark>")

fun TextFieldBuffer.inlineBrackets() = inlineWrap("[", "]")

fun TextFieldBuffer.inlineBraces() = inlineWrap("{", "}")

fun TextFieldBuffer.inlineCode() = inlineWrap("`")

fun TextFieldBuffer.inlineMath() = inlineWrap("$")

fun TextFieldBuffer.quote() {
    val text = toString()
    val lineStart = text.take(selection.min)
        .lastIndexOf('\n')
        .takeIf { it != -1 }
        ?.let { it + 1 }
        ?: 0

    val initialSelection = selection

    replace(lineStart, lineStart, "> ")
    selection = TextRange(
        initialSelection.min + 2,
        initialSelection.max + 2
    )
}

fun TextFieldBuffer.tab() {
    val text = toString()
    val lineStart = text.take(selection.min)
        .lastIndexOf('\n')
        .takeIf { it != -1 }
        ?.let { it + 1 }
        ?: 0

    val initialSelection = selection

    replace(lineStart, lineStart, "    ") // 4 spaces
    selection = TextRange(
        initialSelection.min + 4,
        initialSelection.max + 4
    )
}

fun TextFieldBuffer.unTab() {
    val text = toString()
    val lineStart = text.take(selection.min)
        .lastIndexOf('\n')
        .takeIf { it != -1 }
        ?.let { it + 1 }
        ?: 0

    val tabIndex = text.indexOf("    ", lineStart)
    val initialSelection = selection

    if (tabIndex != -1 && tabIndex < selection.min) {
        replace(tabIndex, tabIndex + 4, "")
        selection = TextRange(
            initialSelection.min - 4,
            initialSelection.max - 4
        )
    }
}

fun TextFieldBuffer.add(str: String) {
    val initialSelection = selection
    replace(initialSelection.min, initialSelection.max, str)
}

fun TextFieldBuffer.addInNewLine(str: String) {
    val text = toString()
    if (selection.min != 0 && text[selection.min - 1] != '\n') {
        // 如果不是换行符，那么就先添加一个换行符
        add("\n")
    }
    add(str)
}

fun TextFieldBuffer.addMermaid() {
    addInNewLine("<pre class=\"mermaid\">\n</pre>")
}

fun TextFieldBuffer.addHeader(level: Int) {
    addInNewLine("#".repeat(level) + " ")
}

fun TextFieldBuffer.addRule() {
    addInNewLine("\n")
    add("------")
    add("\n")
    add("\n")
}

fun TextFieldBuffer.addTask(task: String, checked: Boolean) {
    if (checked) {
        addInNewLine("- [x] $task")
    } else {
        addInNewLine("- [ ] $task")
    }
}

fun TextFieldBuffer.addTable(row: Int, column: Int) = addInNewLine(
    buildString {
        append("|")
        repeat(column) { append(" HEADER |") }
        append("\n|")
        repeat(column) { append(" :-----------: |") }
        repeat(row) {
            append("\n|")
            repeat(column) { append(" Element |") }
        }
    }
)