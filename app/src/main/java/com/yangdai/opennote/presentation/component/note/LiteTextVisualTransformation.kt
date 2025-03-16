package com.yangdai.opennote.presentation.component.note

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.TextUnit
import com.yangdai.opennote.presentation.util.BOLD_ITALIC_STYLE
import com.yangdai.opennote.presentation.util.BOLD_STYLE
import com.yangdai.opennote.presentation.util.CODE_BLOCK_STYLE
import com.yangdai.opennote.presentation.util.CODE_STYLE
import com.yangdai.opennote.presentation.util.CURRENT_SEARCH_WORD_STYLE
import com.yangdai.opennote.presentation.util.HEADER_LINE_STYLES
import com.yangdai.opennote.presentation.util.HEADER_STYLES
import com.yangdai.opennote.presentation.util.HIGHLIGHT_STYLE
import com.yangdai.opennote.presentation.util.ITALIC_STYLE
import com.yangdai.opennote.presentation.util.KEYWORD_STYLE
import com.yangdai.opennote.presentation.util.LINK_STYLE
import com.yangdai.opennote.presentation.util.MARKER_STYLE
import com.yangdai.opennote.presentation.util.PROPERTIES_STYLE
import com.yangdai.opennote.presentation.util.SEARCH_WORD_STYLE
import com.yangdai.opennote.presentation.util.STRIKETHROUGH_AND_UNDERLINE_STYLE
import com.yangdai.opennote.presentation.util.STRIKETHROUGH_STYLE
import com.yangdai.opennote.presentation.util.SYMBOL_STYLE
import com.yangdai.opennote.presentation.util.StyleRanges
import com.yangdai.opennote.presentation.util.UNDERLINE_STYLE
import com.yangdai.opennote.presentation.util.extension.properties.Properties.getPropertiesRange
import com.yangdai.opennote.presentation.util.findTagRanges
import com.yangdai.opennote.presentation.util.overlaps

class LiteTextVisualTransformation(
    private val readMode: Boolean,
    private val searchWordRanges: List<Pair<Int, Int>>,
    private val currentSearchWordIndex: Int,
    private val selection: TextRange
) : VisualTransformation {

    override fun filter(string: AnnotatedString): TransformedText {
        val text = string.text
        val propertiesRange = text.getPropertiesRange()
        var styleRanges = findTagRanges(text)
        if (propertiesRange != null) {
            styleRanges = StyleRanges(
                codeRanges = styleRanges.codeRanges.filter { !it.overlaps(propertiesRange) },
                boldRanges = styleRanges.boldRanges.filter { !it.overlaps(propertiesRange) },
                italicRanges = styleRanges.italicRanges.filter { !it.overlaps(propertiesRange) },
                boldItalicRanges = styleRanges.boldItalicRanges.filter {
                    !it.overlaps(
                        propertiesRange
                    )
                },
                strikethroughRanges = styleRanges.strikethroughRanges.filter {
                    !it.overlaps(
                        propertiesRange
                    )
                },
                underlineRanges = styleRanges.underlineRanges.filter { !it.overlaps(propertiesRange) },
                highlightRanges = styleRanges.highlightRanges.filter { !it.overlaps(propertiesRange) },
                headerRanges = styleRanges.headerRanges.filter { !it.first.overlaps(propertiesRange) },
                markerRanges = styleRanges.markerRanges.filter { !it.overlaps(propertiesRange) },
                linkRanges = styleRanges.linkRanges.filter { !it.overlaps(propertiesRange) },
                fencedCodeBlockInfoRanges = styleRanges.fencedCodeBlockInfoRanges.filter {
                    !it.overlaps(
                        propertiesRange
                    )
                },
                codeBlockContentRanges = styleRanges.codeBlockContentRanges.filter {
                    !it.overlaps(
                        propertiesRange
                    )
                }
            )
        }
        val annotatedString = buildAnnotatedString {
            // 应用语法内文本样式
            applyTextStyles(styleRanges)
            // 应用语法符号样式
            applySymbols(string, styleRanges)
            if (propertiesRange != null) applyPropertiesStyle(propertiesRange)
            if (!readMode)
                applySearchWordStyle(searchWordRanges, currentSearchWordIndex)
            append(string)
        }
        return TransformedText(
            text = annotatedString, offsetMapping = OffsetMapping.Identity
        )
    }

    private fun AnnotatedString.Builder.applyTextStyles(ranges: StyleRanges) {
        ranges.codeRanges.forEach { range ->
            addStyle(CODE_STYLE, range.first, range.last + 1)
        }
        ranges.boldItalicRanges.forEach { range ->
            addStyle(BOLD_ITALIC_STYLE, range.first, range.last + 1)
        }
        ranges.boldRanges.forEach { range ->
            addStyle(BOLD_STYLE, range.first, range.last + 1)
        }
        ranges.italicRanges.forEach { range ->
            addStyle(ITALIC_STYLE, range.first, range.last + 1)
        }
        ranges.highlightRanges.forEach { range ->
            addStyle(HIGHLIGHT_STYLE, range.first, range.last + 1)
        }

        val combinedRanges = (ranges.strikethroughRanges + ranges.underlineRanges).distinct()
        combinedRanges.forEach { range ->
            val hasStrikethrough = ranges.strikethroughRanges.any { it.overlaps(range) }
            val hasUnderline = ranges.underlineRanges.any { it.overlaps(range) }
            val style = when {
                hasStrikethrough && hasUnderline -> STRIKETHROUGH_AND_UNDERLINE_STYLE
                hasStrikethrough -> STRIKETHROUGH_STYLE
                hasUnderline -> UNDERLINE_STYLE
                else -> return@forEach
            }
            addStyle(style, range.first, range.last + 1)
        }

        ranges.headerRanges.forEach { (range, level) ->
            addStyle(HEADER_STYLES[level - 1], range.first, range.last + 1)
            addStyle(HEADER_LINE_STYLES[level - 1], range.first, range.last + 1)
        }

        // Add styling for markers and links
        ranges.markerRanges.forEach { range ->
            addStyle(MARKER_STYLE, range.first, range.last + 1)
        }
        ranges.linkRanges.forEach { range ->
            addStyle(LINK_STYLE, range.first, range.last + 1)
        }
        ranges.fencedCodeBlockInfoRanges.forEach { range ->
            addStyle(KEYWORD_STYLE, range.first, range.last + 1)
        }
        ranges.codeBlockContentRanges.forEach { range ->
            addStyle(CODE_BLOCK_STYLE, range.first, range.last + 1)
        }
    }

    private fun AnnotatedString.Builder.applySymbolsCommon(
        ranges: StyleRanges, isInAffectedLines: ((IntRange) -> Boolean)?
    ) {
        fun applyRangeStyle(range: IntRange, startOffset: Int, endOffset: Int) {
            if (isInAffectedLines == null) {
                addStyle(SYMBOL_STYLE, range.first, range.first + startOffset)
                addStyle(SYMBOL_STYLE, range.last - endOffset + 1, range.last + 1)
                return
            }
            val style = if (isInAffectedLines(range)) {
                SYMBOL_STYLE.copy(fontSize = TextUnit.Unspecified)
            } else {
                SYMBOL_STYLE
            }
            addStyle(style, range.first, range.first + startOffset)
            addStyle(style, range.last - endOffset + 1, range.last + 1)
        }

        ranges.codeRanges.forEach { range -> applyRangeStyle(range, 1, 1) }
        ranges.boldItalicRanges.forEach { range -> applyRangeStyle(range, 3, 3) }
        ranges.boldRanges.forEach { range -> applyRangeStyle(range, 2, 2) }
        ranges.italicRanges.forEach { range -> applyRangeStyle(range, 1, 1) }
        ranges.strikethroughRanges.forEach { range -> applyRangeStyle(range, 2, 2) }
        ranges.underlineRanges.forEach { range -> applyRangeStyle(range, 2, 2) }
        ranges.highlightRanges.forEach { range -> applyRangeStyle(range, 2, 2) }
        ranges.headerRanges.forEach { (range, level) ->
            if (isInAffectedLines == null) {
                addStyle(SYMBOL_STYLE, range.first, range.first + level + 1)
            } else {
                val style = if (isInAffectedLines(range)) {
                    SYMBOL_STYLE.copy(fontSize = TextUnit.Unspecified)
                } else {
                    SYMBOL_STYLE
                }
                addStyle(style, range.first, range.first + level + 1)
            }
        }
    }

    private fun AnnotatedString.Builder.applySymbols(
        text: AnnotatedString, ranges: StyleRanges
    ) {
        if (selection.start < 0 || selection.end < 0 || readMode) {
            applySymbolsCommon(ranges, null)
            return
        }

        val lines = text.lines()
        val lineStarts = IntArray(lines.size + 1).also {
            var pos = 0
            lines.forEachIndexed { index, line ->
                it[index] = pos
                pos += line.length + 1
            }
            it[lines.size] = pos
        }
        // 根据光标位置确定要完全显示的行
        val affectedLines = if (selection.collapsed) {
            val lineIdx = lineStarts.indexOfFirst { it > selection.start } - 1
            lineIdx..lineIdx
        } else {
            val startLine = lineStarts.indexOfFirst { it > selection.start } - 1
            val endLine = lineStarts.indexOfFirst { it > selection.end } - 1
            startLine..endLine
        }

        fun isInAffectedLines(range: IntRange): Boolean {
            val startLine = lineStarts.indexOfFirst { it > range.first } - 1
            return startLine in affectedLines
        }

        applySymbolsCommon(ranges, ::isInAffectedLines)
    }

    private fun AnnotatedString.Builder.applyPropertiesStyle(range: IntRange) {
        addStyle(PROPERTIES_STYLE, range.first, range.last + 1)
    }

    private fun AnnotatedString.Builder.applySearchWordStyle(
        ranges: List<Pair<Int, Int>>,
        index: Int
    ) {
        ranges.forEach { range ->
            addStyle(SEARCH_WORD_STYLE, range.first, range.second)
        }
        val currentRange = ranges.getOrNull(index) ?: return
        addStyle(CURRENT_SEARCH_WORD_STYLE, currentRange.first, currentRange.second)
    }
}
