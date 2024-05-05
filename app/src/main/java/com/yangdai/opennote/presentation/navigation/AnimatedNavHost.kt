package com.yangdai.opennote.presentation.navigation

import android.content.Intent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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

private const val ProgressThreshold = 0.35f
private const val INITIAL_OFFSET_FACTOR = 0.10f
private val Int.ForOutgoing: Int
    get() = (this * ProgressThreshold).toInt()

private val Int.ForIncoming: Int
    get() = this - this.ForOutgoing

fun sharedAxisXIn(
    initialOffsetX: (fullWidth: Int) -> Int,
    durationMillis: Int = NAV_ANIMATION_TIME,
): EnterTransition = slideInHorizontally(
    animationSpec = tween(
        durationMillis = durationMillis,
        easing = FastOutSlowInEasing
    ),
    initialOffsetX = initialOffsetX
) + fadeIn(
    animationSpec = tween(
        durationMillis = durationMillis.ForIncoming,
        delayMillis = durationMillis.ForOutgoing,
        easing = LinearOutSlowInEasing
    )
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
) + fadeOut(
    animationSpec = tween(
        durationMillis = durationMillis.ForOutgoing,
        delayMillis = 0,
        easing = FastOutLinearInEasing
    )
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
    },
) {

    composable(route = Route.MAIN) {
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
            navigateUp = { navController.popBackStack() }
        ) { navController.navigate(Route.CAMERAX) }
    }

    composable(route = Route.FOLDERS) {
        FolderScreen {
            navController.popBackStack()
        }
    }

    composable(route = Route.CAMERAX) {
        CameraXScreen(onCloseClick = { navController.popBackStack() }) {
            navController.previousBackStackEntry?.savedStateHandle?.set("scannedText", it)
            navController.popBackStack()
        }
    }

    composable(route = Route.SETTINGS) {
        SettingsScreen {
            navController.popBackStack()
        }
    }
}
