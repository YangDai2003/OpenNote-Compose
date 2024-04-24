package com.yangdai.opennote.presentation.util

import android.content.Intent
import android.icu.text.DateFormat
import android.os.Environment
import java.io.File
import java.util.Date

fun Intent.isTextMimeType() = type?.startsWith(Constants.MIME_TYPE_TEXT) == true

fun Intent.parseSharedContent(): String {
    if (action != Intent.ACTION_SEND) return ""

    return if (isTextMimeType()) {
        getStringExtra(Intent.EXTRA_TEXT) ?: ""
    } else {
        ""
    }
}

fun Long.timestampToFormatLocalDateTime(): String {
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
        .format(Date(this))
}

fun createAppDirectory(): String {
    val directory = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        Constants.File.OPENNOTE
    )
    if (!directory.exists()) {
        directory.mkdirs()
    }
    return directory.absolutePath
}

fun createBackupDirectory(): String {
    val directory = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        Constants.File.OPENNOTE_BACKUP
    )
    if (!directory.exists()) {
        directory.mkdirs()
    }
    return directory.absolutePath
}