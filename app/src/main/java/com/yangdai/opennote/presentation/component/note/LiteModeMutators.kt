package com.yangdai.opennote.presentation.component.note

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextRange.Companion.Zero
import androidx.compose.ui.text.input.TextFieldValue

fun TextFieldValue.moveCursorLeft(): TextFieldValue {
    return if (selection.min > 0) {
        copy(
            selection = TextRange(selection.min - 1, selection.min - 1)
        )
    } else {
        this
    }
}

fun TextFieldValue.moveCursorRight(): TextFieldValue {
    return if (selection.max < text.length) {
        copy(
            selection = TextRange(selection.max + 1, selection.max + 1)
        )
    } else {
        this
    }
}

fun TextFieldValue.moveCursorUp(): TextFieldValue {
    val currentLineStart = text.take(selection.min).lastIndexOf('\n')
    if (currentLineStart == -1) return copy(selection = Zero)
    val previousLineEnd = text.take(currentLineStart).lastIndexOf('\n')
    val previousLineStart = if (previousLineEnd == -1) 0 else previousLineEnd + 1
    val offsetInPreviousLine =
        (selection.min - currentLineStart).coerceAtMost(currentLineStart - previousLineStart)
    return copy(
        selection = TextRange(previousLineStart + offsetInPreviousLine)
    )
}

fun TextFieldValue.moveCursorDown(): TextFieldValue {
    val currentLineStart = text.take(selection.min).lastIndexOf('\n')
    val nextLineStart = text.indexOf('\n', selection.min)
    if (nextLineStart == -1) return this // 已经在最后一行

    val nextLineEnd = text.indexOf('\n', nextLineStart + 1)
    val nextLineLength = if (nextLineEnd == -1) {
        text.length - (nextLineStart + 1)
    } else {
        nextLineEnd - (nextLineStart + 1)
    }

    val offsetInCurrentLine =
        selection.min - if (currentLineStart == -1) 0 else currentLineStart + 1
    val offsetInNextLine = offsetInCurrentLine.coerceAtMost(nextLineLength)

    return copy(
        selection = TextRange(nextLineStart + 1 + offsetInNextLine)
    )
}

private fun TextFieldValue.inlineWrap(
    startWrappedString: String,
    endWrappedString: String = startWrappedString,
): TextFieldValue {
    return if (selection.collapsed) {
        // No text selected, insert at cursor position and place cursor in the middle
        val newText = text.substring(
            0,
            selection.min
        ) + startWrappedString + endWrappedString + text.substring(selection.min)
        TextFieldValue(
            text = newText,
            selection = TextRange(
                selection.min + startWrappedString.length,
                selection.min + startWrappedString.length
            )
        )
    } else {
        val newText = text.substring(0, selection.min) + startWrappedString + text.substring(
            selection.min,
            selection.max
        ) + endWrappedString + text.substring(selection.max)
        TextFieldValue(
            text = newText,
            selection = TextRange(
                selection.min,
                selection.max + startWrappedString.length + endWrappedString.length
            )
        )
    }
}

fun TextFieldValue.bold() = inlineWrap("**")

fun TextFieldValue.italic() = inlineWrap("_")

fun TextFieldValue.underline() = inlineWrap("++")

fun TextFieldValue.strikeThrough() = inlineWrap("~~")

fun TextFieldValue.highlight() = inlineWrap("==")

fun TextFieldValue.inlineCode() = inlineWrap("`")

fun TextFieldValue.brackets() = inlineWrap("[", "]")

fun TextFieldValue.braces() = inlineWrap("{", "}")

fun TextFieldValue.tab(): TextFieldValue {
    val text = text
    val lineStart = text.take(selection.min)
        .lastIndexOf('\n')
        .takeIf { it != -1 }
        ?.let { it + 1 }
        ?: 0

    val initialSelection = selection

    val newText = text.replaceRange(lineStart, lineStart, "    ") // 4 spaces
    return TextFieldValue(
        text = newText,
        selection = TextRange(
            initialSelection.min + 4,
            initialSelection.max + 4
        )
    )
}

fun TextFieldValue.unTab(): TextFieldValue {
    val text = text
    val lineStart = text.take(selection.min)
        .lastIndexOf('\n')
        .takeIf { it != -1 }
        ?.let { it + 1 }
        ?: 0

    val tabIndex = text.indexOf("    ", lineStart)
    val initialSelection = selection

    return if (tabIndex != -1 && tabIndex < selection.min) {
        val newText = text.replaceRange(tabIndex, tabIndex + 4, "")
        TextFieldValue(
            text = newText,
            selection = TextRange(
                initialSelection.min - 4,
                initialSelection.max - 4
            )
        )
    } else {
        this
    }
}

fun TextFieldValue.add(str: String): TextFieldValue {
    val newText = text.replaceRange(selection.min, selection.max, str)
    return TextFieldValue(
        text = newText,
        selection = TextRange(
            selection.min + str.length,
            selection.min + str.length
        )
    )
}

fun TextFieldValue.header(level: Int): TextFieldValue {
    val headerString = "#".repeat(level)
    return add("$headerString ")
}