package com.yangdai.opennote.presentation.util.extension.highlight

import org.commonmark.node.CustomNode
import org.commonmark.node.Delimited

class Highlight : CustomNode(), Delimited {

    private val delimiter: String = "=="

    override fun getOpeningDelimiter(): String {
        return delimiter
    }

    override fun getClosingDelimiter(): String {
        return delimiter
    }
}