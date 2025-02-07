package com.yangdai.opennote.presentation.component.text

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.yangdai.opennote.presentation.component.text.LiteTextVisualTransformation.StyleType
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.node.*
import org.commonmark.parser.Parser
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.parser.IncludeSourceSpans
import org.commonmark.renderer.NodeRenderer
import org.commonmark.renderer.html.HtmlNodeRendererContext

class LiteTextVisualTransformation2() : VisualTransformation {

    private data class MarkdownStyle(
        val range: IntRange,
        val spanStyle: SpanStyle,
        val paragraphStyle: ParagraphStyle? = null
    )

    override fun filter(text: AnnotatedString): TransformedText {
        val styles = parseMarkdown(text.text)

        val annotatedString = buildAnnotatedString {
            // 应用段落样式
            styles.filter { it.paragraphStyle != null }
                .forEach { style ->
                    style.paragraphStyle?.let {
                        addStyle(it, style.range.first, style.range.last + 1)
                    }
                }

            // 应用文本样式
            styles.forEach { style ->
                addStyle(style.spanStyle, style.range.first, style.range.last + 1)
            }

            append(text)
        }

        return TransformedText(annotatedString, OffsetMapping.Identity)
    }

    private fun parseMarkdown(text: String): List<MarkdownStyle> {
        val extensions = listOf(StrikethroughExtension.create())

        val parser = Parser.builder()
            .extensions(extensions)
            .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
            .build()

        val document = parser.parse(text)
        val styles = mutableListOf<MarkdownStyle>()

        document.accept(object : AbstractVisitor() {
            override fun visit(emphasis: Emphasis) {
                val span = emphasis.sourceSpans.first()
                styles.add(MarkdownStyle(
                    range = span.inputIndex..span.inputIndex + span.length - 1,
                    spanStyle = ITALIC_STYLE
                ))
                visitChildren(emphasis)
            }

            override fun visit(strongEmphasis: StrongEmphasis) {
                val span = strongEmphasis.sourceSpans.first()
                styles.add(MarkdownStyle(
                    range = span.inputIndex..span.inputIndex + span.length - 1,
                    spanStyle = BOLD_STYLE
                ))
                visitChildren(strongEmphasis)
            }

            override fun visit(code: Code) {
                val span = code.sourceSpans.first()
                styles.add(MarkdownStyle(
                    range = span.inputIndex..span.inputIndex + span.length - 1,
                    spanStyle = CODE_STYLE
                ))
            }

            override fun visit(heading: Heading) {
                val span = heading.sourceSpans.first()
                styles.add(MarkdownStyle(
                    range = span.inputIndex..span.inputIndex + span.length - 1,
                    spanStyle = HEADER_STYLES[heading.level - 1],
                    paragraphStyle = HEADER_LINE_STYLES[heading.level - 1]
                ))
                visitChildren(heading)
            }
        })

        return styles
    }

    // ... 其他辅助方法保持不变 ...

    companion object {

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

