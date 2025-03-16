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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.yangdai.opennote.presentation.component.image.FullscreenImageDialog
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
    val hexBorderColor: String,
    val backgroundColor: Int
) {
    companion object {
        fun fromColorScheme(colorScheme: ColorScheme) = MarkdownStyles(
            hexTextColor = colorScheme.onSurface.toArgb().toHexColor(),
            hexCodeBackgroundColor = colorScheme.surfaceVariant.toArgb().toHexColor(),
            hexPreBackgroundColor = colorScheme.surfaceColorAtElevation(1.dp).toArgb().toHexColor(),
            hexQuoteBackgroundColor = colorScheme.secondaryContainer.toArgb().toHexColor(),
            hexLinkColor = linkColor.toArgb().toHexColor(),
            hexBorderColor = colorScheme.outline.toArgb().toHexColor(),
            backgroundColor = colorScheme.surface.toArgb()
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
    scrollSynchronized: Boolean,
    scrollState: ScrollState,
    isAppInDarkMode: Boolean,
    printEnabled: MutableState<Boolean>,
    launchShareIntent: MutableState<Boolean>
) {

    val colorScheme = MaterialTheme.colorScheme
    val markdownStyles = remember(colorScheme) {
        MarkdownStyles.fromColorScheme(colorScheme)
    }
    val codeTheme = remember(isAppInDarkMode) {
        if (isAppInDarkMode) {
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
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <meta name="color-scheme" content="${if (isAppInDarkMode) "dark" else "light"}">
            
            <!-- Preconnect to CDN resources -->
            <link rel="preconnect" href="https://cdn.jsdelivr.net" crossorigin>
            
            <!-- Critical CSS -->
            <style type="text/css">
                body { 
                    color: ${markdownStyles.hexTextColor}; 
                    background-color: ${markdownStyles.backgroundColor.toHexColor()} !important;
                    padding: 0 16px; 
                    margin: 0; 
                }
                img { 
                    max-width: 100%; 
                    height: auto; 
                    -webkit-touch-callout: none;
                    pointer-events: auto !important;
                    draggable: false;
                }
                a { color: ${markdownStyles.hexLinkColor}; }
                p code, td code { 
                    background-color: ${markdownStyles.hexCodeBackgroundColor}; 
                    padding: 4px 4px 2px 4px; 
                    margin: 4px; 
                    border-radius: 4px; 
                    font-family: monospace; 
                }
                pre { 
                    background-color: ${markdownStyles.hexPreBackgroundColor}; 
                    display: block; 
                    padding: 16px; 
                    overflow-x: auto; 
                    margin: 16px 0;
                }
                blockquote { 
                    border-left: 4px solid ${markdownStyles.hexQuoteBackgroundColor}; 
                    padding: 0; 
                    margin: 16px 0; 
                }
                blockquote > * { margin-left: 16px; padding: 0; }
                blockquote blockquote { margin: 16px; }
                table { 
                    border-collapse: collapse; 
                    display: block; 
                    overflow-x: auto; 
                    margin: 16px 0; 
                }
                th, td { 
                    border: 1px solid ${markdownStyles.hexBorderColor}; 
                    padding: 6px 13px; 
                    line-height: 1.5; 
                }
                tr:nth-child(even) { background-color: ${markdownStyles.hexPreBackgroundColor}; }
                video::-webkit-media-controls-fullscreen-button { display: none !important; }
                video, audio { width: 100%; }
            </style>
            
            <!-- Async CSS loading -->
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.21/dist/katex.min.css" integrity="sha384-zh0CIslj+VczCZtlzBcjt5ppRcsAmDnRem7ESsYwWwg3m/OaJ2l4x7YBZl9Kxxib" crossorigin="anonymous">
            <link rel="stylesheet" href="$codeTheme">
            
            <!-- Core functionality -->
            <script>
                // Initialize handler objects
                const handlers = {
                    processMediaItems: () => {
                        handlers.processImages();
                        handlers.processAudio();
                        handlers.processVideos();
                        handlers.processCheckboxLists();
                    },
                    
                    processImages: () => {
                        document.querySelectorAll('img').forEach((img, index) => {
                            const imageName = img.getAttribute('src');
                            const id = 'img_' + index;
                            img.setAttribute('data-id', id);
                            img.setAttribute('loading', 'lazy');
                            window.mediaPathHandler.processMedia(imageName, id, "image");
                            
                            let touchStartTime;
                            
                            img.onclick = () => window.imageInterface.onImageClick(img.src);
                            img.oncontextmenu = e => { e.preventDefault(); return false; };
                            img.draggable = false;
                            
                            img.addEventListener('touchstart', () => {
                                touchStartTime = Date.now();
                            });
                            
                            img.addEventListener('touchend', e => {
                                if (Date.now() - touchStartTime >= 500) {
                                    e.preventDefault();
                                }
                            });
                        });
                    },
                    
                    processAudio: () => {
                        document.querySelectorAll('audio').forEach((audio, index) => {
                            const audioName = audio.getAttribute('src');
                            const id = 'audio_' + index;
                            audio.setAttribute('data-id', id);
                            audio.controls = true;
                            audio.controlsList = "nodownload";
                            window.mediaPathHandler.processMedia(audioName, id, "audio");
                            
                            audio.oncontextmenu = e => { e.preventDefault(); return false; };
                        });
                    },
                    
                    processVideos: () => {
                        document.querySelectorAll('video').forEach((video, index) => {
                            const videoName = video.getAttribute('src');
                            const id = 'video_' + index;
                            video.setAttribute('data-id', id);
                            video.controls = true;
                            video.controlsList = "nodownload nofullscreen";
                            window.mediaPathHandler.processMedia(videoName, id, "video");
                            
                            video.oncontextmenu = e => { e.preventDefault(); return false; };
                        });
                    },
                    
                    processCheckboxLists: () => {
                        document.querySelectorAll('li').forEach(li => {
                            if (li.querySelector('input[type="checkbox"]')) {
                                li.style.listStyleType = 'none';
                            }
                        });
                    }
                };
                
                // Execute on page load
                document.addEventListener('DOMContentLoaded', () => {
                    handlers.processMediaItems();
                    
                    // Initialize Mermaid if available
                    if (typeof mermaid !== 'undefined') {
                        mermaid.initialize({ startOnLoad: true });
                    }
                });
            </script>
        </head>
        <body>
            $html
            
            <!-- Deferred JavaScript -->
            <script src="file:///android_asset/mermaid.min.js" defer></script>
            <script src="https://cdn.jsdelivr.net/npm/prismjs@1.30.0/components/prism-core.min.js" defer></script>
            <script src="https://cdn.jsdelivr.net/npm/prismjs@1.30.0/plugins/autoloader/prism-autoloader.min.js" defer></script>
            
            <!-- KaTeX rendering -->
            <script src="https://cdn.jsdelivr.net/npm/katex@0.16.21/dist/katex.min.js" integrity="sha384-Rma6DA2IPUwhNxmrB/7S3Tno0YY7sFu9WSYMCuulLhIqYSGZ2gKCJWIqhBWqMQfh" crossorigin="anonymous" defer></script>
            <script src="https://cdn.jsdelivr.net/npm/katex@0.16.21/dist/contrib/auto-render.min.js" integrity="sha384-hCXGrW6PitJEwbkoStFjeJxv+fSOOQKOPbJxSfM6G5sWZjAyWhXiTIIAmQqnlLlh" crossorigin="anonymous" defer 
                onload="renderMathInElement(document.body, {
                    delimiters: [
                        {left: '$$', right: '$$', display: true},
                        {left: '$', right: '$', display: false},
                        {left: '\\\\(', right: '\\\\)', display: false},
                        {left: '\\\\[', right: '\\\\]', display: true}
                    ],
                    throwOnError: false
                });"></script>
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

    var imagesDir by remember { mutableStateOf<DocumentFile?>(null) }
    var audioDir by remember { mutableStateOf<DocumentFile?>(null) }
    var videosDir by remember { mutableStateOf<DocumentFile?>(null) }
    LaunchedEffect(rootUri) {
        withContext(Dispatchers.IO) {
            imagesDir = try {
                DocumentFile.fromTreeUri(context.applicationContext, rootUri)
                    ?.findFile(Constants.File.OPENNOTE)
                    ?.findFile(Constants.File.OPENNOTE_IMAGES)
            } catch (e: Exception) {
                null
            }
            audioDir = try {
                DocumentFile.fromTreeUri(context.applicationContext, rootUri)
                    ?.findFile(Constants.File.OPENNOTE)
                    ?.findFile(Constants.File.OPENNOTE_AUDIO)
            } catch (e: Exception) {
                null
            }
            videosDir = try {
                DocumentFile.fromTreeUri(context.applicationContext, rootUri)
                    ?.findFile(Constants.File.OPENNOTE)
                    ?.findFile(Constants.File.OPENNOTE_VIDEOS)
            } catch (e: Exception) {
                null
            }
            // 目录加载完成后，重新触发媒体处理
            withContext(Dispatchers.Main) {
                webView?.evaluateJavascript(
                    """
                (function() {
                    setupImageHandlers();
                    setupAudioHandlers();
                    setupVideoHandlers();
                })();
            """.trimIndent(), null
                )
            }
        }
    }

    AndroidView(
        modifier = modifier.clipToBounds(),
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
                        fun processMedia(mediaName: String, id: String, mediaType: String) {
                            coroutineScope.launch(Dispatchers.IO) {
                                val mediaUri = when (mediaType) {
                                    "image" -> {
                                        // Check cache first for images
                                        if (imageCache.containsKey(mediaName)) {
                                            withContext(Dispatchers.Main) {
                                                webView?.evaluateJavascript(
                                                    """
                                    (function() {
                                        const img = document.querySelector('img[data-id="$id"]');
                                        if (img) img.src = '${imageCache[mediaName]}';
                                    })();
                                    """.trimIndent(), null
                                                )
                                            }
                                            return@launch
                                        }

                                        val file =
                                            imagesDir?.listFiles()?.find { it.name == mediaName }
                                        val uri = file?.uri?.toString().orEmpty()

                                        // Update image cache
                                        if (uri.isNotEmpty()) {
                                            imageCache[mediaName] = uri
                                        }
                                        uri
                                    }

                                    "audio" -> {
                                        val file =
                                            audioDir?.listFiles()?.find { it.name == mediaName }
                                        file?.uri?.toString().orEmpty()
                                    }

                                    "video" -> {
                                        val file =
                                            videosDir?.listFiles()?.find { it.name == mediaName }
                                        file?.uri?.toString().orEmpty()
                                    }

                                    else -> ""
                                }

                                if (mediaUri.isEmpty()) return@launch

                                withContext(Dispatchers.Main) {
                                    when (mediaType) {
                                        "image" -> {
                                            webView?.evaluateJavascript(
                                                """
                                (function() {
                                    const img = document.querySelector('img[data-id="$id"]');
                                    if (img) img.src = '$mediaUri';
                                })();
                                """.trimIndent(), null
                                            )
                                        }

                                        "audio" -> {
                                            webView?.evaluateJavascript(
                                                """
                                (function() {
                                    const audio = document.querySelector('audio[data-id="$id"]');
                                    if (audio) audio.src = '$mediaUri';
                                })();
                                """.trimIndent(), null
                                            )
                                        }

                                        "video" -> {
                                            // Generate thumbnail for videos
                                            val thumbnail = withContext(Dispatchers.IO) {
                                                val retriever = MediaMetadataRetriever()
                                                try {
                                                    retriever.setDataSource(
                                                        context.applicationContext,
                                                        mediaUri.toUri()
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

                                            webView?.evaluateJavascript(
                                                """
                                (function() {
                                    const video = document.querySelector('video[data-id="$id"]');
                                    if (video) {
                                        video.src = '$mediaUri';
                                        video.poster = '$thumbnail';
                                    }
                                })();
                                """.trimIndent(), null
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    "mediaPathHandler"
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
            it.clearHistory()
            it.stopLoading()
            it.destroy()
            webView = null
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
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
        // Use a more efficient compression quality based on bitmap size
        val quality = when {
            bitmap.byteCount > 4 * 1024 * 1024 -> 40 // Large images
            bitmap.byteCount > 1024 * 1024 -> 60 // Medium images
            else -> 80 // Small images
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        Base64.encode(outputStream.toByteArray())
    }
}