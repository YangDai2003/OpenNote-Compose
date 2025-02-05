package com.yangdai.opennote.presentation.component.text

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.PlatformParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

class LiteTextVisualTransformation(
    private val readMode: Boolean,
    private val searchWord: String,
    private val selection: TextRange
) : VisualTransformation {

    private data class StyleRanges(
        val codeRanges: List<IntRange>,
        val boldRanges: List<IntRange>,
        val italicRanges: List<IntRange>,
        val boldItalicRanges: List<IntRange>,
        val strikethroughRanges: List<IntRange>,
        val underlineRanges: List<IntRange>,
        val headerRanges: List<Pair<IntRange, Int>>,
        val searchWordRanges: List<IntRange>
    ) {
        companion object {
            val EMPTY = StyleRanges(
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList()
            )
        }
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val styleRanges = findAllRanges(text)
        val annotatedString = buildAnnotatedString {
            // 应用语法内文本样式
            applyTextStyles(styleRanges)
            // 应用语法符号样式
            applySymbols(text, styleRanges)
            if (!readMode) applySearchWordStyle(styleRanges.searchWordRanges)
            append(text)
        }
        return TransformedText(
            text = annotatedString,
            offsetMapping = OffsetMapping.Identity
        )
    }

    private fun findFilteredRanges(
        regex: Regex,
        textString: String,
        excludeRanges: List<IntRange> = emptyList()
    ): List<IntRange> {
        return regex.findAll(textString)
            .map { it.range }
            .filterNot { range -> excludeRanges.any { it.overlaps(range) } }
            .toList()
    }

    private fun findAllRanges(text: AnnotatedString): StyleRanges {
        if (text.isEmpty()) return StyleRanges.EMPTY
        val textString = text.text

        // 优先找出 codeRanges
        val codeRanges = REGEX_PATTERNS[StyleType.CODE]!!
            .findAll(textString)
            .map { it.range }
            .toList()

        // 找出其他区间时排除 codeRanges
        val boldItalicRanges = findFilteredRanges(
            REGEX_PATTERNS[StyleType.BOLD_ITALIC]!!,
            textString,
            codeRanges
        )
        val boldRanges = findFilteredRanges(
            REGEX_PATTERNS[StyleType.BOLD]!!,
            textString,
            codeRanges + boldItalicRanges
        )
        val italicRanges = findFilteredRanges(
            REGEX_PATTERNS[StyleType.ITALIC]!!,
            textString,
            codeRanges + boldItalicRanges + boldRanges
        )
        val strikethroughRanges = findFilteredRanges(
            REGEX_PATTERNS[StyleType.STRIKETHROUGH]!!,
            textString,
            codeRanges
        )
        val underlineRanges = findFilteredRanges(
            REGEX_PATTERNS[StyleType.UNDERLINE]!!,
            textString,
            codeRanges
        )
        val headerRanges = REGEX_PATTERNS[StyleType.HEADER]!!
            .findAll(textString)
            .map { it.range to it.groupValues[1].length }
            .toList()
        val searchWordRanges = findSearchWordRanges(textString)

        return StyleRanges(
            codeRanges,
            boldRanges,
            italicRanges,
            boldItalicRanges,
            strikethroughRanges,
            underlineRanges,
            headerRanges,
            searchWordRanges
        )
    }

    private fun AnnotatedString.Builder.applyTextStyles(ranges: StyleRanges) {
        ranges.codeRanges.forEach { range -> addStyle(CODE_STYLE, range.first, range.last + 1) }
        ranges.boldItalicRanges.forEach { range -> addStyle(BOLD_ITALIC_STYLE, range.first, range.last + 1) }
        ranges.boldRanges.forEach { range -> addStyle(BOLD_STYLE, range.first, range.last + 1) }
        ranges.italicRanges.forEach { range -> addStyle(ITALIC_STYLE, range.first, range.last + 1) }

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
    }

    private fun AnnotatedString.Builder.applySymbolsCommon(
        ranges: StyleRanges,
        isInAffectedLines: ((IntRange) -> Boolean)?
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
        text: AnnotatedString,
        ranges: StyleRanges
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

    private fun AnnotatedString.Builder.applySearchWordStyle(ranges: List<IntRange>) {
        ranges.forEach { range ->
            addStyle(SEARCH_WORD_STYLE, range.first, range.last + 1)
        }
    }

    private fun IntRange.overlaps(other: IntRange): Boolean {
        return this.first <= other.last && other.first <= this.last
    }

    private fun findSearchWordRanges(text: String): List<IntRange> {
        if (searchWord.isBlank()) return emptyList()
        val ranges = mutableListOf<IntRange>()
        var index = text.indexOf(searchWord, ignoreCase = false)
        while (index >= 0) {
            ranges.add(index until index + searchWord.length)
            index = text.indexOf(searchWord, index + 1, ignoreCase = false)
        }
        return ranges
    }

    private enum class StyleType {
        CODE, BOLD, ITALIC, BOLD_ITALIC, STRIKETHROUGH, UNDERLINE, HEADER
    }

    companion object {
        private const val ITALIC_PATTERN =
            """(?<![*_])\*(?!\*)[^*\n]+?\*(?![*])|(?<![*_])_(?!_)[^_\n]+?_(?!_)"""

        private const val BOLD_PATTERN =
            """\*\*(?!\*)[^*\n]+?\*\*(?!\*)|__(?!_)[^_\n]+?__(?!_)"""

        private const val BOLD_ITALIC_PATTERN =
            """\*\*\*(?!\s*\*\*\*)\s*\S[\s\S]*?\*\*\*|""" +  // ***text***
                    """___(?!\s*___)\s*\S[\s\S]*?___|""" +           // ___text___
                    """\*\*_(?!\s*_)\s*\S[\s\S]*?_\*\*|""" +        // **_text_**
                    """__\*(?!\s*\*)\s*\S[\s\S]*?\*__|""" +         // __*text*__
                    """\*__(?!\s*__)\s*\S[\s\S]*?__\*|""" +         // *__text__*
                    """_\*\*(?!\s*\*\*)\s*\S[\s\S]*?\*\*_"""        // _**text**_

        private val REGEX_PATTERNS = mapOf(
            StyleType.BOLD to BOLD_PATTERN.toRegex(),
            StyleType.ITALIC to ITALIC_PATTERN.toRegex(),
            StyleType.BOLD_ITALIC to BOLD_ITALIC_PATTERN.toRegex(),
            StyleType.STRIKETHROUGH to """~~(?!\s*~~)\s*\S[\s\S]*?~~""".toRegex(),
            StyleType.UNDERLINE to """\+\+(?!\s*\+\+)\s*\S[\s\S]*?\+\+""".toRegex(),
            StyleType.CODE to "`.+?`".toRegex(RegexOption.DOT_MATCHES_ALL),
            StyleType.HEADER to "^(#{1,6})\\s+\\S.*\n".toRegex(RegexOption.MULTILINE)
        )

        private val SYMBOL_STYLE = SpanStyle(
            fontWeight = FontWeight.Thin,
            fontStyle = FontStyle.Normal,
            fontSize = 0.sp,
            color = Color.Gray,
            textDecoration = TextDecoration.None,
            fontFamily = FontFamily.Default,
            background = Color.Transparent
        )

        private val BOLD_STYLE =
            SpanStyle(fontWeight = FontWeight.ExtraBold, fontSynthesis = FontSynthesis.Weight)
        private val ITALIC_STYLE =
            SpanStyle(fontStyle = FontStyle.Italic, fontSynthesis = FontSynthesis.Style)
        private val BOLD_ITALIC_STYLE =
            SpanStyle(
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                fontSynthesis = FontSynthesis.All
            )
        private val STRIKETHROUGH_STYLE = SpanStyle(textDecoration = TextDecoration.LineThrough)
        private val UNDERLINE_STYLE = SpanStyle(textDecoration = TextDecoration.Underline)
        private val STRIKETHROUGH_AND_UNDERLINE_STYLE =
            SpanStyle(textDecoration = TextDecoration.LineThrough + TextDecoration.Underline)
        private val CODE_STYLE = SpanStyle(
            fontFamily = FontFamily.Monospace,
            background = Color.LightGray.copy(alpha = 0.3f)
        )
        private val SEARCH_WORD_STYLE = SpanStyle(background = Color.Cyan.copy(alpha = 0.5f))

        private val HEADER_LINE_STYLES = listOf(
            ParagraphStyle(
                lineHeight = 1.5.em,
                textIndent = TextIndent.None,
                platformStyle = PlatformParagraphStyle(includeFontPadding = true)
            ),
            ParagraphStyle(
                lineHeight = 1.3.em,
                textIndent = TextIndent.None,
                platformStyle = PlatformParagraphStyle(includeFontPadding = true)
            ),
            ParagraphStyle(
                lineHeight = 1.15.em,
                textIndent = TextIndent.None,
                platformStyle = PlatformParagraphStyle(includeFontPadding = true)
            ),
            ParagraphStyle(
                lineHeight = 1.em,
                textIndent = TextIndent.None,
                platformStyle = PlatformParagraphStyle(includeFontPadding = true)
            ),
            ParagraphStyle(
                lineHeight = 0.83.em,
                textIndent = TextIndent.None,
                platformStyle = PlatformParagraphStyle(includeFontPadding = true)
            ),
            ParagraphStyle(
                lineHeight = 0.67.em,
                textIndent = TextIndent.None,
                platformStyle = PlatformParagraphStyle(includeFontPadding = true)
            )
        )

        private val HEADER_STYLES = listOf(
            SpanStyle(
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                fontSynthesis = FontSynthesis.Weight
            ),
            SpanStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                fontSynthesis = FontSynthesis.Weight
            ),
            SpanStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontSynthesis = FontSynthesis.Weight
            ),
            SpanStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontSynthesis = FontSynthesis.Weight
            ),
            SpanStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontSynthesis = FontSynthesis.Weight
            ),
            SpanStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontSynthesis = FontSynthesis.Weight
            )
        )
    }
}
