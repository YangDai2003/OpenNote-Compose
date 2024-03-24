package com.yangdai.opennote.ui.util

import android.content.ContentValues
import android.content.Context
import android.icu.text.DateFormat
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.yangdai.opennote.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStreamWriter
import java.util.Date

fun timestampToFormatLocalDateTime(timestamp: Long): String {
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
        .format(Date(timestamp))
}

/**
 * @noinspection ResultOfMethodCallIgnored
 */
fun getOutputDirectory(context: Context): String {
    val directory = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        context.getString(R.string.app_name)
    )
    if (!directory.exists()) {
        directory.mkdirs()
    }
    return directory.absolutePath
}

fun exportNote(context: Context, fileName: String, content: String, type : String) {

    val extension = when (type) {
        "TXT" -> ".txt"
        "MARKDOWN" -> ".md"
        else -> ".html"
    }

    getOutputDirectory(context)

    val values = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, "$fileName$extension")
        put(MediaStore.Downloads.MIME_TYPE, "text/*")
        put(
            MediaStore.Downloads.RELATIVE_PATH,
            "${Environment.DIRECTORY_DOWNLOADS}/${context.getString(R.string.app_name)}"
        )
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)

    uri?.let { uri1 ->
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                resolver.openOutputStream(uri1)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(content)
                    }
                }
            }.onSuccess {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        R.string.saved_download,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.onFailure {
                it.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        R.string.failed,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}