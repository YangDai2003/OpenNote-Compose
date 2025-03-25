package com.yangdai.opennote.presentation.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import java.io.ByteArrayOutputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

// Create a media cache manager as a singleton object
object MediaCache {
    private const val MAX_CACHE_SIZE = 100
    private const val HIGH_QUALITY = 70
    private const val LOW_QUALITY = 50

    private val imageCache = LruCache<String, String>(MAX_CACHE_SIZE)
    private val videoThumbnailCache = LruCache<String, String>(MAX_CACHE_SIZE)

    fun getImageUri(key: String): String? = imageCache[key]

    fun cacheImageUri(key: String, uri: String) {
        imageCache.put(key, uri)
    }

    fun getVideoThumbnail(key: String): String? = videoThumbnailCache[key]

    @OptIn(ExperimentalEncodingApi::class)
    fun generateVideoThumbnail(context: Context, mediaUri: Uri): String {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, mediaUri)
            val bitmap = retriever.getFrameAtTime(0) ?: return ""
            return ByteArrayOutputStream().use { outputStream ->
                // Dynamic quality based on image size
                val quality = when {
                    bitmap.byteCount > 2 * 1024 * 1024 -> LOW_QUALITY
                    else -> HIGH_QUALITY
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
