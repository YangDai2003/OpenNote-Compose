package com.yangdai.opennote.presentation.screen

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.component.login.NumberLockScreen
import com.yangdai.opennote.presentation.util.BiometricPromptManager

@Composable
fun LoginOverlayScreen(
    storedPassword: String,
    biometricAuthEnabled: Boolean,
    isCreatingPassword: Boolean,
    onCreatingCanceled: () -> Unit,
    onPassCreated: (String) -> Unit,
    onAuthenticated: () -> Unit,
    onAuthenticationNotEnrolled: () -> Unit
) {
    val context = LocalContext.current
    val activity = LocalActivity.current as AppCompatActivity
    val promptManager = remember(activity) { BiometricPromptManager(activity) }
    val biometricPromptResult by promptManager.promptResult.collectAsStateWithLifecycle(initialValue = null)

    val enrollBiometricsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {}

    LaunchedEffect(biometricPromptResult) {
        when (biometricPromptResult) {
            is BiometricPromptManager.BiometricPromptResult.AuthenticationError -> {
                val error =
                    (biometricPromptResult as BiometricPromptManager.BiometricPromptResult.AuthenticationError).errString
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }

            BiometricPromptManager.BiometricPromptResult.AuthenticationFailed -> {
                Toast.makeText(context, "Authentication Failed", Toast.LENGTH_SHORT).show()
            }

            BiometricPromptManager.BiometricPromptResult.AuthenticationSucceeded -> {
                onAuthenticated()
            }

            BiometricPromptManager.BiometricPromptResult.AuthenticationNotEnrolled -> {
                // 处理未注册生物识别的情况
                promptManager.createEnrollBiometricsIntent()?.let {
                    enrollBiometricsLauncher.launch(it)
                } ?: onAuthenticationNotEnrolled()
            }

            null -> { /* 初始状态，不处理 */
            }
        }
    }

    val title = stringResource(R.string.unlock_to_use_open_note)
    val negativeButtonText = stringResource(android.R.string.cancel)

    NumberLockScreen(
        storedPassword = storedPassword,
        isBiometricAuthEnabled = biometricAuthEnabled,
        isCreatingPassword = isCreatingPassword,
        onCreatingCanceled = onCreatingCanceled,
        onPassCreated = onPassCreated,
        onFingerprintClick = { promptManager.showBiometricPrompt(title, negativeButtonText) },
        onAuthenticated = onAuthenticated
    )
}
