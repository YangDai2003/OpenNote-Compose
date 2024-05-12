package com.yangdai.opennote.presentation.util

import android.content.Context
import android.content.Intent
import android.icu.text.DateFormat
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
