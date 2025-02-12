package com.yangdai.opennote.presentation.screen

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.component.login.NumberLockScreen
import com.yangdai.opennote.presentation.util.BiometricPromptManager

@Composable
fun LoginOverlayScreen(
    password: String,
    biometricAuthEnabled: Boolean,
    isCreatingPass: Boolean,
    onCreatingCanceled: () -> Unit,
    onPassCreated: (String) -> Unit,
    onAuthenticated: () -> Unit,
    onAuthenticationNotEnrolled: () -> Unit
) {

    val activity = LocalActivity.current as AppCompatActivity
    val promptManager = BiometricPromptManager(activity)

    val biometricPromptResult by promptManager.promptResult.collectAsStateWithLifecycle(initialValue = null)

    biometricPromptResult?.let { result ->
        var showToast = true
        val message = when (result) {
            is BiometricPromptManager.BiometricPromptResult.AuthenticationError -> result.errString
            BiometricPromptManager.BiometricPromptResult.AuthenticationFailed -> "Authentication Failed"
            BiometricPromptManager.BiometricPromptResult.AuthenticationNotEnrolled -> "Authentication Not Enrolled"
            BiometricPromptManager.BiometricPromptResult.AuthenticationSucceeded -> {
                showToast = false
                onAuthenticated()
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
                onAuthenticationNotEnrolled()
            }
        }
    }

    val title = stringResource(R.string.unlock_to_use_open_note)
    val negativeButtonText = stringResource(android.R.string.cancel)

    NumberLockScreen(
        password = password,
        biometricAuthEnabled = biometricAuthEnabled,
        isCreatingPass = isCreatingPass,
        onCreatingCanceled = onCreatingCanceled,
        onPassCreated = onPassCreated,
        onFingerprintClick = {
            promptManager.showBiometricPrompt(
                title = title,
                negativeButtonText = negativeButtonText
            )
        },
        onAuthenticated = onAuthenticated
    )
}
