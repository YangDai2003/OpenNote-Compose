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
import androidx.compose.ui.unit.sp

class LiteTextVisualTransformation(private val readMode: Boolean) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val annotatedString = buildAnnotatedString {
            // 应用所有样式
            applyTextStyles(text)

            if (readMode)
                applySymbols(text)

            append(text)
        }
        return TransformedText(
            text = annotatedString,
            offsetMapping = OffsetMapping.Identity
        )
    }

    private fun AnnotatedString.Builder.applyTextStyles(text: AnnotatedString) {
        // 处理代码文本
        val codeRanges = CODE_REGEX.findAll(text).map { it.range }.toList()
        codeRanges.forEach { range ->
            val start = range.first
            val end = range.last + 1
            addStyle(CODE_STYLE, start, end)
        }

        // 处理加粗文本
        BOLD_REGEX.findAll(text).forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1
            if (!codeRanges.any { it.contains(start) || it.contains(end) }) {
                addStyle(BOLD_STYLE, start, end)
            }
        }

        // 处理斜体文本
        ITALIC_REGEX.findAll(text).forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1
            if (!codeRanges.any { it.contains(start) || it.contains(end) }) {
                addStyle(ITALIC_STYLE, start, end)
            }
        }

        // 处理删除线和下划线文本避免冲突
        val strikethroughMatches = STRIKETHROUGH_REGEX.findAll(text).map { it.range }.toList()
        val underlineMatches = UNDERLINE_REGEX.findAll(text).map { it.range }.toList()

        val combinedRanges = (strikethroughMatches + underlineMatches).distinct()

        combinedRanges.forEach { range ->
            val start = range.first
            val end = range.last + 1
            if (!codeRanges.any { it.contains(start) || it.contains(end) }) {
                val hasStrikethrough = strikethroughMatches.any { it.overlaps(range) }
                val hasUnderline = underlineMatches.any { it.overlaps(range) }

                when {
                    hasStrikethrough && hasUnderline -> addStyle(
                        STRIKETHROUGH_AND_UNDERLINE_STYLE,
                        start,
                        end
                    )

                    hasStrikethrough -> addStyle(STRIKETHROUGH_STYLE, start, end)
                    hasUnderline -> addStyle(UNDERLINE_STYLE, start, end)
                }
            }
        }

        // 处理标题文本
        HEADER_REGEX.findAll(text).forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1
            val headerLevel = matchResult.groupValues[1].length
            addStyle(HEADER_STYLES[headerLevel - 1], start, end)
            addStyle(HEADER_LINE_STYLES[headerLevel - 1], start, end)
        }
    }

    private fun AnnotatedString.Builder.applySymbols(text: AnnotatedString) {
        // 处理代码文本
        val codeRanges = CODE_REGEX.findAll(text).map { it.range }.toList()
        codeRanges.forEach { matchResult ->
            val start = matchResult.first
            val end = matchResult.last + 1
            addStyle(SYMBOL_STYLE, start, start + 1)
            addStyle(SYMBOL_STYLE, end - 1, end)
        }

        // 处理加粗文本
        BOLD_REGEX.findAll(text).forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1
            if (!codeRanges.any { it.contains(start) || it.contains(end) }) {
                addStyle(SYMBOL_STYLE, start, start + 2)
                addStyle(SYMBOL_STYLE, end - 2, end)
            }
        }

        // 处理斜体文本
        ITALIC_REGEX.findAll(text).forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1
            if (!codeRanges.any { it.contains(start) || it.contains(end) }) {
                addStyle(SYMBOL_STYLE, start, start + 1)
                addStyle(SYMBOL_STYLE, end - 1, end)
            }
        }

        // 处理删除线文本
        STRIKETHROUGH_REGEX.findAll(text).forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1
            if (!codeRanges.any { it.contains(start) || it.contains(end) }) {
                addStyle(SYMBOL_STYLE, start, start + 2)
                addStyle(SYMBOL_STYLE, end - 2, end)
            }
        }

        // 处理下划线文本
        UNDERLINE_REGEX.findAll(text).forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1
            if (!codeRanges.any { it.contains(start) || it.contains(end) }) {
                addStyle(SYMBOL_STYLE, start, start + 2)
                addStyle(SYMBOL_STYLE, end - 2, end)
            }
        }

        // 处理标题文本
        HEADER_REGEX.findAll(text).forEach { matchResult ->
            val start = matchResult.range.first
            val headerLevel = matchResult.groupValues[1].length
            addStyle(SYMBOL_STYLE, start, start + headerLevel + 1)
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

        // 样式定义
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
            fontFamily = FontFamily.Monospace,
            background = Color.LightGray.copy(alpha = 0.3f)
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
            ),
            SpanStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                fontSynthesis = FontSynthesis.Weight
            ),
            SpanStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontSynthesis = FontSynthesis.Weight
            ),
            SpanStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontSynthesis = FontSynthesis.Weight
            ),
            SpanStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontSynthesis = FontSynthesis.Weight
            ),
            SpanStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontSynthesis = FontSynthesis.Weight
            )
        )
    }
}
