package com.yangdai.opennote.presentation.screen

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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yangdai.opennote.presentation.util.BiometricPromptManager
import com.yangdai.opennote.presentation.util.Constants.APP_THEME
import com.yangdai.opennote.presentation.util.Constants.IS_APP_IN_DARK_MODE
import com.yangdai.opennote.presentation.util.Constants.IS_SWITCH_ACTIVE
import com.yangdai.opennote.presentation.util.Constants.MASK_CLICK_X
import com.yangdai.opennote.presentation.util.Constants.MASK_CLICK_Y
import com.yangdai.opennote.presentation.util.Constants.SHOULD_FOLLOW_SYSTEM
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel
import com.yangdai.opennote.presentation.component.MaskAnimModel
import com.yangdai.opennote.presentation.component.MaskBox
import com.yangdai.opennote.presentation.navigation.AnimatedNavHost
import com.yangdai.opennote.presentation.theme.OpenNoteTheme
import com.yangdai.opennote.presentation.util.Constants
import kotlinx.coroutines.flow.map

@Composable
fun BaseScreen(
    sharedViewModel: SharedViewModel,
    promptManager: BiometricPromptManager,
    isLargeScreen: Boolean
) {

    val settingsState by sharedViewModel.settingsStateFlow.collectAsStateWithLifecycle()

    // Biometric authentication
    var authenticated by rememberSaveable {
        mutableStateOf(false)
    }

    // Check if the user is logged in
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
            } else {
                sharedViewModel.putPreferenceValue(Constants.NEED_PASSWORD, false)
            }
        }
    }

    val isAppInDarkTheme by getPreferenceState(
        sharedViewModel,
        booleanPreferencesKey(IS_APP_IN_DARK_MODE),
        false
    )
    val shouldFollowSystem by getPreferenceState(
        sharedViewModel,
        booleanPreferencesKey(SHOULD_FOLLOW_SYSTEM),
        false
    )
    val switchActive by getPreferenceState(
        sharedViewModel,
        booleanPreferencesKey(IS_SWITCH_ACTIVE),
        false
    )
    val maskClickX by getPreferenceState(
        sharedViewModel,
        floatPreferencesKey(MASK_CLICK_X),
        0f
    )
    val maskClickY by getPreferenceState(
        sharedViewModel,
        floatPreferencesKey(MASK_CLICK_Y),
        0f
    )

    val isSystemInDarkTheme = isSystemInDarkTheme()

    LaunchedEffect(settingsState.theme) {
        if (settingsState.theme == 0) {
            sharedViewModel.putPreferenceValue(IS_APP_IN_DARK_MODE, isSystemInDarkTheme)
        }
    }

    OpenNoteTheme(
        color = settingsState.color,
        darkMode = isAppInDarkTheme
    ) {

        // MaskBox is a custom composable that animates a mask over the screen
        MaskBox(
            maskComplete = {
                sharedViewModel.putPreferenceValue(IS_APP_IN_DARK_MODE, !isAppInDarkTheme)
            },
            animFinish = {
                sharedViewModel.putPreferenceValue(IS_SWITCH_ACTIVE, false)
                if (shouldFollowSystem) {
                    sharedViewModel.putPreferenceValue(APP_THEME, 0)
                }
            }
        ) { maskActiveEvent ->

            LaunchedEffect(switchActive) {
                if (!switchActive) return@LaunchedEffect

                if (isAppInDarkTheme)
                    maskActiveEvent(MaskAnimModel.SHRINK, maskClickX, maskClickY)
                else
                    maskActiveEvent(MaskAnimModel.EXPEND, maskClickX, maskClickY)
            }

            AnimatedNavHost(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (!loggedIn) Modifier.blur(16.dp) else Modifier),
                isLargeScreen = isLargeScreen
            )

            AnimatedVisibility(visible = !loggedIn, enter = fadeIn(), exit = fadeOut()) {
                LoginOverlayScreen(promptManager = promptManager)
            }
        }
    }
}

@Composable
private fun <T> getPreferenceState(
    viewModel: SharedViewModel,
    key: Preferences.Key<T>,
    defaultValue: T
): State<T> {
    return viewModel.preferencesFlow()
        .map { preferences -> preferences[key] ?: defaultValue }
        .collectAsStateWithLifecycle(initialValue = defaultValue)
}
