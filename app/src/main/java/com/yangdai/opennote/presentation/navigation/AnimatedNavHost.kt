package com.yangdai.opennote.presentation.navigation

import android.content.Intent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navDeepLink
import com.yangdai.opennote.presentation.util.Constants.NAV_ANIMATION_TIME
import com.yangdai.opennote.presentation.screen.CameraXScreen
import com.yangdai.opennote.presentation.screen.FolderScreen
import com.yangdai.opennote.presentation.screen.MainScreen
import com.yangdai.opennote.presentation.screen.NoteScreen
import com.yangdai.opennote.presentation.screen.SettingsScreen
import com.yangdai.opennote.presentation.screen.sharedViewModel
import com.yangdai.opennote.presentation.viewmodel.MainScreenViewModel

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
        slideIntoContainer(
            animationSpec = tween(NAV_ANIMATION_TIME),
            towards = AnimatedContentTransitionScope.SlideDirection.Left
        )
    },
    exitTransition = {
        slideOutOfContainer(
            animationSpec = tween(NAV_ANIMATION_TIME),
            towards = AnimatedContentTransitionScope.SlideDirection.Left
        )
    },
    popEnterTransition = {
        slideIntoContainer(
            animationSpec = tween(NAV_ANIMATION_TIME),
            towards = AnimatedContentTransitionScope.SlideDirection.Right
        )
    },
    popExitTransition = {
        slideOutOfContainer(
            animationSpec = tween(NAV_ANIMATION_TIME),
            towards = AnimatedContentTransitionScope.SlideDirection.Right
        )
    }
) {
    navigation(
        startDestination = Route.NOTE_LIST,
        route = Route.MAIN
    ) {
        composable(Route.NOTE_LIST) {
            val viewModel =
                it.sharedViewModel<MainScreenViewModel>(navController = navController)
            MainScreen(navController, viewModel, isLargeScreen)
        }

        composable(Route.FOLDERS) {
            val viewModel =
                it.sharedViewModel<MainScreenViewModel>(navController = navController)
            FolderScreen(viewModel) {
                navController.navigateUp()
            }
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
            fadeOut(animationSpec = tween(NAV_ANIMATION_TIME))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(NAV_ANIMATION_TIME))
        },
        popExitTransition = {
            slideOutOfContainer(
                animationSpec = tween(NAV_ANIMATION_TIME),
                towards = AnimatedContentTransitionScope.SlideDirection.Right
            )
        }
    ) {
        NoteScreen(navController = navController, isLargeScreen = isLargeScreen)
    }

    composable(
        route = Route.CAMERAX,
        enterTransition = { scaleIn(animationSpec = tween(NAV_ANIMATION_TIME)) },
        exitTransition = { ExitTransition.None },
        popExitTransition = { scaleOut(animationSpec = tween(NAV_ANIMATION_TIME)) },
        popEnterTransition = { EnterTransition.None }
    ) {
        CameraXScreen(onCloseClick = { navController.navigateUp() }) {
            navController.previousBackStackEntry?.savedStateHandle?.set("scannedText", it)
            navController.navigateUp()
        }
    }

    composable(Route.SETTINGS) {
        SettingsScreen(navigateUp = { navController.navigateUp() })
    }
}