package com.yangdai.opennote.presentation.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.yangdai.opennote.presentation.navigation.Screen.File
import com.yangdai.opennote.presentation.navigation.Screen.Folders
import com.yangdai.opennote.presentation.navigation.Screen.Home
import com.yangdai.opennote.presentation.navigation.Screen.Note
import com.yangdai.opennote.presentation.navigation.Screen.Settings
import com.yangdai.opennote.presentation.screen.FileScreen
import com.yangdai.opennote.presentation.screen.FolderScreen
import com.yangdai.opennote.presentation.screen.MainScreen
import com.yangdai.opennote.presentation.screen.NoteScreen
import com.yangdai.opennote.presentation.screen.SettingsScreen
import com.yangdai.opennote.presentation.util.Constants.LINK
import com.yangdai.opennote.presentation.util.SharedContent
import com.yangdai.opennote.presentation.util.parseSharedContent
import kotlinx.serialization.json.Json

private fun NavHostController.navigateBackWithHapticFeedback(hapticFeedback: HapticFeedback) {
    hapticFeedback.performHapticFeedback(HapticFeedbackType.VirtualKey)
    navigateUp()
}

@Composable
fun AnimatedNavHost(
    modifier: Modifier,
    isLargeScreen: Boolean,
    hapticFeedback: HapticFeedback = LocalHapticFeedback.current,
    navController: NavHostController = rememberNavController()
) = NavHost(
    modifier = modifier,
    navController = navController,
    startDestination = Home,
    enterTransition = {
        sharedAxisXIn(initialOffsetX = { (it * INITIAL_OFFSET_FACTOR).toInt() })
    },
    exitTransition = {
        sharedAxisXOut(targetOffsetX = { -(it * INITIAL_OFFSET_FACTOR).toInt() })
    },
    popEnterTransition = {
        sharedAxisXIn(initialOffsetX = { -(it * INITIAL_OFFSET_FACTOR).toInt() })
    },
    popExitTransition = {
        sharedAxisXOut(targetOffsetX = { (it * INITIAL_OFFSET_FACTOR).toInt() })
    }
) {

    composable<Home> {
        val context = LocalContext.current.applicationContext
        MainScreen(
            isLargeScreen = isLargeScreen,
            navigateToScreen = { navController.navigate(it) }
        ) {
            it.apply {
                when (action) {
                    Intent.ACTION_SEND, Intent.ACTION_VIEW, Intent.ACTION_EDIT -> {
                        val sharedContent = parseSharedContent(context)
                        if (sharedContent.fileName.isNotEmpty() || sharedContent.content.isNotEmpty()) {
                            navController.navigate(
                                Note(
                                    id = -1L,
                                    sharedContent = Json.encodeToString<SharedContent>(sharedContent)
                                )
                            )
                        }
                    }

                    Intent.ACTION_CREATE_NOTE, "com.google.android.gms.actions.CREATE_NOTE" -> {
                        navController.navigate(Note(-1L))
                    }

                    else -> {}
                }
            }
        }
    }

    composable<Note>(deepLinks = listOf(navDeepLink<Note>(basePath = "$LINK/note"))) {
        val route = it.toRoute<Note>()
        NoteScreen(
            noteId = route.id,
            isLargeScreen = isLargeScreen,
            sharedContent = if (route.sharedContent.isNotBlank())
                Json.decodeFromString<SharedContent>(route.sharedContent)
            else null,
            navigateUp = { navController.navigateBackWithHapticFeedback(hapticFeedback) }
        )
    }

    composable<File> {
        val uri = it.toRoute<File>().uri
        FileScreen(
            uriStr = uri,
            isLargeScreen = isLargeScreen,
            navigateUp = { navController.navigateBackWithHapticFeedback(hapticFeedback) }
        )
    }

    composable<Folders> {
        FolderScreen {
            navController.navigateBackWithHapticFeedback(hapticFeedback)
        }
    }

    composable<Settings>(deepLinks = listOf(navDeepLink<Settings>(basePath = "$LINK/settings"))) {
        SettingsScreen {
            navController.navigateBackWithHapticFeedback(hapticFeedback)
        }
    }
}
