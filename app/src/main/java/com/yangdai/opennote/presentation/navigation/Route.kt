package com.yangdai.opennote.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen() {
    @Serializable
    data object Home : Screen()

    @Serializable
    data class Note(val id: Long, val sharedContent: String = "") : Screen()

    @Serializable
    data class File(val uri: String) : Screen()

    @Serializable
    data object Settings : Screen()

    @Serializable
    data object Folders : Screen()
}
