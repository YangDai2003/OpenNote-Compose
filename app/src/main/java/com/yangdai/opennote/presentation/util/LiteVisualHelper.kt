package com.yangdai.opennote.presentation.util

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
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.yangdai.opennote.presentation.theme.linkColor
import com.yangdai.opennote.presentation.util.extension.highlight.Highlight
import com.yangdai.opennote.presentation.util.extension.highlight.HighlightExtension
import com.yangdai.opennote.presentation.util.extension.properties.Properties
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.ins.Ins
import org.commonmark.ext.ins.InsExtension
import org.commonmark.node.AbstractVisitor
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.CustomNode
import org.commonmark.node.Emphasis
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.Heading
import org.commonmark.node.Image
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Link
import org.commonmark.node.ListItem
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text
import org.commonmark.parser.IncludeSourceSpans
import org.commonmark.parser.Parser

val PARSER: Parser =
    Parser.builder().extensions(
        listOf(
            StrikethroughExtension.create(),
            InsExtension.create(),
            HighlightExtension.create(),
            TablesExtension.create()
        )
    ).includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES).build()

val REGEX_TASK_LIST_ITEM = "^\\[([xX\\s])]\\s+(.*)".toRegex()
val PROPERTIES_STYLE = SpanStyle(
    fontWeight = FontWeight.Light,
    fontStyle = FontStyle.Normal,
    fontSynthesis = FontSynthesis.None,
    color = Color.Gray,
    textDecoration = TextDecoration.None,
    fontFamily = FontFamily.Default,
    background = Color.Transparent
)

val SYMBOL_STYLE = SpanStyle(
    fontWeight = FontWeight.Light,
    fontStyle = FontStyle.Normal,
    fontSize = 0.sp,
    color = Color.Gray,
    textDecoration = TextDecoration.None,
    fontFamily = FontFamily.Default,
    background = Color.Transparent
)

val BOLD_STYLE = SpanStyle(
    fontWeight = FontWeight.Bold,
    fontSynthesis = FontSynthesis.Weight
)

val ITALIC_STYLE = SpanStyle(
    fontStyle = FontStyle.Italic,
    fontSynthesis = FontSynthesis.Style
)

val BOLD_ITALIC_STYLE = SpanStyle(
    fontWeight = FontWeight.Bold,
    fontStyle = FontStyle.Italic,
    fontSynthesis = FontSynthesis.All
)

val STRIKETHROUGH_STYLE = SpanStyle(
    textDecoration = TextDecoration.LineThrough
)

val UNDERLINE_STYLE = SpanStyle(
    textDecoration = TextDecoration.Underline
)

val STRIKETHROUGH_AND_UNDERLINE_STYLE = SpanStyle(
    textDecoration = TextDecoration.combine(
        listOf(
            TextDecoration.LineThrough,
            TextDecoration.Underline
        )
    )
)

val HIGHLIGHT_STYLE = SpanStyle(
    color = Color.Black,
    background = Color.Yellow.copy(alpha = 1f)
)

val CODE_STYLE = SpanStyle(
    fontFamily = FontFamily.Monospace,
    background = Color.LightGray.copy(alpha = 0.3f)
)

val CODE_BLOCK_STYLE = SpanStyle(fontFamily = FontFamily.Monospace)
val SEARCH_WORD_STYLE = SpanStyle(background = Color.Cyan.copy(alpha = 0.5f))
val CURRENT_SEARCH_WORD_STYLE = SpanStyle(background = Color.Green.copy(alpha = 0.5f))
val MARKER_STYLE = SpanStyle(color = Color(0xFFCE8D6E))
val KEYWORD_STYLE = SpanStyle(color = Color(0xFFC67CBA))
val LINK_STYLE = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)

val HEADER_LINE_STYLES = listOf(
    ParagraphStyle(
        lineHeight = 2.em,
        platformStyle = PlatformParagraphStyle.Default,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    ), ParagraphStyle(
        lineHeight = 1.5.em,
        platformStyle = PlatformParagraphStyle.Default,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    ), ParagraphStyle(
        lineHeight = 1.17.em,
        platformStyle = PlatformParagraphStyle.Default,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    ), ParagraphStyle(
        lineHeight = 1.em,
        platformStyle = PlatformParagraphStyle.Default,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    ), ParagraphStyle(
        lineHeight = 0.83.em,
        platformStyle = PlatformParagraphStyle.Default,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    ), ParagraphStyle(
        lineHeight = 0.75.em,
        platformStyle = PlatformParagraphStyle.Default,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    )
)

val HEADER_STYLES = listOf(
    SpanStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Black,
        fontSynthesis = FontSynthesis.Weight
    ), SpanStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.ExtraBold,
        fontSynthesis = FontSynthesis.Weight
    ), SpanStyle(
        fontSize = 18.72.sp,
        fontWeight = FontWeight.ExtraBold,
        fontSynthesis = FontSynthesis.Weight
    ), SpanStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        fontSynthesis = FontSynthesis.Weight
    ), SpanStyle(
        fontSize = 13.28.sp,
        fontWeight = FontWeight.Bold,
        fontSynthesis = FontSynthesis.Weight
    ), SpanStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        fontSynthesis = FontSynthesis.Weight
    )
)

data class StyleRanges(
    val codeRanges: List<IntRange>,
    val boldRanges: List<IntRange>,
    val italicRanges: List<IntRange>,
    val boldItalicRanges: List<IntRange>,
    val strikethroughRanges: List<IntRange>,
    val underlineRanges: List<IntRange>,
    val highlightRanges: List<IntRange>,
    val headerRanges: List<Pair<IntRange, Int>>,
    val markerRanges: List<IntRange>,
    val linkRanges: List<IntRange>,
    val fencedCodeBlockInfoRanges: List<IntRange>,
    val codeBlockContentRanges: List<IntRange>
) {
    companion object {
        val EMPTY = StyleRanges(
            emptyList(), emptyList(), emptyList(),
            emptyList(), emptyList(), emptyList(),
            emptyList(), emptyList(), emptyList(),
            emptyList(), emptyList(), emptyList()
        )
    }
}

fun findTagRanges(text: String): StyleRanges {
    if (text.isEmpty()) return StyleRanges.EMPTY
    val document = PARSER.parse(text)
    val codeRanges = mutableListOf<IntRange>()
    val boldRanges = mutableListOf<IntRange>()
    val italicRanges = mutableListOf<IntRange>()
    val boldItalicRanges = mutableListOf<IntRange>()
    val strikethroughRanges = mutableListOf<IntRange>()
    val underlineRanges = mutableListOf<IntRange>()
    val highlightRanges = mutableListOf<IntRange>()
    val headerRanges = mutableListOf<Pair<IntRange, Int>>()
    val markerRanges = mutableListOf<IntRange>()
    val linkRanges = mutableListOf<IntRange>()
    val fencedCodeBlockInfoRanges = mutableListOf<IntRange>()
    val codeBlockContentRanges = mutableListOf<IntRange>()

    // 遍历节点
    document.accept(object : AbstractVisitor() {
        override fun visit(code: Code) {
            val span = code.sourceSpans.first()
            codeRanges.add(span.inputIndex until (span.inputIndex + span.length))
        }

        override fun visit(emphasis: Emphasis) {
            val span = emphasis.sourceSpans.first()
            when {
                // 检查是否在Strong节点内或包含Strong节点
                emphasis.parent is StrongEmphasis || emphasis.firstChild is StrongEmphasis -> {
                    boldItalicRanges.add(span.inputIndex until (span.inputIndex + span.length))
                }

                else -> {
                    italicRanges.add(span.inputIndex until (span.inputIndex + span.length))
                }
            }
        }

        override fun visit(strong: StrongEmphasis) {
            val span = strong.sourceSpans.first()
            when {
                // 检查是否在Emphasis节点内或包含Emphasis节点
                strong.parent is Emphasis || strong.firstChild is Emphasis -> {
                    boldItalicRanges.add(span.inputIndex until (span.inputIndex + span.length))
                }

                else -> {
                    boldRanges.add(span.inputIndex until (span.inputIndex + span.length))
                }
            }
        }

        override fun visit(listItem: ListItem) {
            val child = listItem.firstChild
            val markerIndent = listItem.markerIndent ?: 0
            if (child is Paragraph) {
                val node = child.firstChild
                if (node is Text) {
                    val textNode = node
                    val span = listItem.sourceSpans.firstOrNull()
                    if (span != null) {
                        val literal = textNode.literal
                        val markerStart = span.inputIndex
                        markerRanges.add(markerStart until (markerStart + markerIndent + 1))
                        val matchResult = REGEX_TASK_LIST_ITEM.find(literal)
                        if (matchResult != null) {
                            val markerLength = 3 // Length of [x] or [ ]

                            // Add the task list marker range to markerRanges
                            markerRanges.add(markerStart + markerIndent + 2 until (markerStart + markerIndent + 2 + markerLength))
                        }
                    }
                }
            }
            visitChildren(listItem)
        }

        override fun visit(bulletList: BulletList) {
            var item = bulletList.firstChild
            while (item != null) {
                val span = item.sourceSpans.firstOrNull()
                if (span != null) {
                    // The marker is at the beginning of the list item,  -, * or +
                    val marker = bulletList.marker ?: "*"
                    val markerLength = marker.length
                    // Add the bullet marker range
                    markerRanges.add(span.inputIndex until (span.inputIndex + markerLength))
                }
                item = item.next
            }
            visitChildren(bulletList)
        }

        override fun visit(orderedList: OrderedList) {
            var item = orderedList.firstChild

            while (item != null) {
                val span = item.sourceSpans.firstOrNull()
                if (span != null) {
                    // Extract the text from the source span
                    val itemText =
                        text.substring(
                            span.inputIndex,
                            span.inputIndex + span.length
                        )

                    // Find the delimiter in the text
                    val delimiter = orderedList.markerDelimiter ?: "."
                    val delimiterIndex = itemText.indexOf(delimiter)

                    if (delimiterIndex > 0) {
                        // Add range for just the marker part
                        markerRanges.add(span.inputIndex until (span.inputIndex + delimiterIndex + 1))
                    }
                }
                item = item.next
            }
            visitChildren(orderedList)
        }

        override fun visit(link: Link) {
            val span = link.sourceSpans.firstOrNull()
            if (span != null) {
                // The entire link including text and URL needs to be styled
                // Format is [text](url)
                linkRanges.add(span.inputIndex until (span.inputIndex + span.length))
            }
            visitChildren(link)
        }

        override fun visit(image: Image) {
            val span = image.sourceSpans.firstOrNull()
            if (span != null) {
                // The entire image including alt text and URL needs to be styled
                // Format is ![alt text](url)
                linkRanges.add(span.inputIndex until (span.inputIndex + span.length))
            }
            visitChildren(image)
        }

        override fun visit(customNode: CustomNode) {
            when (customNode) {
                is Strikethrough -> {
                    val span = customNode.sourceSpans.first()
                    strikethroughRanges.add(span.inputIndex until (span.inputIndex + span.length))
                }

                is Ins -> {
                    val span = customNode.sourceSpans.first()
                    underlineRanges.add(span.inputIndex until (span.inputIndex + span.length))
                }

                is Highlight -> {
                    val span = customNode.sourceSpans.first()
                    highlightRanges.add(span.inputIndex until (span.inputIndex + span.length))
                }

                is TableRow -> {
                    val span = customNode.sourceSpans.firstOrNull()
                    if (span != null) {
                        // Get the row's text
                        val rowText =
                            text.substring(
                                span.inputIndex,
                                span.inputIndex + span.length
                            )

                        // Find all | characters in the row
                        var charIndex = 0
                        while (charIndex < rowText.length) {
                            val pipeIndex = rowText.indexOf('|', charIndex)
                            if (pipeIndex == -1) break

                            // Add the | separator to marker ranges
                            markerRanges.add((span.inputIndex + pipeIndex) until (span.inputIndex + pipeIndex + 1))
                            charIndex = pipeIndex + 1
                        }
                    }
                }
            }
            visitChildren(customNode)
        }

        override fun visit(heading: Heading) {
            val span = heading.sourceSpans.first()
            if (span.inputIndex + span.length + 1 <= text.length) {
                val range = span.inputIndex until (span.inputIndex + span.length + 1)
                headerRanges.add(range to heading.level)
            }
            visitChildren(heading)
        }

        override fun visit(fencedCodeBlock: FencedCodeBlock) {
            val span = fencedCodeBlock.sourceSpans.firstOrNull()
            if (span != null) {
                // Get the opening fence marker (```language)
                val openingFence = span.inputIndex
                val infoString = fencedCodeBlock.info?.length ?: 0
                val openingMarkerLength = fencedCodeBlock.openingFenceLength ?: return

                markerRanges.add(openingFence until (openingFence + openingMarkerLength)) // ```
                fencedCodeBlockInfoRanges.add(openingFence + openingMarkerLength until (openingFence + openingMarkerLength + infoString)) // language

                val blockContent = fencedCodeBlock.literal ?: ""
                val fence =
                    openingFence + openingMarkerLength + infoString + blockContent.length + 1 // +1 for \n after info string
                codeBlockContentRanges.add((openingFence + openingMarkerLength + infoString) until fence) // content

                val closingMarkerLength = fencedCodeBlock.closingFenceLength ?: return
                if (fence + closingMarkerLength <= text.length) {
                    markerRanges.add(fence until (fence + closingMarkerLength))
                }
            }
        }

        override fun visit(indentedCodeBlock: IndentedCodeBlock) {
            val span = indentedCodeBlock.sourceSpans.firstOrNull()
            if (span != null) {
                val range = span.inputIndex until (span.inputIndex + span.length)
                codeBlockContentRanges.add(range)
            }
        }
    })

    return StyleRanges(
        codeRanges = codeRanges,
        boldRanges = boldRanges,
        italicRanges = italicRanges,
        boldItalicRanges = boldItalicRanges,
        strikethroughRanges = strikethroughRanges,
        underlineRanges = underlineRanges,
        highlightRanges = highlightRanges,
        headerRanges = headerRanges,
        markerRanges = markerRanges,
        linkRanges = linkRanges,
        fencedCodeBlockInfoRanges = fencedCodeBlockInfoRanges,
        codeBlockContentRanges = codeBlockContentRanges
    )
}

fun parseMarkdownContent(text: String): AnnotatedString {
    val textWithoutProperties = Properties.splitPropertiesAndContent(text).second
    val styleRanges = findTagRanges(textWithoutProperties)

    return buildAnnotatedString {
        styleRanges.apply {
            codeRanges.forEach { range ->
                addStyle(CODE_STYLE, range.first, range.last + 1)
                addStyle(SYMBOL_STYLE, range.first, range.first + 1)
                addStyle(SYMBOL_STYLE, range.last - 1 + 1, range.last + 1)
            }
            boldItalicRanges.forEach { range ->
                addStyle(BOLD_ITALIC_STYLE, range.first, range.last + 1)
                addStyle(SYMBOL_STYLE, range.first, range.first + 3)
                addStyle(SYMBOL_STYLE, range.last - 3 + 1, range.last + 1)
            }
            boldRanges.forEach { range ->
                addStyle(BOLD_STYLE, range.first, range.last + 1)
                addStyle(SYMBOL_STYLE, range.first, range.first + 2)
                addStyle(SYMBOL_STYLE, range.last - 2 + 1, range.last + 1)
            }
            italicRanges.forEach { range ->
                addStyle(ITALIC_STYLE, range.first, range.last + 1)
                addStyle(SYMBOL_STYLE, range.first, range.first + 1)
                addStyle(SYMBOL_STYLE, range.last - 1 + 1, range.last + 1)
            }
            highlightRanges.forEach { range ->
                addStyle(HIGHLIGHT_STYLE, range.first, range.last + 1)
                addStyle(SYMBOL_STYLE, range.first, range.first + 2)
                addStyle(SYMBOL_STYLE, range.last - 2 + 1, range.last + 1)
            }

            val combinedRanges = (strikethroughRanges + underlineRanges).distinct()
            combinedRanges.forEach { range ->
                val hasStrikethrough = strikethroughRanges.any { it.overlaps(range) }
                val hasUnderline = underlineRanges.any { it.overlaps(range) }
                val style = when {
                    hasStrikethrough && hasUnderline -> STRIKETHROUGH_AND_UNDERLINE_STYLE
                    hasStrikethrough -> STRIKETHROUGH_STYLE
                    hasUnderline -> UNDERLINE_STYLE
                    else -> return@forEach
                }
                addStyle(style, range.first, range.last + 1)
            }

            strikethroughRanges.forEach { range ->
                addStyle(SYMBOL_STYLE, range.first, range.first + 2)
                addStyle(SYMBOL_STYLE, range.last - 2 + 1, range.last + 1)
            }
            underlineRanges.forEach { range ->
                addStyle(SYMBOL_STYLE, range.first, range.first + 2)
                addStyle(SYMBOL_STYLE, range.last - 2 + 1, range.last + 1)
            }

            headerRanges.forEach { (range, level) ->
                addStyle(HEADER_STYLES[level - 1], range.first, range.last + 1)
                addStyle(HEADER_LINE_STYLES[level - 1], range.first, range.last + 1)
                addStyle(SYMBOL_STYLE, range.first, range.first + level + 1)
            }

            // Add styling for list markers
            markerRanges.forEach { range ->
                addStyle(MARKER_STYLE, range.first, range.last + 1)
            }
            linkRanges.forEach { range ->
                addStyle(LINK_STYLE, range.first, range.last + 1)
            }
            fencedCodeBlockInfoRanges.forEach { range ->
                addStyle(KEYWORD_STYLE, range.first, range.last + 1)
            }
            codeBlockContentRanges.forEach { range ->
                addStyle(CODE_BLOCK_STYLE, range.first, range.last + 1)
            }
        }
        append(textWithoutProperties)
    }
}