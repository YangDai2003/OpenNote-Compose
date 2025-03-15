package com.yangdai.opennote.presentation.util.extension.highlight

import org.commonmark.node.Node
import org.commonmark.renderer.text.TextContentNodeRendererContext

class HighlightTextContentNodeRenderer(private val context: TextContentNodeRendererContext) :
    HighlightNodeRenderer() {
    override fun render(node: Node) {
        var children = node.firstChild
        while (children != null) {
            val next = children.next
            context.render(children)
            children = next
        }
    }
}