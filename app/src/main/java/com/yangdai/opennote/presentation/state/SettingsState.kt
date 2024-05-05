package com.yangdai.opennote.presentation.state

data class SettingsState(
    val theme: Int = -1,
    val color: Int = 0,
    val needPassword: Boolean = false,
    val isAppInDarkMode: Boolean = false,
    val shouldFollowSystem: Boolean = false,
    val isSwitchActive: Boolean = false,
    val isListView: Boolean = false
)
