package com.yangdai.opennote.presentation.util

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.AuthenticationRequest
import androidx.biometric.AuthenticationResult
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.registerForAuthenticationResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class BiometricPromptManager(activity: AppCompatActivity) {
    private val resultChannel = Channel<BiometricPromptResult>()
    val promptResult = resultChannel.receiveAsFlow()

    val requestAuthentication = activity.registerForAuthenticationResult(
        onAuthFailedCallback = {
            resultChannel.trySend(BiometricPromptResult.AuthenticationFailed)
        }
    ) { result: AuthenticationResult ->
        when (result) {
            is AuthenticationResult.Error -> {
                if (result.errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS) {
                    resultChannel.trySend(BiometricPromptResult.AuthenticationNotEnrolled)
                } else {
                    resultChannel.trySend(
                        BiometricPromptResult.AuthenticationError(
                            "${"${result.errorCode} "}${result.errString}"
                        )
                    )
                }
            }

            is AuthenticationResult.Success -> {
                resultChannel.trySend(BiometricPromptResult.AuthenticationSucceeded)
            }
        }
    }

    fun showBiometricPrompt(title: String, negativeButtonText: String) {
        val authRequest = AuthenticationRequest.biometricRequest(
            title = title,
            authFallback = AuthenticationRequest.Biometric.Fallback.NegativeButton(
                negativeButtonText = negativeButtonText
            )
        ) {
            setMinStrength(AuthenticationRequest.Biometric.Strength.Class2)
            setIsConfirmationRequired(false)
        }
        requestAuthentication.launch(authRequest)
    }

    fun createEnrollBiometricsIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                putExtra(
                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
                )
            }
        } else null
    }

    sealed interface BiometricPromptResult {
        data object AuthenticationFailed : BiometricPromptResult
        data class AuthenticationError(val errString: String) : BiometricPromptResult
        data object AuthenticationSucceeded : BiometricPromptResult
        data object AuthenticationNotEnrolled : BiometricPromptResult
    }
}