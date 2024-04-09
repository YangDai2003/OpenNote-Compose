package com.yangdai.opennote.presentation.component

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HtmlView(html: String) {
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val codeBackgroundColor = MaterialTheme.colorScheme.surfaceVariant.toArgb()
    val preCodeBackgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).toArgb()
    val quoteBackgroundColor = MaterialTheme.colorScheme.surfaceDim.toArgb()
    val hexTextColor = String.format("#%06X", 0xFFFFFF and textColor)
    val hexCodeBackgroundColor = String.format("#%06X", 0xFFFFFF and codeBackgroundColor)
    val hexPreCodeBackgroundColor = String.format("#%06X", 0xFFFFFF and preCodeBackgroundColor)
    val hexQuoteBackgroundColor = String.format("#%06X", 0xFFFFFF and quoteBackgroundColor)

    val customTabsIntent = CustomTabsIntent.Builder()
        .setShowTitle(true)
        .build()

    val data = """
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
            body{color: $hexTextColor; padding: 0px; margin: 0px;}
            p code { background-color: $hexCodeBackgroundColor; padding: 4px 4px 2px 4px; margin: 4px; border-radius: 4px; }
            pre { background-color: $hexPreCodeBackgroundColor; display: block; padding: 16px; overflow-x: auto;}
            blockquote { border-left: 4px solid ${hexQuoteBackgroundColor}; padding-left: 0px; margin-left: 0px; padding-right: 0px; margin-right: 0px; }
            blockquote > * { margin-left: 16px; padding: 0px; }
            blockquote blockquote { margin: 16px; }
        </style>
        </head>
        <body>
        $html
        </body>
        </html>
    """.trimIndent()

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
            settings.defaultTextEncodingName = "utf-8"
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.useWideViewPort = false
            settings.loadWithOverviewMode = false
            setPadding(0, 0, 0, 0)
            setBackgroundColor(Color.TRANSPARENT)
            loadDataWithBaseURL(
                null,
                data,
                "text/html",
                "UTF-8",
                null
            )
        }
    }, update = {
        it.loadDataWithBaseURL(
            null,
            data,
            "text/html",
            "UTF-8",
            null
        )
    })
}
