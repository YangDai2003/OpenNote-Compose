package com.yangdai.opennote.presentation.util

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.text.DateFormat
import android.provider.OpenableColumns
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import com.yangdai.opennote.MainActivity
import java.util.Date

fun Intent.isTextMimeType() = type?.startsWith(Constants.MIME_TYPE_TEXT) == true

data class SharedContent(val fileName: String, val content: String)

@SuppressLint("Range")
fun Intent.parseSharedContent(context: Context): SharedContent {
    return when (action) {
        Intent.ACTION_SEND -> {
            if (isTextMimeType()) {
                SharedContent("", getStringExtra(Intent.EXTRA_TEXT) ?: "")
            } else {
                SharedContent("", "")
            }
        }

        Intent.ACTION_VIEW -> {
            if (isTextMimeType()) {
                var text = ""
                var fileName = ""
                try {
                    context.contentResolver.openInputStream(data!!)?.use { inputStream ->

                        inputStream.bufferedReader().use { reader ->
                            text = reader.readText()
                        }
                    }
                    context.contentResolver.query(data!!, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            fileName =
                                cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                SharedContent(fileName, text)
            } else {
                SharedContent("", "")
            }
        }

        else -> SharedContent("", "")
    }
}

fun Long.timestampToFormatLocalDateTime(): String {
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
        .format(Date(this))
}

@Composable
fun rememberCustomTabsIntent(): CustomTabsIntent {
    return remember {
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
    }
}

fun Context.sendPendingIntent(data: String) {
    val intent = Intent(this, MainActivity::class.java)
        .setData(data.toUri())
    val pendingIntent = TaskStackBuilder.create(this).run {
        addNextIntentWithParentStack(intent)
        getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
    pendingIntent?.send()
}

fun Int.toHexColor(): String {
    return String.format("#%06X", 0xFFFFFF and this)
}