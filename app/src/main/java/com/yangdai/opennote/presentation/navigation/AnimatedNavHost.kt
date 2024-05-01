package com.yangdai.opennote.presentation.navigation

import android.content.Intent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.yangdai.opennote.presentation.screen.CameraXScreen
import com.yangdai.opennote.presentation.screen.FolderScreen
import com.yangdai.opennote.presentation.screen.MainScreen
import com.yangdai.opennote.presentation.screen.NoteScreen
import com.yangdai.opennote.presentation.screen.SettingsScreen
import com.yangdai.opennote.presentation.util.Constants.NAV_ANIMATION_TIME
import com.yangdai.opennote.presentation.util.parseSharedContent

fun sharedAxisXIn(
    initialOffsetX: (fullWidth: Int) -> Int,
    durationMillis: Int = NAV_ANIMATION_TIME,
): EnterTransition = slideInHorizontally(
    animationSpec = tween(
        durationMillis = durationMillis,
        easing = FastOutSlowInEasing
    ),
    initialOffsetX = initialOffsetX
)

fun sharedAxisXOut(
    targetOffsetX: (fullWidth: Int) -> Int,
    durationMillis: Int = NAV_ANIMATION_TIME,
): ExitTransition = slideOutHorizontally(
    animationSpec = tween(
        durationMillis = durationMillis,
        easing = FastOutSlowInEasing
    ),
    targetOffsetX = targetOffsetX
)

@Composable
fun AnimatedNavHost(
    modifier: Modifier,
    navController: NavHostController,
    isLargeScreen: Boolean
) = NavHost(
    modifier = modifier,
    navController = navController,
    startDestination = Route.MAIN,
    enterTransition = {
        sharedAxisXIn(initialOffsetX = { fullWidth -> fullWidth })
    },
    exitTransition = {
        sharedAxisXOut(targetOffsetX = { fullWidth -> -fullWidth / 4 })
    },
    popEnterTransition = {
        sharedAxisXIn(initialOffsetX = { fullWidth -> -fullWidth / 4 })
    },
    popExitTransition = {
        sharedAxisXOut(targetOffsetX = { fullWidth -> fullWidth })
    }
) {

    composable(
        route = Route.MAIN,
        enterTransition = { EnterTransition.None },
        exitTransition = {
            sharedAxisXOut(targetOffsetX = { fullWidth -> -fullWidth / 4 })
        },
        popEnterTransition = {
            sharedAxisXIn(initialOffsetX = { fullWidth -> -fullWidth / 4 })
        },
        popExitTransition = { ExitTransition.None }
    ) {
        MainScreen(isLargeScreen = isLargeScreen) { route ->
            navController.navigate(route)
        }
    }

    composable(
        route = Route.NOTE,
        deepLinks = listOf(
            navDeepLink {
                action = Intent.ACTION_SEND
                mimeType = "text/*"
            }
        )
    ) {
        val sharedText = it.arguments?.getParcelable<Intent>(NavController.KEY_DEEP_LINK_INTENT)
            ?.parseSharedContent()?.trim()
        val scannedText =
            navController.currentBackStackEntry?.savedStateHandle?.get<String>("scannedText")
                ?.trim()
        NoteScreen(
            isLargeScreen = isLargeScreen,
            sharedText = sharedText,
            scannedText = scannedText,
            navigateUp = { navController.navigateUp() }
        ) { navController.navigate(Route.CAMERAX) }
    }

    composable(route = Route.FOLDERS) {
        FolderScreen {
            navController.navigateUp()
        }
    }

    composable(route = Route.CAMERAX) {
        CameraXScreen(onCloseClick = { navController.navigateUp() }) {
            navController.previousBackStackEntry?.savedStateHandle?.set("scannedText", it)
            navController.navigateUp()
        }
    }

    composable(route = Route.SETTINGS) {
        SettingsScreen {
            navController.navigateUp()
        }
    }
}
