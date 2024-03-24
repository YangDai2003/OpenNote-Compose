package com.yangdai.opennote.ui.util

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class BiometricPromptManager(
    private val activity: AppCompatActivity
) {
    private val resultChannel = Channel<BiometricPromptResult>()
    val promptResult = resultChannel.receiveAsFlow()
    fun showBiometricPrompt(
        title: String,
        subtitle: String
    ) {
        val manager = BiometricManager.from(activity)
        val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL
        else
            BIOMETRIC_STRONG or BIOMETRIC_WEAK

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators)

        when (manager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                BiometricPrompt(
                    activity,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            resultChannel.trySend(BiometricPromptResult.AuthenticationSucceeded)

                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            resultChannel.trySend(BiometricPromptResult.AuthenticationFailed)

                        }

                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                        ) {
                            super.onAuthenticationError(errorCode, errString)
                            resultChannel.trySend(
                                BiometricPromptResult.AuthenticationError(
                                    errString.toString()
                                )
                            )

                        }
                    }).authenticate(promptInfo.build())
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                resultChannel.trySend(BiometricPromptResult.FeatureUnavailable)
                return
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                resultChannel.trySend(BiometricPromptResult.HardwareUnavailable)
                return
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                resultChannel.trySend(BiometricPromptResult.AuthenticationNotEnrolled)
                return
            }

            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                resultChannel.trySend(BiometricPromptResult.FeatureUnavailable)
                return
            }

            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                resultChannel.trySend(BiometricPromptResult.FeatureUnavailable)
                return
            }

            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                resultChannel.trySend(BiometricPromptResult.FeatureUnavailable)
                return
            }
        }
    }

    sealed interface BiometricPromptResult {
        data object HardwareUnavailable : BiometricPromptResult
        data object FeatureUnavailable : BiometricPromptResult
        data object AuthenticationFailed : BiometricPromptResult
        data class AuthenticationError(val errString: String) : BiometricPromptResult
        data object AuthenticationSucceeded : BiometricPromptResult
        data object AuthenticationNotEnrolled : BiometricPromptResult
    }
}