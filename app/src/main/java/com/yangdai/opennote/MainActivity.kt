package com.yangdai.opennote

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Build
import android.os.Bundle

import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.yangdai.opennote.home.MainViewModel
import com.yangdai.opennote.note.NoteViewModel
import com.yangdai.opennote.note.UiEvent
import com.yangdai.opennote.ui.screens.FolderScreen
import com.yangdai.opennote.ui.screens.MainScreen
import com.yangdai.opennote.ui.screens.NoteScreen
import com.yangdai.opennote.ui.screens.SettingsScreen
import com.yangdai.opennote.ui.theme.OpenNoteTheme
import com.yangdai.opennote.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executor

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        executor = ContextCompat.getMainExecutor(this)

        setContent {

            var authenticationSucceeded by remember {
                mutableStateOf(false)
            }

            biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {

                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult
                    ) {
                        super.onAuthenticationSucceeded(result)
                        authenticationSucceeded = true
                    }
                })
            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(stringResource(R.string.biometric_login))
                .setSubtitle(stringResource(R.string.log_in_using_your_biometric_credential))
                .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
                .build()

            val context = LocalContext.current
            val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

            var appMode by remember {
                mutableIntStateOf(defaultSharedPreferences.getInt("APP_MODE", 0))
            }
            var appColor by remember {
                mutableIntStateOf(defaultSharedPreferences.getInt("APP_COLOR", 0))
            }

            val listener = OnSharedPreferenceChangeListener { _, key ->
                when (key) {
                    "APP_MODE" -> appMode = defaultSharedPreferences.getInt("APP_MODE", 0)
                    "APP_COLOR" -> appColor = defaultSharedPreferences.getInt("APP_COLOR", 0)
                }
            }

            val needLogin = defaultSharedPreferences.getBoolean("APP_PASSWORD", false)

            var loggedIn by remember {
                mutableStateOf(false)
            }

            if (!needLogin || authenticationSucceeded) loggedIn = true

            val lifecycleOwner = LocalLifecycleOwner.current

            DisposableEffect(lifecycleOwner) {

                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_STOP) {
                        if (needLogin) {
                            loggedIn = false
                            authenticationSucceeded = false
                        }
                    }
                }

                // Add the observer to the lifecycle
                lifecycleOwner.lifecycle.addObserver(observer)
                defaultSharedPreferences.registerOnSharedPreferenceChangeListener(listener)

                // When the effect leaves the Composition, remove the observer
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }

            OpenNoteTheme(darkTheme = shouldUseDarkTheme(mode = appMode), color = appColor) {

                val navController = rememberNavController()

                val time = 350

                val modifier: Modifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier
                        .fillMaxSize()
                        .blur(16.dp)
                        .pointerInput(Unit) {

                        }
                } else {
                    Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.surface)
                        .pointerInput(Unit) {

                        }
                }

                NavHost(
                    modifier = if (!loggedIn) modifier else Modifier,
                    navController = navController, startDestination = Route.MAIN,
                    enterTransition = {
                        slideIntoContainer(
                            animationSpec = tween(time),
                            towards = AnimatedContentTransitionScope.SlideDirection.Left
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            animationSpec = tween(time),
                            towards = AnimatedContentTransitionScope.SlideDirection.Left
                        )
                    },
                    popEnterTransition = {
                        slideIntoContainer(
                            animationSpec = tween(time),
                            towards = AnimatedContentTransitionScope.SlideDirection.Right
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            animationSpec = tween(time),
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
                                it.sharedViewModel<MainViewModel>(navController = navController)
                            MainScreen(navController, viewModel)
                        }

                        composable(Route.FOLDERS) {
                            val viewModel =
                                it.sharedViewModel<MainViewModel>(navController = navController)
                            FolderScreen(navController, viewModel)
                        }
                    }

                    composable(Route.NOTE) {
                        val viewModel = hiltViewModel<NoteViewModel>()
                        val noteState by viewModel.stateFlow.collectAsStateWithLifecycle()
                        LaunchedEffect(key1 = true) {
                            viewModel.event.collect { event ->
                                when (event) {
                                    is UiEvent.NavigateBack -> navController.navigateUp()
                                }
                            }
                        }

                        NoteScreen(state = noteState, onEvent = viewModel::onEvent)
                    }

                    composable(Route.SETTINGS) {
                        SettingsScreen(onNavigateToMain = {
                            navController.navigateUp()
                        })
                    }

                }

                if (!loggedIn) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {

                            },
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Red,
                                        Orange,
                                        Yellow,
                                        Green,
                                        Cyan,
                                        Blue,
                                        Purple
                                    )
                                )
                            )
                        )

                        OutlinedButton(onClick = {
                            biometricPrompt.authenticate(promptInfo)
                        }) {
                            Text(text = stringResource(R.string.login))
                        }
                    }
                }
            }
        }
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

@Composable
private fun shouldUseDarkTheme(
    mode: Int,
): Boolean = when (mode) {
    1 -> false
    2 -> true
    else -> isSystemInDarkTheme()
}
