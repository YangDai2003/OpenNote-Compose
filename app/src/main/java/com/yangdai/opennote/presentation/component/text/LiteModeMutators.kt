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
    endWrappedString: String = startWrappedString
): TextFieldValue {

    val newText = text.replaceRange(
        selection.min,
        selection.max,
        startWrappedString + text.substring(
            selection.min,
            selection.max
        ) + endWrappedString
    )
    return TextFieldValue(
        text = newText,
        selection = TextRange(
            selection.min,
            selection.max + startWrappedString.length + endWrappedString.length
        )
    )
}

fun TextFieldValue.bold() = inlineWrap("**")

fun TextFieldValue.italic() = inlineWrap("_")

fun TextFieldValue.underline() = inlineWrap("++")

fun TextFieldValue.strikeThrough() = inlineWrap("~~")

fun TextFieldValue.inlineCode() = inlineWrap("`")

fun TextFieldValue.header(level: Int): TextFieldValue {
    val headerString = "#".repeat(level)
    return inlineWrap("$headerString ", "\n")
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
