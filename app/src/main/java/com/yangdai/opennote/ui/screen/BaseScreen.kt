package com.yangdai.opennote.ui.screen

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.yangdai.opennote.BiometricPromptManager
import com.yangdai.opennote.Constants.APP_THEME
import com.yangdai.opennote.Constants.IS_APP_IN_DARK_MODE
import com.yangdai.opennote.Constants.IS_DARK_SWITCH_ACTIVE
import com.yangdai.opennote.Constants.MASK_CLICK_X
import com.yangdai.opennote.Constants.MASK_CLICK_Y
import com.yangdai.opennote.Constants.SHOULD_FOLLOW_SYSTEM
import com.yangdai.opennote.ui.viewmodel.BaseScreenViewModel
import com.yangdai.opennote.R
import com.yangdai.opennote.ui.component.MaskAnimModel
import com.yangdai.opennote.ui.component.MaskAnimWay
import com.yangdai.opennote.ui.component.MaskBox
import com.yangdai.opennote.ui.navigation.AnimatedNavHost
import com.yangdai.opennote.ui.theme.Blue
import com.yangdai.opennote.ui.theme.Cyan
import com.yangdai.opennote.ui.theme.Green
import com.yangdai.opennote.ui.theme.OpenNoteTheme
import com.yangdai.opennote.ui.theme.Orange
import com.yangdai.opennote.ui.theme.Purple
import com.yangdai.opennote.ui.theme.Red
import com.yangdai.opennote.ui.theme.Yellow
import kotlinx.coroutines.flow.map

@Composable
fun BaseScreen(
    promptManager: BiometricPromptManager,
    baseScreenViewModel: BaseScreenViewModel
) {

    val settingsState by baseScreenViewModel.stateFlow.collectAsStateWithLifecycle()

    var authenticated by rememberSaveable {
        mutableStateOf(false)
    }

    val loggedIn by remember {
        derivedStateOf {
            !settingsState.needPassword || authenticated
        }
    }

    val biometricPromptResult by promptManager.promptResult.collectAsStateWithLifecycle(initialValue = null)

    biometricPromptResult?.let { result ->
        var showToast = true
        val message = when (result) {
            is BiometricPromptManager.BiometricPromptResult.AuthenticationError -> result.errString
            BiometricPromptManager.BiometricPromptResult.AuthenticationFailed -> "Authentication Failed"
            BiometricPromptManager.BiometricPromptResult.AuthenticationNotEnrolled -> "Authentication Not Enrolled"
            BiometricPromptManager.BiometricPromptResult.AuthenticationSucceeded -> {
                authenticated = true
                showToast = false
                "Authentication Succeeded"
            }

            BiometricPromptManager.BiometricPromptResult.FeatureUnavailable -> "Feature Unavailable"
            BiometricPromptManager.BiometricPromptResult.HardwareUnavailable -> "Hardware Unavailable"
        }
        if (showToast)
            Toast.makeText(LocalContext.current, message, Toast.LENGTH_SHORT).show()
    }

    val enrollLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {

        }

    LaunchedEffect(biometricPromptResult) {
        if (biometricPromptResult is BiometricPromptManager.BiometricPromptResult.AuthenticationNotEnrolled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(
                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BiometricManager.Authenticators.BIOMETRIC_STRONG
                    )
                }
                enrollLauncher.launch(enrollIntent)
            }
        }
    }

    val isAppInDarkTheme by baseScreenViewModel.getFlow()
        .map { preferences ->
            preferences[booleanPreferencesKey(IS_APP_IN_DARK_MODE)] ?: false
        }
        .collectAsState(initial = false)
    val followSystem by baseScreenViewModel.getFlow()
        .map { preferences ->
            preferences[booleanPreferencesKey(SHOULD_FOLLOW_SYSTEM)] ?: false
        }
        .collectAsState(initial = false)
    val darkSwitchActive by baseScreenViewModel.getFlow()
        .map { preferences ->
            preferences[booleanPreferencesKey(IS_DARK_SWITCH_ACTIVE)] ?: false
        }
        .collectAsState(initial = false)
    val maskClickX by baseScreenViewModel.getFlow()
        .map { preferences ->
            preferences[floatPreferencesKey(MASK_CLICK_X)] ?: 0f
        }
        .collectAsState(initial = 0f)
    val maskClickY by baseScreenViewModel.getFlow()
        .map { preferences ->
            preferences[floatPreferencesKey(MASK_CLICK_Y)] ?: 0f
        }
        .collectAsState(initial = 0f)

    var maskAnimWay by remember {
        mutableStateOf(MaskAnimWay.DARK_SWITCH)
    }

    val isSystemInDarkTheme = isSystemInDarkTheme()
    LaunchedEffect(isSystemInDarkTheme) {
        if (settingsState.mode == 0) {
            baseScreenViewModel.putBoolean(IS_APP_IN_DARK_MODE, isSystemInDarkTheme)
        }
    }

    OpenNoteTheme(
        color = settingsState.color,
        darkMode = shouldUseDarkTheme(settingsState.mode, isAppInDarkTheme)
    ) {

        MaskBox(
            maskComplete = {
                when (maskAnimWay) {
                    MaskAnimWay.DARK_SWITCH ->
                        baseScreenViewModel.putBoolean(IS_APP_IN_DARK_MODE, !isAppInDarkTheme)
                }
            },
            animFinish = {
                when (maskAnimWay) {
                    MaskAnimWay.DARK_SWITCH -> {
                        baseScreenViewModel.putBoolean(
                            IS_DARK_SWITCH_ACTIVE,
                            false
                        )
                        if (followSystem) {
                            baseScreenViewModel.putInt(APP_THEME, 0)
                        }
                    }
                }
            }
        ) { maskActiveEvent ->
            LaunchedEffect(darkSwitchActive) {
                if (!darkSwitchActive) return@LaunchedEffect
                maskAnimWay = MaskAnimWay.DARK_SWITCH
                if (isAppInDarkTheme)
                    maskActiveEvent(MaskAnimModel.SHRINK, maskClickX, maskClickY)
                else
                    maskActiveEvent(MaskAnimModel.EXPEND, maskClickX, maskClickY)
            }

            val modifier: Modifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Modifier.blur(16.dp)
            } else {
                Modifier.background(color = MaterialTheme.colorScheme.surface)
            }

            AnimatedNavHost(
                modifier = if (!loggedIn) modifier else Modifier,
                navController = rememberNavController()
            )

            AnimatedVisibility(visible = !loggedIn, enter = fadeIn(), exit = fadeOut()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {},
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

                    val title = stringResource(R.string.biometric_login)
                    val subtitle = stringResource(R.string.log_in_using_your_biometric_credential)

                    OutlinedButton(onClick = {
                        promptManager.showBiometricPrompt(
                            title = title,
                            subtitle = subtitle
                        )
                    }) {
                        Text(text = stringResource(R.string.login))
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
    isDarkTheme: Boolean
): Boolean = when (mode) {
    0 -> isSystemInDarkTheme()
    else -> isDarkTheme
}