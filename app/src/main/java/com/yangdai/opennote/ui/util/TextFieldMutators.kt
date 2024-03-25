@file:OptIn(ExperimentalFoundationApi::class)

package com.yangdai.opennote.ui.util

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.TextFieldBuffer
import androidx.compose.ui.text.TextRange

fun TextFieldBuffer.inlineWrap(
    startWrappedString: String,
    endWrappedString: String = startWrappedString
) {
    val initialSelection = selectionInChars
    replace(initialSelection.min, initialSelection.min, startWrappedString)
    replace(
        initialSelection.max + startWrappedString.length,
        initialSelection.max + startWrappedString.length,
        endWrappedString
    )
    selectCharsIn(
        TextRange(
            initialSelection.min,
            initialSelection.max + startWrappedString.length + endWrappedString.length
        )
    )
}

fun TextFieldBuffer.bold() = inlineWrap("**")

fun TextFieldBuffer.italic() = inlineWrap("_")

fun TextFieldBuffer.underline() = inlineWrap("<u>", "</u>")

fun TextFieldBuffer.strikeThrough() = inlineWrap("~~")

fun TextFieldBuffer.mark() = inlineWrap("<mark>", "</mark>")

fun TextFieldBuffer.inlineCode() = inlineWrap("`")

fun TextFieldBuffer.quote() {
    val text = toString()
    val lineStart = text.take(selectionInChars.min)
        .lastIndexOf('\n')
        .takeIf { it != -1 }
        ?.let { it + 1 }
        ?: 0

    val initialSelection = selectionInChars

    replace(lineStart, lineStart, "> ")
    selectCharsIn(
        TextRange(
            initialSelection.min + 2,
            initialSelection.max + 2
        )
    )
}

fun TextFieldBuffer.add(str: String) {
    val initialSelection = selectionInChars
    replace(initialSelection.min, initialSelection.max, str)
    selectCharsIn(
        TextRange(
            initialSelection.min,
            initialSelection.min + str.length
        )
    )
}

fun TextFieldBuffer.addLink(link: String) = add(link)

fun TextFieldBuffer.addTask(task: String, checked: Boolean) {
    if (checked) {
        add("- [x] $task")
    } else {
        add("- [ ] $task")
    }
}
