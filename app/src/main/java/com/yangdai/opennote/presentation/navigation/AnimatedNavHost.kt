package com.yangdai.opennote.presentation.navigation

import android.content.Intent
import android.os.Build
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
import com.yangdai.opennote.presentation.screen.CameraXScreen
import com.yangdai.opennote.presentation.screen.FolderScreen
import com.yangdai.opennote.presentation.screen.MainScreen
import com.yangdai.opennote.presentation.screen.NoteScreen
import com.yangdai.opennote.presentation.screen.SettingsScreen
import com.yangdai.opennote.presentation.util.Constants.NAV_ANIMATION_TIME
import com.yangdai.opennote.presentation.util.Constants.LINK
import com.yangdai.opennote.presentation.util.parseSharedContent
import com.yangdai.opennote.presentation.navigation.Screen.*

private const val ProgressThreshold = 0.35f
private const val INITIAL_OFFSET_FACTOR = 0.10f
private val Int.ForOutgoing: Int
    get() = (this * ProgressThreshold).toInt()

private val Int.ForIncoming: Int
    get() = this - this.ForOutgoing

private fun sharedAxisXIn(
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

private fun sharedAxisXOut(
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

private fun NavHostController.navigateBackWithHapticFeedback(hapticFeedback: HapticFeedback) {
    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
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
    },
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
        val sharedText = it.arguments?.getParcelable<Intent>(NavController.KEY_DEEP_LINK_INTENT)
            ?.parseSharedContent(context.applicationContext)?.trim()
        val scannedText = navController.currentBackStackEntry
            ?.savedStateHandle?.get<String>("scannedText")?.trim()
        NoteScreen(
            id = id,
            isLargeScreen = isLargeScreen,
            sharedText = sharedText,
            scannedText = scannedText,
            navigateUp = { navController.navigateBackWithHapticFeedback(hapticFeedback) },
            onScanTextClick = { navController.navigate(CameraX) }
        )
    }

    composable<Folders> {
        FolderScreen {
            navController.navigateBackWithHapticFeedback(hapticFeedback)
        }
    }

    composable<CameraX> {
        CameraXScreen(onCloseClick = {
            navController.navigateBackWithHapticFeedback(hapticFeedback)
        }) {
            navController.previousBackStackEntry?.savedStateHandle?.set("scannedText", it)
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
