package com.yangdai.opennote.presentation.navigation

import android.content.Intent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.yangdai.opennote.presentation.util.Constants.NAV_ANIMATION_TIME
import com.yangdai.opennote.presentation.screen.CameraXScreen
import com.yangdai.opennote.presentation.screen.FolderScreen
import com.yangdai.opennote.presentation.screen.MainScreen
import com.yangdai.opennote.presentation.screen.NoteScreen
import com.yangdai.opennote.presentation.screen.SettingsScreen
import com.yangdai.opennote.presentation.viewmodel.MainRouteScreenViewModel

@Composable
fun AnimatedNavHost(
    modifier: Modifier,
    navController: NavHostController,
    isLargeScreen: Boolean
) = NavHost(
    modifier = modifier,
    navController = navController,
    startDestination = Route.MAIN,
    contentAlignment = Alignment.Center,
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
                it.sharedViewModel<MainRouteScreenViewModel>(navController = navController)
            MainScreen(viewModel, isLargeScreen) { route ->
                navController.navigate(route)
            }
        }

        composable(Route.FOLDERS) {
            val viewModel =
                it.sharedViewModel<MainRouteScreenViewModel>(navController = navController)
            FolderScreen(viewModel) {
                navController.navigateUp()
            }
        }

        composable(
            route = Route.NOTE,
            arguments = listOf(navArgument("id") { defaultValue = "" }),
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
        ) { backStackEntry ->
            val viewModel =
                backStackEntry.sharedViewModel<MainRouteScreenViewModel>(navController = navController)
            NoteScreen(
                id = backStackEntry.arguments?.getString("id"),
                viewModel = viewModel,
                isLargeScreen = isLargeScreen,
                scannedText = navController.currentBackStackEntry?.savedStateHandle?.get<String>("scannedText"),
                navigateUp = { navController.navigateUp() }
            ) { navController.navigate(Route.CAMERAX) }
        }
    }

    composable(
        route = Route.CAMERAX,
        enterTransition = { fadeIn() },
        exitTransition = { ExitTransition.None },
        popExitTransition = { fadeOut() },
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

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController): T {
    val navGraphRoute = destination.parent?.route ?: return hiltViewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return hiltViewModel(parentEntry)
}
