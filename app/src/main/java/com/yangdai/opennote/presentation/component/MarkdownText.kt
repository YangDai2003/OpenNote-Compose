package com.yangdai.opennote.presentation.component

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.yangdai.opennote.presentation.theme.linkColor
import com.yangdai.opennote.presentation.util.rememberCustomTabsIntent

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MarkdownText(
    html: String,
    colorScheme: ColorScheme = MaterialTheme.colorScheme
) {

    val hexTextColor = remember(colorScheme) {
        String.format("#%06X", 0xFFFFFF and colorScheme.onSurface.toArgb())
    }
    val hexCodeBackgroundColor = remember(colorScheme) {
        String.format("#%06X", 0xFFFFFF and colorScheme.surfaceVariant.toArgb())
    }
    val hexPreBackgroundColor = remember(colorScheme) {
        String.format("#%06X", 0xFFFFFF and colorScheme.surfaceColorAtElevation(1.dp).toArgb())
    }
    val hexQuoteBackgroundColor = remember(colorScheme) {
        String.format("#%06X", 0xFFFFFF and colorScheme.secondaryContainer.toArgb())
    }
    val hexLinkColor = remember(colorScheme) {
        String.format("#%06X", 0xFFFFFF and linkColor.toArgb())
    }
    val hexBorderColor = remember(colorScheme) {
        String.format("#%06X", 0xFFFFFF and colorScheme.outline.toArgb())
    }

    val data by remember(
        html,
        hexTextColor,
        hexCodeBackgroundColor,
        hexPreBackgroundColor,
        hexQuoteBackgroundColor,
        hexLinkColor,
        hexBorderColor
    ) {
        mutableStateOf(
            """
                    <!DOCTYPE html>
                    <html>
                    <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <script>
                        MathJax = {
                            tex: {
                                inlineMath: [['${'$'}', '${'$'}'], ['\\(', '\\)']]
                            }
                        };
                    </script>
                    <script type="text/javascript" id="MathJax-script" async
                        src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js">
                    </script>
                    <script type="module">
                        import mermaid from 'https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.esm.min.mjs';
                        mermaid.initialize({ startOnLoad: true });
                    </script>
                    <style type="text/css">
                        body { color: $hexTextColor; padding: 0px; margin: 0px; }
                        a { color: $hexLinkColor; }
                        p code { background-color: $hexCodeBackgroundColor; padding: 4px 4px 2px 4px; margin: 4px; border-radius: 4px; }
                        td code { background-color: $hexCodeBackgroundColor; padding: 4px 4px 2px 4px; margin: 4px; border-radius: 4px; }
                        pre { background-color: $hexPreBackgroundColor; display: block; white-space: nowrap; padding: 16px; overflow-x: auto; }
                        blockquote { border-left: 4px solid ${hexQuoteBackgroundColor}; padding-left: 0px; margin-left: 0px; padding-right: 0px; margin-right: 0px; }
                        blockquote > * { margin-left: 16px; padding: 0px; }
                        blockquote blockquote { margin: 16px; }
                        table { border-collapse: collapse; display: block; white-space: nowrap; overflow-x: auto; margin-right: 1px; }
                        th, td { border: 1px solid $hexBorderColor; padding: 6px 13px; line-height: 1.5; }
                        tr:nth-child(even) { background-color: $hexPreBackgroundColor; }
                    </style>
                    </head>
                    <body>
                    $html
                    </body>
                    </html>
                """.trimIndent()
        )
    }

    val customTabsIntent = rememberCustomTabsIntent()

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest
                    ): Boolean {
                        val url = request.url.toString()
                        if (url.startsWith("http://") || url.startsWith("https://")) {
                            customTabsIntent.launchUrl(it, Uri.parse(url))
                        }
                        return true
                    }
                }
                settings.javaScriptEnabled = true
                settings.loadsImagesAutomatically = true
                settings.defaultTextEncodingName = "UTF-8"
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.useWideViewPort = false
                settings.loadWithOverviewMode = false
                setBackgroundColor(Color.TRANSPARENT)
            }
        },
        update = {
            it.loadDataWithBaseURL(
                null,
                data,
                "text/html",
                "UTF-8",
                null
            )
        },
        onReset = {
            it.stopLoading()
            it.clearHistory()
        })
}
