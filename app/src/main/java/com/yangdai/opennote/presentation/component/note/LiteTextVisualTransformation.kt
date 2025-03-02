package com.yangdai.opennote.presentation.component.note

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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.yangdai.opennote.presentation.theme.linkColor
import com.yangdai.opennote.presentation.util.highlight.Highlight
import com.yangdai.opennote.presentation.util.highlight.HighlightExtension
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

class LiteTextVisualTransformation(
    private val readMode: Boolean, private val searchWord: String, private val selection: TextRange
) : VisualTransformation {

    private data class StyleRanges(
        val codeRanges: List<IntRange>,
        val boldRanges: List<IntRange>,
        val italicRanges: List<IntRange>,
        val boldItalicRanges: List<IntRange>,
        val strikethroughRanges: List<IntRange>,
        val underlineRanges: List<IntRange>,
        val highlightRanges: List<IntRange>,
        val headerRanges: List<Pair<IntRange, Int>>,
        val searchWordRanges: List<IntRange>,
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
                emptyList(), emptyList(), emptyList(),
                emptyList()
            )
        }
    }

    private val parser =
        Parser.builder().extensions(
            listOf(
                StrikethroughExtension.create(),
                InsExtension.create(),
                HighlightExtension.create(),
                TablesExtension.create()
            )
        ).includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES).build()

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
            text = annotatedString, offsetMapping = OffsetMapping.Identity
        )
    }

    private fun findAllRanges(text: AnnotatedString): StyleRanges {
        if (text.isEmpty()) return StyleRanges.EMPTY
        val textString = text.text

        val document = parser.parse(textString)

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
                            textString.substring(span.inputIndex, span.inputIndex + span.length)

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
                                textString.substring(span.inputIndex, span.inputIndex + span.length)

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

                    val content = fencedCodeBlock.literal ?: ""
                    val fence =
                        openingFence + openingMarkerLength + infoString + content.length + 1 // +1 for \n after info string
                    codeBlockContentRanges.add((openingFence + openingMarkerLength + infoString) until fence) // content

                    val closingMarkerLength = fencedCodeBlock.closingFenceLength ?: return
                    if (fence + closingMarkerLength <= textString.length) {
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

        val searchWordRanges = findSearchWordRanges(textString)

        return StyleRanges(
            codeRanges,
            boldRanges,
            italicRanges,
            boldItalicRanges,
            strikethroughRanges,
            underlineRanges,
            highlightRanges,
            headerRanges,
            searchWordRanges,
            markerRanges,
            linkRanges,
            fencedCodeBlockInfoRanges,
            codeBlockContentRanges
        )
    }

    private fun AnnotatedString.Builder.applyTextStyles(ranges: StyleRanges) {
        ranges.codeRanges.forEach { range -> addStyle(CODE_STYLE, range.first, range.last + 1) }
        ranges.boldItalicRanges.forEach { range ->
            addStyle(BOLD_ITALIC_STYLE, range.first, range.last + 1)
        }
        ranges.boldRanges.forEach { range -> addStyle(BOLD_STYLE, range.first, range.last + 1) }
        ranges.italicRanges.forEach { range -> addStyle(ITALIC_STYLE, range.first, range.last + 1) }
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

        // Add styling for list markers
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

    companion object {
        private val REGEX_TASK_LIST_ITEM = "^\\[([xX\\s])]\\s+(.*)".toRegex()
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
        private val BOLD_ITALIC_STYLE = SpanStyle(
            fontWeight = FontWeight.ExtraBold,
            fontStyle = FontStyle.Italic,
            fontSynthesis = FontSynthesis.All
        )
        private val STRIKETHROUGH_STYLE = SpanStyle(textDecoration = TextDecoration.LineThrough)
        private val UNDERLINE_STYLE = SpanStyle(textDecoration = TextDecoration.Underline)
        private val STRIKETHROUGH_AND_UNDERLINE_STYLE =
            SpanStyle(textDecoration = TextDecoration.LineThrough + TextDecoration.Underline)
        private val HIGHLIGHT_STYLE = SpanStyle(
            color = Color.Black,
            background = Color.Yellow.copy(alpha = 1f)
        )
        private val CODE_STYLE = SpanStyle(
            fontFamily = FontFamily.Monospace, background = Color.LightGray.copy(alpha = 0.3f)
        )
        private val CODE_BLOCK_STYLE = SpanStyle(fontFamily = FontFamily.Monospace)
        private val SEARCH_WORD_STYLE = SpanStyle(background = Color.Cyan.copy(alpha = 0.5f))
        private val MARKER_STYLE = SpanStyle(color = Color(0xFFCE8D6E))
        private val KEYWORD_STYLE = SpanStyle(color = Color(0xFFC67CBA))
        private val LINK_STYLE =
            SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)

        private val HEADER_LINE_STYLES = listOf(
            ParagraphStyle(
                lineHeight = 2.em,
                platformStyle = PlatformParagraphStyle(includeFontPadding = true)
            ), ParagraphStyle(
                lineHeight = 1.5.em,
                platformStyle = PlatformParagraphStyle(includeFontPadding = true)
            ), ParagraphStyle(
                lineHeight = 1.15.em,
                platformStyle = PlatformParagraphStyle(includeFontPadding = true)
            ), ParagraphStyle(
                lineHeight = 1.em,
                platformStyle = PlatformParagraphStyle(includeFontPadding = true)
            ), ParagraphStyle(
                lineHeight = 0.83.em,
                platformStyle = PlatformParagraphStyle(includeFontPadding = true)
            ), ParagraphStyle(
                lineHeight = 0.67.em,
                platformStyle = PlatformParagraphStyle(includeFontPadding = true)
            )
        )

        private val HEADER_STYLES = listOf(
            SpanStyle(
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                fontSynthesis = FontSynthesis.Weight
            ), SpanStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                fontSynthesis = FontSynthesis.Weight
            ), SpanStyle(
                fontSize = 22.sp, fontWeight = FontWeight.Bold, fontSynthesis = FontSynthesis.Weight
            ), SpanStyle(
                fontSize = 18.sp, fontWeight = FontWeight.Bold, fontSynthesis = FontSynthesis.Weight
            ), SpanStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontSynthesis = FontSynthesis.Weight
            ), SpanStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontSynthesis = FontSynthesis.Weight
            )
        )
    }
}
