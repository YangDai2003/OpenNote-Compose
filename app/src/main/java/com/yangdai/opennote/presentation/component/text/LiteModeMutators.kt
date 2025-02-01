package com.yangdai.opennote.presentation.component.text

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

fun TextFieldValue.moveCursorLeft(): TextFieldValue {
    return if (selection.min > 0) {
        TextFieldValue(
            text = text,
            selection = TextRange(selection.min - 1, selection.min - 1)
        )
    } else {
        this
    }
}

fun TextFieldValue.moveCursorRight(): TextFieldValue {
    return if (selection.max < text.length) {
        TextFieldValue(
            text = text,
            selection = TextRange(selection.max + 1, selection.max + 1)
        )
    } else {
        this
    }
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

    val newText = text.replaceRange(lineStart, lineStart, "\t")
    return TextFieldValue(
        text = newText,
        selection = TextRange(
            initialSelection.min + "\t".length,
            initialSelection.max + "\t".length
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

    val tabIndex = text.indexOf('\t', lineStart)
    val initialSelection = selection

    return if (tabIndex != -1 && tabIndex < selection.min) {
        val newText = text.replaceRange(tabIndex, tabIndex + 1, "")
        TextFieldValue(
            text = newText,
            selection = TextRange(
                initialSelection.min - 1,
                initialSelection.max - 1
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