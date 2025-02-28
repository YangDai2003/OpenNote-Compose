package com.yangdai.opennote.presentation.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.icu.text.DateFormat
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import kotlinx.serialization.Serializable

fun Intent.isTextMimeType() = type?.startsWith(Constants.MIME_TYPE_TEXT) == true

@Stable
@Serializable
data class SharedContent(val fileName: String = "", val content: String = "")

@SuppressLint("Range")
fun Intent.parseSharedContent(context: Context): SharedContent {
    return when (action) {
        Intent.ACTION_SEND -> {
            SharedContent(
                fileName = getStringExtra(Intent.EXTRA_SUBJECT).orEmpty(),
                content = getStringExtra(Intent.EXTRA_TEXT).orEmpty()
            )
        }

        Intent.ACTION_VIEW, Intent.ACTION_EDIT -> {
            var text = ""
            var fileName = ""
            if (isTextMimeType()) {
                try {
                    data?.let { uri ->
                        fileName = getFileName(context, uri).toString()
                        when (uri.scheme) {
                            "content" -> {
                                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                    inputStream.bufferedReader().use { reader ->
                                        text = reader.readText()
                                    }
                                }
                            }

                            "file" -> {
                                val file = uri.toFile()
                                file.bufferedReader().use { reader ->
                                    text = reader.readText()
                                }
                            }

                            else -> {}
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            SharedContent(fileName, text)
        }

        else -> SharedContent()
    }
}

// 检查目录中是否存在指定名称的文件
fun hasFileWithName(dir: DocumentFile, fileName: String): Boolean {
    return dir.listFiles().any { file ->
        file.name == fileName
    }
}

fun getFileName(context: Context, uri: Uri): String? {
    val docFile = DocumentFile.fromSingleUri(context, uri)
    return docFile?.name
}

fun getOrCreateDirectory(
    context: Context, parentUri: Uri, dirName: String
): DocumentFile? {
    // 尝试获取父URI的DocumentFile表示
    val parent = DocumentFile.fromTreeUri(context, parentUri)
        ?: return null // 无法获取父目录，返回 null

    return try {
        // 检查是否存在同名文件或目录
        parent.findFile(dirName)?.let { existingFile ->
            if (existingFile.isDirectory) {
                // 如果已存在同名目录，则直接返回
                return existingFile
            }
        }

        parent.createDirectory(dirName)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun rememberDateTimeFormatter(): DateFormat {
    return remember { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT) }
}

@Composable
fun rememberCustomTabsIntent(): CustomTabsIntent {
    return remember {
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
    }
}

fun Int.toHexColor(): String {
    return String.format("#%06X", 0xFFFFFF and this)
}