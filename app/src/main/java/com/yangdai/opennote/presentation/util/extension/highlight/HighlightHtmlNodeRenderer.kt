package com.yangdai.opennote.presentation.util.extension.highlight

import org.commonmark.node.Node
import org.commonmark.renderer.html.HtmlNodeRendererContext
import org.commonmark.renderer.html.HtmlWriter

class HighlightHtmlNodeRenderer(
    private val context: HtmlNodeRendererContext,
    private val writer: HtmlWriter = context.writer
) : HighlightNodeRenderer() {

    override fun render(node: Node) {
        val attributes = context.extendAttributes(node, "mark", emptyMap())
        writer.tag("mark", attributes)
        var children = node.firstChild
        while (children != null) {
            val next = children.next
            context.render(children)
            children = next
        }
        writer.tag("/mark")
    }
}