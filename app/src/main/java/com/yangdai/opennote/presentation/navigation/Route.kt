package com.yangdai.opennote.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen(val route: String) {
    @Serializable
    data object Home : Screen("home")

    data object Note : Screen("note/{id}") {
        fun passId(id: Long): String {
            return this.route.replace("{id}", id.toString())
        }
    }

    @Serializable
    data object Settings : Screen("settings")

    @Serializable
    data object Folders : Screen("folders")

    @Serializable
    data object CameraX : Screen("cameraX")
}
