package com.yangdai.opennote

import android.icu.text.DateFormat
import java.util.Date

fun timestampToFormatLocalDateTime(timestamp: Long): String {
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
        .format(Date(timestamp))
}