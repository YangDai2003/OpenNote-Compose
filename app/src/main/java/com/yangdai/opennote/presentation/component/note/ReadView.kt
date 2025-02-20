@file:Suppress("DEPRECATION", "unused")

package com.yangdai.opennote.presentation.component.note

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebView.enableSlowWholeDocumentDraw
import android.webkit.WebViewClient
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.ScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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
    settingsState: SettingsState,
    launchShareIntent: MutableState<Boolean>
) {

    val colorScheme = MaterialTheme.colorScheme
    val backgroundColor = colorScheme.surface
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
                
                function setupVideoHandlers() {
                    document.querySelectorAll('video').forEach((video, index) => {
                        const videoName = video.getAttribute('src');
                        const id = 'video_' + index;
                        video.setAttribute('data-id', id);
                        window.videoPathHandler.processVideo(videoName, id);
                        
                        // 设置视频控件样式
                        video.style.width = '100%';
                        video.controls = true;
                        
                        // 禁用下载和全屏
                        video.controlsList = "nodownload nofullscreen";
                        
                        // 禁用右键菜单
                        video.oncontextmenu = function(e) {
                            e.preventDefault();
                            return false;
                        };
                    });
                }
                
                function setupAudioHandlers() {
                    document.querySelectorAll('audio').forEach((audio, index) => {
                        const audioName = audio.getAttribute('src');
                        const id = 'audio_' + index;
                        audio.setAttribute('data-id', id);
                        window.audioPathHandler.processAudio(audioName, id);
                        
                        // 设置音频控件样式
                        audio.style.width = '100%';
                        audio.controls = true;
                        
                        // 禁用下载
                        audio.controlsList = "nodownload";
                        
                        // 禁用右键菜单
                        audio.oncontextmenu = function(e) {
                            e.preventDefault();
                            return false;
                        };
                    });
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
                
                document.addEventListener('DOMContentLoaded', () => {
                    setupImageHandlers();
                    setupAudioHandlers();
                    setupVideoHandlers();
                });
            </script>
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.21/dist/katex.min.css" integrity="sha384-zh0CIslj+VczCZtlzBcjt5ppRcsAmDnRem7ESsYwWwg3m/OaJ2l4x7YBZl9Kxxib" crossorigin="anonymous">
            <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.21/dist/katex.min.js" integrity="sha384-Rma6DA2IPUwhNxmrB/7S3Tno0YY7sFu9WSYMCuulLhIqYSGZ2gKCJWIqhBWqMQfh" crossorigin="anonymous"></script>
            <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.21/dist/contrib/auto-render.min.js" integrity="sha384-hCXGrW6PitJEwbkoStFjeJxv+fSOOQKOPbJxSfM6G5sWZjAyWhXiTIIAmQqnlLlh" crossorigin="anonymous"
                 onload="renderMathInElement(document.body);"></script>
            <script>
                document.addEventListener("DOMContentLoaded", function() {
                    renderMathInElement(document.body, {
                      // customised options
                      // • auto-render specific keys, e.g.:
                      delimiters: [
                          {left: '$$', right: '$$', display: true},
                          {left: '$', right: '$', display: false},
                          {left: '\\(', right: '\\)', display: false},
                          {left: '\\[', right: '\\]', display: true}
                      ],
                      // • rendering keys, e.g.:
                      throwOnError : false
                    });
                });
            </script>
            <script type="module">
                import mermaid from 'https://cdn.jsdelivr.net/npm/mermaid@11/dist/mermaid.esm.min.mjs';
                mermaid.initialize({ startOnLoad: true });
            </script>
            <script>
                document.addEventListener('DOMContentLoaded', () => {
                    document.querySelectorAll('li').forEach(li => {
                        if (li.querySelector('input[type="checkbox"]')) {
                            li.style.listStyleType = 'none';
                        }
                    });
                });
            </script>
            <link href="$codeTheme" rel="stylesheet" />
            <script src="https://cdn.jsdelivr.net/npm/prismjs@1.29.0/components/prism-core.min.js"></script>
            <script src="https://cdn.jsdelivr.net/npm/prismjs@1.29.0/plugins/autoloader/prism-autoloader.min.js"></script>
            <style type="text/css">
                body { color: ${markdownStyles.hexTextColor}; padding-left: 16px; padding-right: 16px; padding-top: 0; padding-bottom: 0; margin: 0; }
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
                video::-webkit-media-controls-fullscreen-button {
                    display: none !important;
                }
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
        }
        printEnabled.value = false
    }
    val context = LocalContext.current
    LaunchedEffect(launchShareIntent.value) {
        if (!launchShareIntent.value) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            webView?.let {
                try {
                    shareBitmap(context, webView!!, noteName)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        launchShareIntent.value = false
    }

    AndroidView(
        modifier = modifier.clip(RectangleShape),
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
                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun processAudio(audioName: String, id: String) {
                            coroutineScope.launch(Dispatchers.IO) {
                                val audioDir =
                                    DocumentFile.fromTreeUri(context.applicationContext, rootUri)
                                        ?.findFile(Constants.File.OPENNOTE)
                                        ?.findFile(Constants.File.OPENNOTE_AUDIO)

                                val audioFile = audioDir?.listFiles()?.find {
                                    it.name == audioName
                                }

                                val audioUri = audioFile?.uri?.toString() ?: ""

                                withContext(Dispatchers.Main) {
                                    // 通过 JavaScript 更新音频 src
                                    webView?.evaluateJavascript(
                                        """
                                    (function() {
                                        const audio = document.querySelector('audio[data-id="$id"]');
                                        if (audio && '${audioUri}' !== '') {
                                            audio.src = '${audioUri}';
                                        }
                                    })();
                                    """.trimIndent(),
                                        null
                                    )
                                }
                            }
                        }
                    },
                    "audioPathHandler"
                )
                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun processVideo(videoName: String, id: String) {
                            coroutineScope.launch(Dispatchers.IO) {
                                val videoDir =
                                    DocumentFile.fromTreeUri(context.applicationContext, rootUri)
                                        ?.findFile(Constants.File.OPENNOTE)
                                        ?.findFile(Constants.File.OPENNOTE_VIDEOS)

                                val videoFile = videoDir?.listFiles()?.find {
                                    it.name == videoName
                                }

                                val videoUri = videoFile?.uri?.toString() ?: ""
                                // 生成缩略图
                                val thumbnail = if (videoUri.isNotEmpty()) {
                                    withContext(Dispatchers.IO) {
                                        val retriever = MediaMetadataRetriever()
                                        try {
                                            retriever.setDataSource(
                                                context.applicationContext,
                                                videoUri.toUri()
                                            )
                                            val bitmap = retriever.getFrameAtTime(0)
                                            val base64 = bitmapToBase64(bitmap)
                                            "data:image/jpeg;base64,$base64"
                                        } catch (e: Exception) {
                                            ""
                                        } finally {
                                            retriever.release()
                                        }
                                    }
                                } else ""

                                withContext(Dispatchers.Main) {
                                    webView?.evaluateJavascript(
                                        """
                                        (function() {
                                            const video = document.querySelector('video[data-id="$id"]');
                                            if (video && '${videoUri}' !== '') {
                                                video.src = '${videoUri}';
                                                video.poster = '${thumbnail}';
                                            }
                                        })();
                                        """.trimIndent(),
                                        null
                                    )
                                }
                            }
                        }
                    },
                    "videoPathHandler"
                )
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.allowFileAccessFromFileURLs = true
                settings.allowUniversalAccessFromFileURLs = true
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
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
                enableSlowWholeDocumentDraw()
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

private fun shareBitmap(context: Context, webView: WebView, noteName: String) {

    // 创建临时文件
    val file = File(context.cacheDir, "${noteName}_${System.currentTimeMillis()}_preview.jpg")

    val bitmap = convertHtmlToBitmap(webView)
    if (bitmap == null) return
    val canvas = Canvas(bitmap)
    webView.draw(canvas)

    // 保存位图到文件
    FileOutputStream(file).use { stream ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        bitmap.recycle()
    }

    // 创建FileProvider的URI
    val imageUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    // 创建分享Intent
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, imageUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooserIntent = Intent.createChooser(shareIntent, null)
    context.startActivity(chooserIntent)
}

private fun convertHtmlToBitmap(webView: WebView): Bitmap? {
    webView.measure(
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    //layout of webview
    webView.layout(0, 0, webView.measuredWidth, webView.measuredHeight)

    webView.isDrawingCacheEnabled = true
    webView.buildDrawingCache()
    //create Bitmap if measured height and width >0
    val b = if (webView.measuredWidth > 0 && webView.measuredHeight > 0)
        createBitmap(webView.measuredWidth, webView.measuredHeight)
    else null
    // Draw bitmap on canvas
    b?.let {
        Canvas(b).apply {
            drawBitmap(it, 0f, b.height.toFloat(), Paint())
            webView.draw(this)
        }
    }
    return b
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

@OptIn(ExperimentalEncodingApi::class)
private fun bitmapToBase64(bitmap: Bitmap?): String {
    if (bitmap == null) return ""
    return ByteArrayOutputStream().use { outputStream ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        Base64.encode(outputStream.toByteArray())
    }
}