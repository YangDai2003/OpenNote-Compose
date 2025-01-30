package com.yangdai.opennote.presentation.component.text

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
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

class LiteTextVisualTransformation(private val readMode: Boolean) : VisualTransformation {

    // 缓存找到的范围结果
    private data class StyleRanges(
        val codeRanges: List<IntRange>,
        val boldRanges: List<IntRange>,
        val italicRanges: List<IntRange>,
        val strikethroughRanges: List<IntRange>,
        val underlineRanges: List<IntRange>,
        val headerRanges: List<Pair<IntRange, Int>>
    ) {
        companion object {
            val EMPTY = StyleRanges(
                emptyList(), emptyList(), emptyList(),
                emptyList(), emptyList(), emptyList()
            )
        }
    }

    override fun filter(text: AnnotatedString): TransformedText {

        val styleRanges = findAllRanges(text)

        val annotatedString = buildAnnotatedString {
            applyTextStyles(styleRanges)
            applySymbols(styleRanges, readMode)
            append(text)
        }

        return TransformedText(
            text = annotatedString, offsetMapping = OffsetMapping.Identity
        )
    }

    private fun findAllRanges(text: AnnotatedString): StyleRanges {

        if (text.isEmpty()) return StyleRanges.EMPTY

        val codeRanges = CODE_REGEX.findAll(text).map { it.range }.toList()

        val boldRanges = BOLD_REGEX.findAll(text).map { it.range }
            .filterNot { range -> codeRanges.any { it.overlaps(range) } }.toList()

        val italicRanges = ITALIC_REGEX.findAll(text).map { it.range }
            .filterNot { range -> codeRanges.any { it.overlaps(range) } }.toList()

        val strikethroughRanges = STRIKETHROUGH_REGEX.findAll(text).map { it.range }
            .filterNot { range -> codeRanges.any { it.overlaps(range) } }.toList()

        val underlineRanges = UNDERLINE_REGEX.findAll(text).map { it.range }
            .filterNot { range -> codeRanges.any { it.overlaps(range) } }.toList()

        val headerRanges = HEADER_REGEX.findAll(text)
            .map { it.range to it.groupValues[1].length }.toList()

        return StyleRanges(
            codeRanges, boldRanges, italicRanges, strikethroughRanges, underlineRanges, headerRanges
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

    private fun IntRange.overlaps(other: IntRange): Boolean {
        return this.first <= other.last && other.first <= this.last
    }

    companion object {
        // 正则表达式
        private val BOLD_REGEX = """\*\*(?!\s*\*\*)\s*\S[\s\S]*?\*\*""".toRegex()
        private val ITALIC_REGEX = """_(?!\s*_)\s*\S[\s\S]*?_""".toRegex()
        private val STRIKETHROUGH_REGEX = """~~(?!\s*~~)\s*\S[\s\S]*?~~""".toRegex()
        private val UNDERLINE_REGEX = """\+\+(?!\s*\+\+)\s*\S[\s\S]*?\+\+""".toRegex()
        private val CODE_REGEX = "`.+?`".toRegex(RegexOption.DOT_MATCHES_ALL)
        private val HEADER_REGEX = "^(#{1,6})\\s+\\S.*\n".toRegex(RegexOption.MULTILINE)

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
        private val BOLD_STYLE = SpanStyle(fontWeight = FontWeight.Bold)
        private val ITALIC_STYLE = SpanStyle(fontStyle = FontStyle.Italic)
        private val STRIKETHROUGH_STYLE = SpanStyle(textDecoration = TextDecoration.LineThrough)
        private val UNDERLINE_STYLE = SpanStyle(textDecoration = TextDecoration.Underline)
        private val STRIKETHROUGH_AND_UNDERLINE_STYLE =
            SpanStyle(textDecoration = TextDecoration.LineThrough + TextDecoration.Underline)
        private val CODE_STYLE = SpanStyle(
            fontFamily = FontFamily.Monospace, background = Color.LightGray.copy(alpha = 0.3f)
        )

        private val HEADER_LINE_STYLES = listOf(
            ParagraphStyle(lineHeight = 26.sp, textIndent = TextIndent.None),
            ParagraphStyle(lineHeight = 24.sp, textIndent = TextIndent.None),
            ParagraphStyle(lineHeight = 22.sp, textIndent = TextIndent.None),
            ParagraphStyle(lineHeight = 20.sp, textIndent = TextIndent.None),
            ParagraphStyle(lineHeight = 20.sp, textIndent = TextIndent.None),
            ParagraphStyle(lineHeight = 20.sp, textIndent = TextIndent.None)
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
                fontSize = 24.sp, fontWeight = FontWeight.Bold, fontSynthesis = FontSynthesis.Weight
            ), SpanStyle(
                fontSize = 20.sp, fontWeight = FontWeight.Bold, fontSynthesis = FontSynthesis.Weight
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
