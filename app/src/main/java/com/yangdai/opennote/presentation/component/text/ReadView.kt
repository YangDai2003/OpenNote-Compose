@file:Suppress("DEPRECATION", "unused")

package com.yangdai.opennote.presentation.component.text

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.yangdai.opennote.presentation.component.image.FullscreenImageDialog
import com.yangdai.opennote.presentation.state.SettingsState
import com.yangdai.opennote.presentation.theme.linkColor
import com.yangdai.opennote.presentation.util.Constants
import com.yangdai.opennote.presentation.util.rememberCustomTabsIntent
import com.yangdai.opennote.presentation.util.toHexColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MarkdownStyles(
    val hexTextColor: String,
    val hexCodeBackgroundColor: String,
    val hexPreBackgroundColor: String,
    val hexQuoteBackgroundColor: String,
    val hexLinkColor: String,
    val hexBorderColor: String
) {
    companion object {
        fun fromColorScheme(colorScheme: ColorScheme) = MarkdownStyles(
            hexTextColor = colorScheme.onSurface.toArgb().toHexColor(),
            hexCodeBackgroundColor = colorScheme.surfaceVariant.toArgb().toHexColor(),
            hexPreBackgroundColor = colorScheme.surfaceColorAtElevation(1.dp).toArgb().toHexColor(),
            hexQuoteBackgroundColor = colorScheme.secondaryContainer.toArgb().toHexColor(),
            hexLinkColor = linkColor.toArgb().toHexColor(),
            hexBorderColor = colorScheme.outline.toArgb().toHexColor()
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ReadView(
    modifier: Modifier = Modifier,
    html: String,
    rootUri: Uri,
    noteName: String,
    printEnabled: MutableState<Boolean>,
    scrollSynchronized: Boolean,
    scrollState: ScrollState = rememberScrollState(),
    settingsState: SettingsState
) {

    val colorScheme = MaterialTheme.colorScheme
    val markdownStyles = remember(colorScheme) {
        MarkdownStyles.fromColorScheme(colorScheme)
    }
    val codeTheme = remember(settingsState.isAppInDarkMode) {
        if (settingsState.isAppInDarkMode) {
            "https://cdn.jsdelivr.net/npm/prism-themes@1.9.0/themes/prism-material-dark.css"
        } else {
            "https://cdn.jsdelivr.net/npm/prism-themes@1.9.0/themes/prism-material-light.css"
        }
    }

    val data by remember(html, markdownStyles, codeTheme) {
        mutableStateOf(
            """
                    <!DOCTYPE html>
                    <html>
                    <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <script>
                        function handleImageClick(src) {
                            window.imageInterface.onImageClick(src);
                        }

                        function setupImageHandlers() {
                            document.querySelectorAll('img').forEach((img, index) => {
                                const imageName = img.getAttribute('src');
                                const id = 'img_' + index;
                                img.setAttribute('data-id', id);
                                window.imagePathHandler.processImage(imageName, id);
                                
                                let touchTimeout;
                                let touchStartTime;
                                
                                img.onclick = function() {
                                    handleImageClick(this.src);
                                };
                                
                                img.oncontextmenu = function(e) {
                                    return false;
                                };
                                
                                img.addEventListener('touchstart', function(e) {
                                    touchStartTime = Date.now();
                                });
                                
                                img.addEventListener('touchend', function(e) {
                                    // 如果触摸时间小于500ms,认为是点击操作
                                    if (Date.now() - touchStartTime < 500) {
                                        return; // 允许点击事件继续传播
                                    }
                                    e.preventDefault(); // 阻止长按操作
                                });
                                
                                // 禁用拖拽
                                img.draggable = false;
                            });
                        }
                        document.addEventListener('DOMContentLoaded', setupImageHandlers);
                    </script>
                    <script>
                        MathJax = {
                            tex: {
                                inlineMath: [['$', '$'], ['\\(', '\\)']]
                            }
                        };
                    </script>
                    <script type="text/javascript" id="MathJax-script" async
                        src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js">
                    </script>
                    <script type="module">
                      import mermaid from 'https://cdn.jsdelivr.net/npm/mermaid@11/dist/mermaid.esm.min.mjs';
                      mermaid.initialize({ startOnLoad: true });
                    </script>
                    <link href="$codeTheme" rel="stylesheet" />
                    <script src="https://cdn.jsdelivr.net/npm/prismjs@1.29.0/components/prism-core.min.js"></script>
                    <script src="https://cdn.jsdelivr.net/npm/prismjs@1.29.0/plugins/autoloader/prism-autoloader.min.js"></script>
                    <style type="text/css">
                        body { color: ${markdownStyles.hexTextColor}; padding: 0px; margin: 0px; }
                        img { max-width: 100%; height: auto; }
                        img {
                            -webkit-touch-callout: none; /* 禁用长按呼出菜单 */
                            pointer-events: auto !important; /* 确保点击事件可以工作 */
                        }
                        a { color: ${markdownStyles.hexLinkColor}; }
                        p code { background-color: ${markdownStyles.hexCodeBackgroundColor}; padding: 4px 4px 2px 4px; margin: 4px; border-radius: 4px; font-family: monospace; }
                        td code { background-color: ${markdownStyles.hexCodeBackgroundColor}; padding: 4px 4px 2px 4px; margin: 4px; border-radius: 4px; font-family: monospace; }
                        pre { background-color: ${markdownStyles.hexPreBackgroundColor}; display: block; padding: 16px; overflow-x: auto; }
                        blockquote { border-left: 4px solid ${markdownStyles.hexQuoteBackgroundColor}; padding-left: 0px; margin-left: 0px; padding-right: 0px; margin-right: 0px; }
                        blockquote > * { margin-left: 16px; padding: 0px; }
                        blockquote blockquote { margin: 16px; }
                        table { border-collapse: collapse; display: block; white-space: nowrap; overflow-x: auto; margin-right: 1px; }
                        th, td { border: 1px solid ${markdownStyles.hexBorderColor}; padding: 6px 13px; line-height: 1.5; }
                        tr:nth-child(even) { background-color: ${markdownStyles.hexPreBackgroundColor}; }
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
    val coroutineScope = rememberCoroutineScope()

    var webView by remember { mutableStateOf<WebView?>(null) }
    val imageCache = remember(rootUri) {
        mutableMapOf<String, String>()
    }
    var showDialog by remember { mutableStateOf(false) }
    var clickedImageUrl by remember { mutableStateOf("") }

    LaunchedEffect(scrollState.value) {
        if (!scrollSynchronized) return@LaunchedEffect

        val totalHeight = scrollState.maxValue
        val currentScrollPercent = when {
            totalHeight <= 0 -> 0f
            scrollState.value >= totalHeight -> 1f
            else -> (scrollState.value.toFloat() / totalHeight).coerceIn(0f, 1f)
        }

        webView?.evaluateJavascript(
            """
        (function() {
            const d = document.documentElement;
            const b = document.body;
            const maxHeight = Math.max(
                d.scrollHeight, d.offsetHeight, d.clientHeight,
                b.scrollHeight, b.offsetHeight
            );
            window.scrollTo({ 
                top: maxHeight * $currentScrollPercent, 
                behavior: 'auto' 
            });
        })();
        """.trimIndent(),
            null
        )
    }

    val activity = LocalActivity.current
    LaunchedEffect(printEnabled.value) {
        if (!printEnabled.value) return@LaunchedEffect
        webView?.let {
            createWebPrintJob(it, activity, noteName)
            printEnabled.value = false
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.surface
    AndroidView(
        modifier = modifier.fillMaxSize().clip(RectangleShape),
        factory = {
            WebView(it).also { webView = it }.apply {
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
                            customTabsIntent.launchUrl(it, url.toUri())
                        }
                        return true
                    }
                }
                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun onImageClick(urlStr: String) {
                            clickedImageUrl = urlStr
                            showDialog = true
                        }

                        @JavascriptInterface
                        fun getImageUri(imageName: String): String {
                            val imagesDir =
                                DocumentFile.fromTreeUri(context.applicationContext, rootUri)
                                    ?.findFile(Constants.File.OPENNOTE)
                                    ?.findFile(Constants.File.OPENNOTE_IMAGES)

                            val imageFile = imagesDir?.listFiles()?.find {
                                it.name == imageName
                            }
                            Toast.makeText(context, imageFile?.uri.toString(), Toast.LENGTH_LONG)
                                .show()
                            return imageFile?.uri?.toString() ?: ""
                        }
                    },
                    "imageInterface"
                )
                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun processImage(imageName: String, id: String) {
                            coroutineScope.launch(Dispatchers.IO) {
                                // 先检查缓存
                                if (imageCache.containsKey(imageName)) {
                                    withContext(Dispatchers.Main) {
                                        webView?.evaluateJavascript(
                                            """
                                            (function() {
                                                const img = document.querySelector('img[data-id="$id"]');
                                                if (img) {
                                                    img.src = '${imageCache[imageName]}';
                                                }
                                            })();
                                            """.trimIndent(),
                                            null
                                        )
                                    }
                                    return@launch
                                }

                                val imagesDir =
                                    DocumentFile.fromTreeUri(context.applicationContext, rootUri)
                                        ?.findFile(Constants.File.OPENNOTE)
                                        ?.findFile(Constants.File.OPENNOTE_IMAGES)

                                val imageFile = imagesDir?.listFiles()?.find {
                                    it.name == imageName
                                }

                                val imageUri = imageFile?.uri?.toString() ?: ""

                                // 更新缓存
                                if (imageUri.isNotEmpty()) {
                                    imageCache[imageName] = imageUri
                                }

                                withContext(Dispatchers.Main) {
                                    // 通过 JavaScript 更新图片 src
                                    webView?.evaluateJavascript(
                                        """
                                        (function() {
                                            const img = document.querySelector('img[data-id="$id"]');
                                            if (img && '${imageUri}' !== '') {
                                                img.src = '${imageUri}';
                                            }
                                        })();
                                        """.trimIndent(),
                                        null
                                    )
                                }
                            }
                        }
                    },
                    "imagePathHandler"
                )
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.allowFileAccessFromFileURLs = true
                settings.allowUniversalAccessFromFileURLs = true
                settings.domStorageEnabled = true
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
            }
        },
        update = {
            it.setBackgroundColor(backgroundColor.toArgb())
            it.loadDataWithBaseURL(
                null,
                data,
                "text/html",
                "UTF-8",
                null
            )
        },
        onReset = {
            imageCache.clear()
            webView = null
            it.stopLoading()
            it.destroy()
        })

    if (showDialog)
        FullscreenImageDialog(
            onDismiss = { showDialog = false },
            imageUrl = clickedImageUrl,
        )
}

private fun createWebPrintJob(webView: WebView, activity: Activity?, name: String) {

    // Get a PrintManager instance
    (activity?.getSystemService(Context.PRINT_SERVICE) as? PrintManager)?.let { printManager ->

        val jobName = "$name Document"

        // Get a print adapter instance
        val printAdapter = webView.createPrintDocumentAdapter(jobName)

        // Create a print job with name and adapter instance
        printManager.print(
            jobName,
            printAdapter,
            PrintAttributes.Builder().build()
        )
    }
}
