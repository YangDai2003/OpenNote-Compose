package com.yangdai.opennote.presentation.util.extension.highlight

import org.commonmark.Extension
import org.commonmark.parser.Parser
import org.commonmark.renderer.NodeRenderer
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext
import org.commonmark.renderer.markdown.MarkdownNodeRendererFactory
import org.commonmark.renderer.markdown.MarkdownRenderer
import org.commonmark.renderer.text.TextContentRenderer

class HighlightExtension : Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension,
    TextContentRenderer.TextContentRendererExtension, MarkdownRenderer.MarkdownRendererExtension {

    companion object {
        @JvmStatic
        fun create(): Extension {
            return HighlightExtension()
        }
    }

    override fun extend(parserBuilder: Parser.Builder) {
        parserBuilder.customDelimiterProcessor(HighlightDelimiterProcessor())
    }

    override fun extend(rendererBuilder: HtmlRenderer.Builder) {
        rendererBuilder.nodeRendererFactory { context ->
            HighlightHtmlNodeRenderer(context)
        }
    }

    override fun extend(rendererBuilder: TextContentRenderer.Builder) {
        rendererBuilder.nodeRendererFactory { context ->
            HighlightTextContentNodeRenderer(context)
        }
    }

    override fun extend(rendererBuilder: MarkdownRenderer.Builder) {
        rendererBuilder.nodeRendererFactory(object : MarkdownNodeRendererFactory {
            override fun create(context: MarkdownNodeRendererContext): NodeRenderer {
                return HighlightMarkdownNodeRenderer(context)
            }

            override fun getSpecialCharacters(): Set<Char> {
                return setOf('=')
            }
        })
    }
}