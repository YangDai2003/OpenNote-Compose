package com.yangdai.opennote.presentation.component.note

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextLayoutResult
import com.yangdai.opennote.presentation.util.findAllIndices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TextEditorBase(
    state: TextFieldState,
    scrollState: ScrollState,
    readMode: Boolean,
    showLineNumbers: Boolean,
    headerRange: IntRange?,
    textLayoutResult: TextLayoutResult?,
    findAndReplaceState: FindAndReplaceState,
    onFindAndReplaceUpdate: (FindAndReplaceState) -> Unit,
    content: @Composable (
        indices: List<Pair<Int, Int>>,
        currentIndex: Int,
        actualLinePositions: List<Pair<Int, Float>>
    ) -> Unit
) {
    // Shared state management
    var indices by remember { mutableStateOf(emptyList<Pair<Int, Int>>()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var actualLinePositions by remember { mutableStateOf<List<Pair<Int, Float>>>(emptyList()) }

    // Search functionality
    LaunchedEffect(state.text, findAndReplaceState.searchWord, readMode) {
        if (!readMode && findAndReplaceState.searchWord.isNotBlank()) {
            withContext(Dispatchers.Default) {
                indices = findAllIndices(state.text.toString(), findAndReplaceState.searchWord)
            }
        } else {
            indices = emptyList()
        }
    }

    // Handle search navigation
    LaunchedEffect(findAndReplaceState.searchWord, indices, findAndReplaceState.scrollDirection) {
        if (indices.isNotEmpty() && textLayoutResult != null && findAndReplaceState.scrollDirection != null) {
            // Update current index
            currentIndex = when (findAndReplaceState.scrollDirection) {
                ScrollDirection.NEXT -> (currentIndex + 1) % indices.size
                ScrollDirection.PREVIOUS -> if (currentIndex <= 0) indices.size - 1 else currentIndex - 1
            }

            // Get the target match position
            val targetMatch = indices[currentIndex]
            // Get the text bounds
            val bounds = textLayoutResult.getBoundingBox(targetMatch.first)
            // Calculate scroll position
            val scrollPosition = (bounds.top - 50f).toInt().coerceAtLeast(0)
            // Execute scroll
            scrollState.animateScrollTo(scrollPosition)
            // Notify scroll completion
            onFindAndReplaceUpdate(findAndReplaceState.copy(scrollDirection = null))
        }
    }

    // Handle header navigation
    LaunchedEffect(headerRange) {
        headerRange?.let {
            textLayoutResult?.let { layout ->
                // Get bounds of that position
                val bounds = layout.getBoundingBox(headerRange.first)
                // Calculate scroll position
                val scrollPosition = (bounds.top - 50f).toInt().coerceAtLeast(0)
                // Execute scroll
                scrollState.animateScrollTo(scrollPosition)
            }
        }
    }

    // Handle replace functionality
    LaunchedEffect(findAndReplaceState.replaceType) {
        if (findAndReplaceState.replaceType != null && findAndReplaceState.searchWord.isNotBlank()) {
            if (findAndReplaceState.replaceType == ReplaceType.ALL) {
                val currentText = state.text.toString()
                val newText = currentText.replace(
                    findAndReplaceState.searchWord,
                    findAndReplaceState.replaceWord
                )
                state.setTextAndPlaceCursorAtEnd(newText)
            } else if (findAndReplaceState.replaceType == ReplaceType.CURRENT) {
                // Check if index is valid
                if (indices.isEmpty() || currentIndex >= indices.size) return@LaunchedEffect
                // Get the position to be replaced
                val (startIndex, endIndex) = indices[currentIndex]
                // Execute replacement
                state.edit {
                    replace(startIndex, endIndex, findAndReplaceState.replaceWord)
                }
            }
            onFindAndReplaceUpdate(findAndReplaceState.copy(replaceType = null))
        }
    }

    // Calculate line positions for line numbers
    LaunchedEffect(showLineNumbers, textLayoutResult) {
        if (!showLineNumbers || textLayoutResult == null) {
            actualLinePositions = emptyList()
            return@LaunchedEffect
        }

        withContext(Dispatchers.Default) {
            val text = state.text.toString()
            val layout = textLayoutResult
            val newLines = ArrayList<Pair<Int, Float>>(text.count { it == '\n' } + 1)

            // Always add the first line
            newLines.add(0 to layout.getLineTop(0))

            // Find all newline characters and map them to layout positions
            var lineStartIndex = 0
            text.forEachIndexed { index, char ->
                if (char == '\n') {
                    lineStartIndex = index + 1
                    val lineNumber = layout.getLineForOffset(lineStartIndex)
                    newLines.add(lineStartIndex to layout.getLineTop(lineNumber))
                }
            }

            actualLinePositions = newLines
        }
    }

    content(indices, currentIndex, actualLinePositions)
}
