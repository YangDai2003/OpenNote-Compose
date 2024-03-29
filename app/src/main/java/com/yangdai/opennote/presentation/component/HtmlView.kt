package com.yangdai.opennote.presentation.component

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
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
    val hexTextColor = String.format("#%06X", 0xFFFFFF and textColor)
    val hexCodeBackgroundColor = String.format("#%06X", 0xFFFFFF and codeBackgroundColor)
    val hexPreCodeBackgroundColor = String.format("#%06X", 0xFFFFFF and preCodeBackgroundColor)

    val data = """
        <!DOCTYPE html>
        <html>
        <head>
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.10/dist/katex.min.css" integrity="sha384-wcIxkf4k558AjM3Yz3BBFQUbk/zgIYC2R0QpeeYb+TwlBVMrlgLqwRjRtGZiK7ww" crossorigin="anonymous">
        <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.10/dist/katex.min.js" integrity="sha384-hIoBPJpTUs74ddyc4bFZSM1TVlQDA60VBbJS0oA934VSz82sBx1X7kSx2ATBDIyd" crossorigin="anonymous"></script>
        <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.10/dist/contrib/auto-render.min.js" integrity="sha384-43gviWU0YVjaDtb/GhzOouOXtZMP/7XUzwPTstBeZFe/+rCMvRwr4yROQP43s0Xk" crossorigin="anonymous"></script>
        <script>
            document.addEventListener("DOMContentLoaded", function() {
                renderMathInElement(document.body, {
                    delimiters: [
                        {left: "${'$'}${'$'}", right: "${'$'}${'$'}", display: true},
                        {left: "${'$'}", right: "${'$'}", display: false},
                        {left: "\\(", right: "\\)", display: false},
                        {left: "\\begin{equation}", right: "\\end{equation}", display: true},
                        {left: "\\begin{align}", right: "\\end{align}", display: true},
                        {left: "\\begin{alignat}", right: "\\end{alignat}", display: true},
                        {left: "\\begin{gather}", right: "\\end{gather}", display: true},
                        {left: "\\begin{CD}", right: "\\end{CD}", display: true},
                        {left: "\\[", right: "\\]", display: true}
                    ],
                    throwOnError : false
                });
            });
        </script>
        <style type="text/css">
            body{color: $hexTextColor; padding: 0px; margin: 0px;}
            p > code { background-color: $hexCodeBackgroundColor; padding: 4px 4px 2px 4px; margin: 4px; border-radius: 4px; }
            pre { background-color: $hexPreCodeBackgroundColor; padding: 16px;}
        </style>
        </head>
        <body>
        $html
        </body>
        </html>
    """.trimIndent()

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = {
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
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
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
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
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
}