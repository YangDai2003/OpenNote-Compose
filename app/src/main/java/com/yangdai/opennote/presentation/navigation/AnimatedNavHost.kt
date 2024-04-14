package com.yangdai.opennote.presentation.navigation

import android.content.Intent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.yangdai.opennote.presentation.screen.CameraXScreen
import com.yangdai.opennote.presentation.screen.FolderScreen
import com.yangdai.opennote.presentation.screen.MainScreen
import com.yangdai.opennote.presentation.screen.NoteScreen
import com.yangdai.opennote.presentation.screen.SettingsScreen
import com.yangdai.opennote.presentation.util.Constants.NAV_ANIMATION_TIME
@Composable
fun AnimatedNavHost(
    modifier: Modifier,
    navController: NavHostController = rememberNavController(),
    isLargeScreen: Boolean
) = NavHost(
    modifier = modifier,
    navController = navController,
    startDestination = Route.MAIN,
    contentAlignment = Alignment.Center,
    enterTransition = {
        EnterTransition.None
    },
    exitTransition = {
        ExitTransition.None
    },
    popEnterTransition = {
        EnterTransition.None
    },
    popExitTransition = {
        ExitTransition.None
    }
) {

    composable(
        route = Route.MAIN,
        enterTransition = { EnterTransition.None },
        exitTransition = {
            slideOutOfContainer(
                animationSpec = tween(NAV_ANIMATION_TIME),
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                targetOffset = { it / 4 }
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                animationSpec = tween(NAV_ANIMATION_TIME),
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                initialOffset = { it / 4 }
            )
        },
        popExitTransition = { ExitTransition.None }
    ) {
        MainScreen(isLargeScreen = isLargeScreen) { route ->
            navController.navigate(route)
        }
    }

    composable(
        route = Route.FOLDERS,
        enterTransition = {
            slideIntoContainer(
                animationSpec = tween(NAV_ANIMATION_TIME),
                towards = AnimatedContentTransitionScope.SlideDirection.Left
            )
        },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = {
            slideOutOfContainer(
                animationSpec = tween(NAV_ANIMATION_TIME),
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
            )
        }
    ) {
        FolderScreen {
            navController.navigateUp()
        }
    }

    composable(
        route = Route.NOTE,
        deepLinks = listOf(
            navDeepLink {
                action = Intent.ACTION_SEND
                mimeType = "text/*"
            },
            navDeepLink {
                action = Intent.ACTION_VIEW
                mimeType = "text/*"
            }
        ),
        enterTransition = {
            slideIntoContainer(
                animationSpec = tween(NAV_ANIMATION_TIME),
                towards = AnimatedContentTransitionScope.SlideDirection.Left
            )
        },
        exitTransition = {
            slideOutOfContainer(
                animationSpec = tween(NAV_ANIMATION_TIME),
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                targetOffset = { it / 4 }
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                animationSpec = tween(NAV_ANIMATION_TIME),
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                initialOffset = { it / 4 }
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                animationSpec = tween(NAV_ANIMATION_TIME),
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
            )
        }
    ) {
        NoteScreen(
            isLargeScreen = isLargeScreen,
            scannedText = navController.currentBackStackEntry?.savedStateHandle?.get<String>("scannedText"),
            navigateUp = { navController.navigateUp() }
        ) { navController.navigate(Route.CAMERAX) }
    }

    composable(
        route = Route.CAMERAX,
        enterTransition = {
            slideIntoContainer(
                animationSpec = tween(NAV_ANIMATION_TIME),
                towards = AnimatedContentTransitionScope.SlideDirection.Left
            )
        },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = {
            slideOutOfContainer(
                animationSpec = tween(NAV_ANIMATION_TIME),
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
            )
        }
    ) {
        CameraXScreen(onCloseClick = { navController.navigateUp() }) {
            navController.previousBackStackEntry?.savedStateHandle?.set("scannedText", it)
            navController.navigateUp()
        }
    }

    composable(
        route = Route.SETTINGS,
        enterTransition = {
            slideIntoContainer(
                animationSpec = tween(NAV_ANIMATION_TIME),
                towards = AnimatedContentTransitionScope.SlideDirection.Left
            )
        },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = {
            slideOutOfContainer(
                animationSpec = tween(NAV_ANIMATION_TIME),
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
            )
        }
    ) {
        SettingsScreen {
            navController.navigateUp()
        }
    }
}
