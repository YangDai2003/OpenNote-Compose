package com.yangdai.opennote.presentation.navigation

import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.yangdai.opennote.presentation.navigation.Screen.Folders
import com.yangdai.opennote.presentation.navigation.Screen.Home
import com.yangdai.opennote.presentation.navigation.Screen.Note
import com.yangdai.opennote.presentation.navigation.Screen.Settings
import com.yangdai.opennote.presentation.screen.FolderScreen
import com.yangdai.opennote.presentation.screen.MainScreen
import com.yangdai.opennote.presentation.screen.NoteScreen
import com.yangdai.opennote.presentation.screen.SettingsScreen
import com.yangdai.opennote.presentation.util.Constants.LINK
import com.yangdai.opennote.presentation.util.parseSharedContent

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
        MainScreen(
            isLargeScreen = isLargeScreen,
            navigateToNote = { navController.navigate(Note.passId(it)) },
            navigateToScreen = { navController.navigate(it) }
        )
    }

    val noteNavDeepLink = mutableListOf(
        navDeepLink {
            action = Intent.ACTION_SEND
            mimeType = "text/*"
        },
        navDeepLink {
            action = Intent.ACTION_VIEW
            mimeType = "text/*"
        },
        navDeepLink {
            action = Intent.ACTION_EDIT
            mimeType = "text/*"
        },
        navDeepLink {
            uriPattern = "$LINK/${Note.route}"
        }
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        noteNavDeepLink.add(
            navDeepLink {
                action = Intent.ACTION_CREATE_NOTE
            }
        )
    }

    composable(
        route = Note.route,
        deepLinks = noteNavDeepLink,
        arguments = listOf(
            navArgument("id") {
                type = NavType.LongType
                defaultValue = -1L
            }
        )
    ) {
        val context = LocalContext.current
        val id = it.arguments?.getLong("id") ?: -1L
        val sharedContent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            it.arguments?.getParcelable(NavController.KEY_DEEP_LINK_INTENT, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            it.arguments?.getParcelable<Intent>(NavController.KEY_DEEP_LINK_INTENT)
        }?.parseSharedContent(context.applicationContext)
        NoteScreen(
            noteId = id,
            isLargeScreen = isLargeScreen,
            sharedContent = sharedContent,
            navigateUp = { navController.navigateBackWithHapticFeedback(hapticFeedback) }
        )
    }

    composable<Folders> {
        FolderScreen {
            navController.navigateBackWithHapticFeedback(hapticFeedback)
        }
    }

    composable<Settings>(
        deepLinks = listOf(
            navDeepLink {
                uriPattern = "$LINK/${Settings.route}"
            }
        )
    ) {
        SettingsScreen {
            navController.navigateBackWithHapticFeedback(hapticFeedback)
        }
    }
}
