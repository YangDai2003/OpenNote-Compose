package com.yangdai.opennote.presentation.util.extension.highlight

import org.commonmark.node.Node
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext
import org.commonmark.renderer.markdown.MarkdownWriter

class HighlightMarkdownNodeRenderer(
    private val context: MarkdownNodeRendererContext,
    private val writer: MarkdownWriter = context.writer
) : HighlightNodeRenderer() {
    override fun render(node: Node?) {
        val highlight = node as Highlight
        writer.raw(highlight.openingDelimiter)
        var children = node.firstChild
        while (children != null) {
            val next = children.next
            context.render(children)
            children = next
        }
        writer.raw(highlight.closingDelimiter)
    }
}