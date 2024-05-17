package com.yangdai.opennote.presentation.screen

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.theme.MaterialColors
import com.yangdai.opennote.presentation.util.BiometricPromptManager

@Composable
fun LoginOverlayScreen(
    onAuthenticated: () -> Unit,
    onAuthenticationNotEnrolled: () -> Unit
) {

    val activity = LocalContext.current as AppCompatActivity
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

    // 判断系统版本是否大于android 12
    val modifier: Modifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier
    } else {
        Modifier.background(MaterialTheme.colorScheme.surface)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {}
            .then(modifier),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.displaySmall.copy(
                brush = Brush.linearGradient(
                    colors = MaterialColors
                )
            )
        )

        val title = stringResource(R.string.app_name)
        val negativeButtonText = stringResource(android.R.string.cancel)

        OutlinedButton(onClick = {
            promptManager.showBiometricPrompt(
                title = title,
                negativeButtonText = negativeButtonText
            )
        }) {
            Text(text = stringResource(R.string.login))
        }
    }
}
