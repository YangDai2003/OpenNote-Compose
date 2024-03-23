package com.yangdai.opennote.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.yangdai.opennote.Constants.NAV_ANIMATION_TIME
import com.yangdai.opennote.Route
import com.yangdai.opennote.ui.event.UiEvent
import com.yangdai.opennote.ui.screen.CameraXScreen
import com.yangdai.opennote.ui.screen.FolderScreen
import com.yangdai.opennote.ui.screen.MainScreen
import com.yangdai.opennote.ui.screen.NoteScreen
import com.yangdai.opennote.ui.screen.SettingsScreen
import com.yangdai.opennote.ui.screen.sharedViewModel
import com.yangdai.opennote.ui.viewmodel.MainScreenViewModel
import com.yangdai.opennote.ui.viewmodel.NoteScreenViewModel

@Composable
fun AnimatedNavHost(
    modifier: Modifier,
    navController: NavHostController
) = NavHost(
    modifier = modifier.fillMaxSize(),
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
            MainScreen(navController, viewModel)
        }

        composable(Route.FOLDERS) {
            val viewModel =
                it.sharedViewModel<MainScreenViewModel>(navController = navController)
            FolderScreen(navController, viewModel)
        }
    }

    composable(Route.NOTE) {
        val viewModel : NoteScreenViewModel = hiltViewModel()
        LaunchedEffect(key1 = true) {
            viewModel.event.collect { event ->
                when (event) {
                    is UiEvent.NavigateBack -> navController.navigateUp()
                }
            }
        }
        NoteScreen(
            navController = navController,
            viewModel = viewModel,
            onEvent = viewModel::onEvent
        )
    }

    composable(Route.CAMERAX) {
        CameraXScreen(navController = navController)
    }

    composable(Route.SETTINGS) {
        SettingsScreen(navController = navController)
    }
}