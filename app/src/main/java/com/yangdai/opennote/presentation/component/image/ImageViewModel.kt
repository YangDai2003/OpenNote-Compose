package com.yangdai.opennote.presentation.component.image

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class ImageViewModel : ViewModel() {
    private val _imageState = MutableStateFlow<ImageState>(ImageState.Empty)
    val imageState = _imageState.asStateFlow()

    fun loadImage(context: Context, imageUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _imageState.value = ImageState.Loading
            try {
                val isLocalFile = isLocalFile(imageUrl)
                val imagePath = if (isLocalFile) getLocalFilePath(imageUrl) else downloadAndSaveImage(context, imageUrl)
                _imageState.value = ImageState.Success(imagePath, !imageUrl.startsWith("content://"), isLocalFile)
            } catch (e: Exception) {
                _imageState.value = ImageState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun isLocalFile(url: String): Boolean {
        return url.startsWith("file://") || url.startsWith("content://")
    }

    private fun getLocalFilePath(fileUrl: String): String {
        // 移除 "file:///" 前缀
        return fileUrl.replace("file:///", "")
    }

    private suspend fun downloadAndSaveImage(context: Context, imageUrl: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(imageUrl).openConnection() as HttpURLConnection
                connection.connect()

                val inputStream = connection.inputStream
                // 创建临时文件
                val fileName = "img_${System.currentTimeMillis()}"
                val file = File(context.cacheDir, fileName)

                // 将图片保存到临时文件
                FileOutputStream(file).use { output ->
                    inputStream.copyTo(output)
                }

                file.absolutePath
            } catch (e: Exception) {
                throw e
            }
        }
    }

    // 清理缓存目录中的图片文件
    fun clearCache(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _imageState.value = ImageState.Empty
            try {
                context.cacheDir.listFiles()?.forEach { file ->
                    if (file.name.startsWith("img_")) {
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
