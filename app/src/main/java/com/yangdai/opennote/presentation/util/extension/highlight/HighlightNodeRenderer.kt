package com.yangdai.opennote.presentation.util.extension.highlight

import org.commonmark.node.Node
import org.commonmark.renderer.NodeRenderer

abstract class HighlightNodeRenderer : NodeRenderer {

    override fun getNodeTypes(): Set<Class<out Node?>?>? {
        return setOf(Highlight::class.java)
    }
}