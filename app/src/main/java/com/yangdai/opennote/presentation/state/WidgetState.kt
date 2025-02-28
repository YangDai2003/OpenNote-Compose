package com.yangdai.opennote.presentation.state

import androidx.compose.runtime.Stable

@Stable
data class WidgetState(
    val textSize: WidgetTextSize = WidgetTextSize.MEDIUM,
    val textLines: Int = 1,
    val backgroundColor: WidgetBackgroundColor = WidgetBackgroundColor.MATERIAL3
)

enum class WidgetTextSize(private val value: Int) {
    SMALL(0),
    MEDIUM(1),
    LARGE(2);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: MEDIUM
        fun WidgetTextSize.toInt() = value
    }
}

enum class WidgetBackgroundColor(private val value: Int) {
    TRANSPARENT(0),
    MATERIAL3(1);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: MATERIAL3
        fun WidgetBackgroundColor.toInt() = value
    }
}