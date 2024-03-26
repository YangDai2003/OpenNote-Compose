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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navDeepLink
import com.yangdai.opennote.presentation.util.Constants.NAV_ANIMATION_TIME
import com.yangdai.opennote.presentation.event.UiEvent
import com.yangdai.opennote.presentation.screen.CameraXScreen
import com.yangdai.opennote.presentation.screen.FolderScreen
import com.yangdai.opennote.presentation.screen.MainScreen
import com.yangdai.opennote.presentation.screen.NoteScreen
import com.yangdai.opennote.presentation.screen.SettingsScreen
import com.yangdai.opennote.presentation.screen.sharedViewModel
import com.yangdai.opennote.presentation.viewmodel.MainScreenViewModel
import com.yangdai.opennote.presentation.viewmodel.NoteScreenViewModel

@Composable
fun AnimatedNavHost(
    modifier: Modifier,
    navController: NavHostController,
    windowSize: WindowSizeClass
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
            MainScreen(navController, viewModel, windowSize)
        }

        composable(Route.FOLDERS) {
            val viewModel =
                it.sharedViewModel<MainScreenViewModel>(navController = navController)
            FolderScreen(navController, viewModel)
        }
    }

    composable(
        route = Route.NOTE,
        deepLinks = listOf(
            navDeepLink {
                action = Intent.ACTION_SEND
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
        val viewModel: NoteScreenViewModel = hiltViewModel()
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

    composable(
        route = Route.CAMERAX,
        enterTransition = { scaleIn(animationSpec = tween(NAV_ANIMATION_TIME)) },
        exitTransition = { ExitTransition.None },
        popExitTransition = { scaleOut(animationSpec = tween(NAV_ANIMATION_TIME)) },
        popEnterTransition = { EnterTransition.None }
    ) {
        CameraXScreen(navController = navController)
    }

    composable(Route.SETTINGS) {
        SettingsScreen(navController = navController)
    }
}