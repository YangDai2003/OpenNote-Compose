package com.yangdai.opennote.presentation.util.extension.highlight

import org.commonmark.node.Nodes
import org.commonmark.node.SourceSpans
import org.commonmark.parser.delimiter.DelimiterProcessor
import org.commonmark.parser.delimiter.DelimiterRun

class HighlightDelimiterProcessor : DelimiterProcessor {
    override fun getOpeningCharacter(): Char {
        return '='
    }

    override fun getClosingCharacter(): Char {
        return '='
    }

    override fun getMinLength(): Int {
        return 2
    }

    override fun process(
        openingRun: DelimiterRun,
        closingRun: DelimiterRun
    ): Int {
        if (openingRun.length() >= 2 && closingRun.length() >= 2) {
            val opener = openingRun.opener
            val highlight = Highlight()
            val sourceSpans = SourceSpans()
            sourceSpans.addAllFrom(openingRun.getOpeners(2))
            for (node in Nodes.between(opener, closingRun.closer)) {
                highlight.appendChild(node)
                sourceSpans.addAll(node.sourceSpans)
            }
            sourceSpans.addAllFrom(closingRun.getClosers(2))
            highlight.sourceSpans = sourceSpans.sourceSpans
            opener.insertAfter(highlight)
            return 2
        } else {
            return 0
        }
    }
}