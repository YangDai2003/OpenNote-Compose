package com.yangdai.opennote.presentation.component.text

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.PlatformParagraphStyle
import androidx.compose.ui.text.SpanStyle
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
import androidx.compose.ui.unit.sp

class LiteTextVisualTransformation(private val readMode: Boolean, private val searchWord: String) :
    VisualTransformation {

    // 缓存找到的范围结果
    private data class StyleRanges(
        val codeRanges: List<IntRange>,
        val boldRanges: List<IntRange>,
        val italicRanges: List<IntRange>,
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
                emptyList()
            )
        }
    }

    override fun filter(text: AnnotatedString): TransformedText {

        val styleRanges = findAllRanges(text)

        val annotatedString = buildAnnotatedString {
            applyTextStyles(styleRanges)
            applySymbols(styleRanges, readMode)
            if (!readMode)
                applySearchWordStyle(styleRanges.searchWordRanges)
            append(text)
        }

        return TransformedText(
            text = annotatedString, offsetMapping = OffsetMapping.Identity
        )
    }

    private fun findAllRanges(text: AnnotatedString): StyleRanges {
        if (text.isEmpty()) return StyleRanges.EMPTY

        val codeRanges = REGEX_PATTERNS[StyleType.CODE]!!.findAll(text).map { it.range }.toList()

        fun findNonCodeRanges(pattern: Regex) = pattern.findAll(text)
            .map { it.range }
            .filterNot { range -> codeRanges.any { it.overlaps(range) } }
            .toList()

        return StyleRanges(
            codeRanges = codeRanges,
            boldRanges = findNonCodeRanges(REGEX_PATTERNS[StyleType.BOLD]!!),
            italicRanges = findNonCodeRanges(REGEX_PATTERNS[StyleType.ITALIC]!!),
            strikethroughRanges = findNonCodeRanges(REGEX_PATTERNS[StyleType.STRIKETHROUGH]!!),
            underlineRanges = findNonCodeRanges(REGEX_PATTERNS[StyleType.UNDERLINE]!!),
            headerRanges = REGEX_PATTERNS[StyleType.HEADER]!!.findAll(text)
                .map { it.range to it.groupValues[1].length }
                .toList(),
            searchWordRanges = findSearchWordRanges(text.text)
        )
    }

    private fun AnnotatedString.Builder.applyTextStyles(ranges: StyleRanges) {
        // 应用代码样式
        ranges.codeRanges.forEach { range ->
            addStyle(CODE_STYLE, range.first, range.last + 1)
        }

        // 应用加粗样式
        ranges.boldRanges.forEach { range ->
            addStyle(BOLD_STYLE, range.first, range.last + 1)
        }

        // 应用斜体样式
        ranges.italicRanges.forEach { range ->
            addStyle(ITALIC_STYLE, range.first, range.last + 1)
        }

        // 应用删除线和下划线样式
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

        // 应用标题样式
        ranges.headerRanges.forEach { (range, level) ->
            addStyle(HEADER_STYLES[level - 1], range.first, range.last + 1)
            addStyle(HEADER_LINE_STYLES[level - 1], range.first, range.last + 1)
        }
    }

    private fun AnnotatedString.Builder.applySymbols(ranges: StyleRanges, readMode: Boolean) {

        val symbolStyle =
            if (readMode) SYMBOL_STYLE else SYMBOL_STYLE.copy(fontSize = TextUnit.Unspecified)

        ranges.codeRanges.forEach { range ->
            addStyle(symbolStyle, range.first, range.first + 1)
            addStyle(symbolStyle, range.last, range.last + 1)
        }

        ranges.boldRanges.forEach { range ->
            addStyle(symbolStyle, range.first, range.first + 2)
            addStyle(symbolStyle, range.last - 1, range.last + 1)
        }

        ranges.italicRanges.forEach { range ->
            addStyle(symbolStyle, range.first, range.first + 1)
            addStyle(symbolStyle, range.last, range.last + 1)
        }

        ranges.strikethroughRanges.forEach { range ->
            addStyle(symbolStyle, range.first, range.first + 2)
            addStyle(symbolStyle, range.last - 1, range.last + 1)
        }

        ranges.underlineRanges.forEach { range ->
            addStyle(symbolStyle, range.first, range.first + 2)
            addStyle(symbolStyle, range.last - 1, range.last + 1)
        }

        ranges.headerRanges.forEach { (range, level) ->
            addStyle(symbolStyle, range.first, range.first + level + 1)
        }
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
        CODE, BOLD, ITALIC, STRIKETHROUGH, UNDERLINE, HEADER
    }

    companion object {
        // 正则表达式
        private val REGEX_PATTERNS = mapOf(
            StyleType.BOLD to """\*\*(?!\s*\*\*)\s*\S[\s\S]*?\*\*""".toRegex(),
            StyleType.ITALIC to """_(?!\s*_)\s*\S[\s\S]*?_""".toRegex(),
            StyleType.STRIKETHROUGH to """~~(?!\s*~~)\s*\S[\s\S]*?~~""".toRegex(),
            StyleType.UNDERLINE to """\+\+(?!\s*\+\+)\s*\S[\s\S]*?\+\+""".toRegex(),
            StyleType.CODE to "`.+?`".toRegex(RegexOption.DOT_MATCHES_ALL),
            StyleType.HEADER to "^(#{1,6})\\s+\\S.*\n".toRegex(RegexOption.MULTILINE)
        )

        // 语法符号样式定义
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
            SpanStyle(fontWeight = FontWeight.Bold, fontSynthesis = FontSynthesis.Weight)
        private val ITALIC_STYLE =
            SpanStyle(fontStyle = FontStyle.Italic, fontSynthesis = FontSynthesis.Style)
        private val STRIKETHROUGH_STYLE = SpanStyle(textDecoration = TextDecoration.LineThrough)
        private val UNDERLINE_STYLE = SpanStyle(textDecoration = TextDecoration.Underline)
        private val STRIKETHROUGH_AND_UNDERLINE_STYLE =
            SpanStyle(textDecoration = TextDecoration.LineThrough + TextDecoration.Underline)
        private val CODE_STYLE = SpanStyle(
            fontFamily = FontFamily.Monospace, background = Color.LightGray.copy(alpha = 0.3f)
        )
        private val SEARCH_WORD_STYLE = SpanStyle(background = Color.Cyan.copy(alpha = 0.5f))

        private val HEADER_LINE_STYLES = listOf(
            ParagraphStyle(
                lineHeight = 26.sp,
                textIndent = TextIndent.None,
                platformStyle = PlatformParagraphStyle(includeFontPadding = true)
            ),
            ParagraphStyle(
                lineHeight = 24.sp,
                textIndent = TextIndent.None,
                platformStyle = PlatformParagraphStyle(includeFontPadding = true)
            ),
            ParagraphStyle(
                lineHeight = 22.sp,
                textIndent = TextIndent.None,
                platformStyle = PlatformParagraphStyle(includeFontPadding = true)
            ),
            ParagraphStyle(
                lineHeight = 20.sp,
                textIndent = TextIndent.None,
                platformStyle = PlatformParagraphStyle(includeFontPadding = true)
            ),
            ParagraphStyle(
                lineHeight = 20.sp,
                textIndent = TextIndent.None,
                platformStyle = PlatformParagraphStyle(includeFontPadding = true)
            ),
            ParagraphStyle(
                lineHeight = 20.sp,
                textIndent = TextIndent.None,
                platformStyle = PlatformParagraphStyle(includeFontPadding = true)
            )
        )

        // 标题样式
        private val HEADER_STYLES = listOf(
            SpanStyle(
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                fontSynthesis = FontSynthesis.Weight
            ), SpanStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                fontSynthesis = FontSynthesis.Weight
            ), SpanStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontSynthesis = FontSynthesis.Weight
            ), SpanStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontSynthesis = FontSynthesis.Weight
            ), SpanStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontSynthesis = FontSynthesis.Weight
            ), SpanStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontSynthesis = FontSynthesis.Weight
            )
        )
    }
}
