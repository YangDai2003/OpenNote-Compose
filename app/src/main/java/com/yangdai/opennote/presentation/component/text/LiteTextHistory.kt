@file:Suppress("unused", "unused", "unused", "unused", "unused", "unused", "unused")

package com.yangdai.opennote.presentation.component.text

import androidx.compose.ui.text.input.TextFieldValue

// TODO
class TextHistory(private val maxHistorySize: Int = 10) {
    private val undoStack = ArrayDeque<TextFieldValue>()
    private val redoStack = ArrayDeque<TextFieldValue>()

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun push(value: TextFieldValue) {
        undoStack.addLast(value)
        if (undoStack.size > maxHistorySize) {
            undoStack.removeFirst()
        }
        redoStack.clear()
    }

    fun undo(current: TextFieldValue): TextFieldValue? {
        if (undoStack.isEmpty()) return null
        redoStack.addLast(current)
        return undoStack.removeLastOrNull()
    }

    fun redo(current: TextFieldValue): TextFieldValue? {
        if (redoStack.isEmpty()) return null
        undoStack.addLast(current)
        return redoStack.removeLastOrNull()
    }
}