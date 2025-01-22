package com.yangdai.opennote.presentation.state

import androidx.compose.runtime.Stable

@Stable
data class SettingsState(
    val theme: AppTheme = AppTheme.UNDEFINED,
    val color: AppColor = AppColor.DYNAMIC,
    val needPassword: Boolean = false,
    val isAppInDarkMode: Boolean = false,
    val shouldFollowSystem: Boolean = false,
    val isSwitchActive: Boolean = false,
    val isListView: Boolean = false,
    val isAppInAmoledMode: Boolean = false,
    val isDefaultViewForReading: Boolean = false,
    val isDefaultLiteMode: Boolean = false
)

enum class AppTheme(private val value: Int) {
    UNDEFINED(-1),
    SYSTEM(0),
    LIGHT(1),
    DARK(2);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: UNDEFINED
        fun AppTheme.toInt() = value
    }
}

enum class AppColor(private val value: Int) {
    DYNAMIC(0),
    PURPLE(1),
    BLUE(2),
    GREEN(3),
    ORANGE(4),
    RED(5);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: DYNAMIC
        fun AppColor.toInt() = value
    }
}