package com.yangdai.opennote.presentation.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.text.DateFormat
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import com.yangdai.opennote.MainActivity
import java.util.Date

fun Intent.isTextMimeType() = type?.startsWith(Constants.MIME_TYPE_TEXT) == true

fun Intent.parseSharedContent(context: Context): String {
    when (action) {
        Intent.ACTION_SEND -> {
            return if (isTextMimeType()) {
                getStringExtra(Intent.EXTRA_TEXT) ?: ""
            } else {
                ""
            }
        }

        Intent.ACTION_VIEW -> {
            return if (isTextMimeType()) {
                var text = ""
                try {
                    context.contentResolver.openInputStream(data!!)?.use {
                        it.bufferedReader().use { reader ->
                            text = reader.readText()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                text
            } else {
                ""
            }
        }

        else -> return ""
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