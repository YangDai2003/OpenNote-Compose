package com.yangdai.opennote.ui.state

data class SettingsState(
    val mode: Int = 0,
    val color: Int = 0,
    val needPassword: Boolean = false
)
