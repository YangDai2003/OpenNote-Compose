package com.yangdai.opennote.presentation.state

import androidx.compose.runtime.Stable
import com.yangdai.opennote.presentation.state.AppColor.entries
import com.yangdai.opennote.presentation.state.AppTheme.entries
import com.yangdai.opennote.presentation.state.ListNoteContentOverflowStyle.entries
import com.yangdai.opennote.presentation.state.ListNoteContentSize.entries

@Stable
data class SettingsState(
    val theme: AppTheme = AppTheme.UNDEFINED,
    val color: AppColor = AppColor.DYNAMIC,
    val isAppInDarkMode: Boolean = false,
    val shouldFollowSystem: Boolean = false,
    val isSwitchActive: Boolean = false,
    val isListView: Boolean = false,
    val isAppInAmoledMode: Boolean = false,
    val isDefaultViewForReading: Boolean = false,
    val isDefaultLiteMode: Boolean = false,
    val isLintActive: Boolean = false,
    val storagePath: String = "",
    val dateFormatter: String = "",
    val timeFormatter: String = "",
    val isScreenProtected: Boolean = false,
    val fontScale: Float = 1f,
    val backupFrequency: Int = 0,
    val password: String = "",
    val biometricAuthEnabled: Boolean = false,
    val enumOverflowStyle: ListNoteContentOverflowStyle = ListNoteContentOverflowStyle.ELLIPSIS,
    val enumContentSize: ListNoteContentSize = ListNoteContentSize.DEFAULT
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

enum class ListNoteContentOverflowStyle(private val value: Int) {
    ELLIPSIS(0),
    CLIP(1);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: ELLIPSIS
        fun ListNoteContentOverflowStyle.toInt() = value
    }
}

enum class ListNoteContentSize(private val value: Int) {
    DEFAULT(0),
    COMPACT(1),
    FLAT(2);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: DEFAULT
        fun ListNoteContentSize.toInt() = value
    }
}