package com.yangdai.opennote.presentation.util

import android.content.Intent
import android.icu.text.DateFormat
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
