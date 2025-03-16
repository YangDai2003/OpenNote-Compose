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
import android.util.LruCache
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
import androidx.compose.runtime.saveable.rememberSaveable
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
    val context = LocalContext.current
    val activity = LocalActivity.current
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

    var template by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            template = try {
                context.assets.open("template.html").bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }

    val data by remember(html, markdownStyles, codeTheme, isAppInDarkMode, template) {
        mutableStateOf(
            processHtml(html, markdownStyles, codeTheme, isAppInDarkMode, template)
        )
    }

    val customTabsIntent = rememberCustomTabsIntent()
    val coroutineScope = rememberCoroutineScope()

    var webView by remember { mutableStateOf<WebView?>(null) }

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

    LaunchedEffect(printEnabled.value) {
        if (!printEnabled.value) return@LaunchedEffect
        webView?.let {
            createWebPrintJob(it, activity, noteName)
        }
        printEnabled.value = false
    }
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
            MediaCache.clearCaches()
            imagesDir = try {
                DocumentFile.fromTreeUri(context.applicationContext, rootUri)
                    ?.findFile(Constants.File.OPENNOTE)
                    ?.findFile(Constants.File.OPENNOTE_IMAGES)
            } catch (_: Exception) {
                null
            }
            audioDir = try {
                DocumentFile.fromTreeUri(context.applicationContext, rootUri)
                    ?.findFile(Constants.File.OPENNOTE)
                    ?.findFile(Constants.File.OPENNOTE_AUDIO)
            } catch (_: Exception) {
                null
            }
            videosDir = try {
                DocumentFile.fromTreeUri(context.applicationContext, rootUri)
                    ?.findFile(Constants.File.OPENNOTE)
                    ?.findFile(Constants.File.OPENNOTE_VIDEOS)
            } catch (_: Exception) {
                null
            }
            // 目录加载完成后，重新触发媒体处理
            withContext(Dispatchers.Main) {
                webView?.evaluateJavascript(
                    """
        (function() {
            if (handlers && typeof handlers.processMediaItems === 'function') {
                handlers.processMediaItems();
            }
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
                                when (mediaType) {
                                    "image" -> {
                                        // Check cache first for images
                                        MediaCache.getImageUri(mediaName)?.let {
                                            updateImageInWebView(id, it)
                                            return@launch
                                        }

                                        val file =
                                            imagesDir?.listFiles()?.find { it.name == mediaName }
                                        val uri = file?.uri?.toString().orEmpty()
                                        if (uri.isNotEmpty()) {
                                            MediaCache.cacheImageUri(mediaName, uri)
                                            updateImageInWebView(id, uri)
                                        }
                                    }

                                    "video" -> {
                                        val file =
                                            videosDir?.listFiles()?.find { it.name == mediaName }
                                        val uri = file?.uri ?: return@launch
                                        val uriString = uri.toString()

                                        // Get thumbnail from cache or generate new one
                                        val thumbnail =
                                            MediaCache.getVideoThumbnail(mediaName) ?: run {
                                                val newThumbnail =
                                                    MediaCache.generateVideoThumbnail(context, uri)
                                                if (newThumbnail.isNotEmpty()) {
                                                    MediaCache.cacheVideoThumbnail(
                                                        mediaName,
                                                        newThumbnail
                                                    )
                                                }
                                                newThumbnail
                                            }

                                        updateVideoInWebView(id, uriString, thumbnail)
                                    }

                                    "audio" -> {
                                        val file =
                                            audioDir?.listFiles()?.find { it.name == mediaName }
                                        val uri = file?.uri?.toString().orEmpty()
                                        if (uri.isNotEmpty()) {
                                            updateAudioInWebView(id, uri)
                                        }
                                    }
                                }
                            }
                        }

                        private suspend fun updateImageInWebView(id: String, uri: String) {
                            withContext(Dispatchers.Main) {
                                webView?.evaluateJavascript(
                                    """
                    (function() {
                        const img = document.querySelector('img[data-id="$id"]');
                        if (img) img.src = '$uri';
                    })();
                    """.trimIndent(), null
                                )
                            }
                        }

                        private suspend fun updateVideoInWebView(
                            id: String,
                            uri: String,
                            thumbnail: String
                        ) {
                            withContext(Dispatchers.Main) {
                                webView?.evaluateJavascript(
                                    """
                    (function() {
                        const video = document.querySelector('video[data-id="$id"]');
                        if (video) {
                            video.src = '$uri';
                            video.poster = '$thumbnail';
                        }
                    })();
                    """.trimIndent(), null
                                )
                            }
                        }

                        private suspend fun updateAudioInWebView(id: String, uri: String) {
                            withContext(Dispatchers.Main) {
                                webView?.evaluateJavascript(
                                    """
                    (function() {
                        const audio = document.querySelector('audio[data-id="$id"]');
                        if (audio) audio.src = '$uri';
                    })();
                    """.trimIndent(), null
                                )
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

    val bitmap = convertHtmlToBitmap(webView) ?: return
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
    var bitmap: Bitmap? = null
    try {
        webView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        //layout of webview
        webView.layout(0, 0, webView.measuredWidth, webView.measuredHeight)

        webView.isDrawingCacheEnabled = true
        webView.buildDrawingCache()
        //create Bitmap if measured height and width >0
        bitmap = if (webView.measuredWidth > 0 && webView.measuredHeight > 0)
            createBitmap(webView.measuredWidth, webView.measuredHeight)
        else null
        // Draw bitmap on canvas
        bitmap?.let {
            Canvas(bitmap).apply {
                drawBitmap(it, 0f, bitmap.height.toFloat(), Paint())
                webView.draw(this)
            }
        }
        return bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        bitmap?.recycle()
        return null
    }
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

private fun processHtml(
    html: String,
    markdownStyles: MarkdownStyles,
    codeTheme: String,
    isAppInDarkMode: Boolean,
    template: String
): String {
    return template
        .replace("{{CONTENT}}", html)
        .replace("{{TEXT_COLOR}}", markdownStyles.hexTextColor)
        .replace("{{BACKGROUND_COLOR}}", markdownStyles.backgroundColor.toHexColor())
        .replace("{{CODE_BACKGROUND}}", markdownStyles.hexCodeBackgroundColor)
        .replace("{{PRE_BACKGROUND}}", markdownStyles.hexPreBackgroundColor)
        .replace("{{QUOTE_BACKGROUND}}", markdownStyles.hexQuoteBackgroundColor)
        .replace("{{LINK_COLOR}}", markdownStyles.hexLinkColor)
        .replace("{{BORDER_COLOR}}", markdownStyles.hexBorderColor)
        .replace("{{COLOR_SCHEME}}", if (isAppInDarkMode) "dark" else "light")
        .replace("{{CODE_THEME}}", codeTheme)
}

// Create a media cache manager as a singleton object
object MediaCache {
    private const val MAX_CACHE_SIZE = 100
    private const val THUMBNAIL_QUALITY = 70

    private val imageCache = LruCache<String, String>(MAX_CACHE_SIZE)
    private val videoThumbnailCache = LruCache<String, String>(MAX_CACHE_SIZE)

    fun getImageUri(key: String): String? = imageCache.get(key)

    fun cacheImageUri(key: String, uri: String) {
        imageCache.put(key, uri)
    }

    fun getVideoThumbnail(key: String): String? = videoThumbnailCache.get(key)

    @OptIn(ExperimentalEncodingApi::class)
    fun generateVideoThumbnail(context: Context, mediaUri: Uri): String {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, mediaUri)
            val bitmap = retriever.getFrameAtTime(0) ?: return ""
            return ByteArrayOutputStream().use { outputStream ->
                // Dynamic quality based on image size
                val quality = when {
                    bitmap.byteCount > 2 * 1024 * 1024 -> 50
                    else -> THUMBNAIL_QUALITY
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                "data:image/jpeg;base64,${Base64.encode(outputStream.toByteArray())}"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        } finally {
            retriever.release()
        }
    }

    fun cacheVideoThumbnail(key: String, thumbnail: String) {
        videoThumbnailCache.put(key, thumbnail)
    }

    fun clearCaches() {
        imageCache.evictAll()
        videoThumbnailCache.evictAll()
    }
}