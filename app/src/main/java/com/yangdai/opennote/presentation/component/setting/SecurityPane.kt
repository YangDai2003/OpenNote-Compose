package com.yangdai.opennote.presentation.component.setting

import android.app.KeyguardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.util.Constants
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel

@Composable
fun SecurityPane(sharedViewModel: SharedViewModel) {

    val context = LocalContext.current
    val settingsState by sharedViewModel.settingsStateFlow.collectAsStateWithLifecycle()
    val isCreatingPass by sharedViewModel.isCreatingPassword.collectAsStateWithLifecycle()
    val hapticFeedback = LocalHapticFeedback.current

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = if (settingsState.isScreenProtected) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = "Visibility"
                )
            },
            headlineContent = { Text(text = stringResource(R.string.screen_protection)) },
            trailingContent = {
                Switch(
                    checked = settingsState.isScreenProtected,
                    onCheckedChange = { checked ->
                        if (checked)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        else
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
                        sharedViewModel.putPreferenceValue(
                            Constants.Preferences.IS_SCREEN_PROTECTED,
                            checked
                        )
                    }
                )
            },
            supportingContent = {
                Text(
                    text = stringResource(R.string.screen_protection_detail)
                )
            }
        )

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = if (settingsState.password.isEmpty()) Icons.Outlined.LockOpen else Icons.Outlined.Lock,
                    contentDescription = "Password"
                )
            },
            headlineContent = { Text(text = stringResource(R.string.password)) },
            trailingContent = {
                Switch(
                    checked = settingsState.password.isNotEmpty() || isCreatingPass,
                    onCheckedChange = { checked ->
                        if (checked) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            if (settingsState.password.isEmpty()) {
                                sharedViewModel.isCreatingPassword.value = true
                            }
                        } else {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
                            sharedViewModel.putPreferenceValue(
                                Constants.Preferences.PASSWORD,
                                ""
                            )
                            sharedViewModel.putPreferenceValue(
                                Constants.Preferences.BIOMETRIC_AUTH_ENABLED,
                                false
                            )
                            sharedViewModel.isCreatingPassword.value = false
                            sharedViewModel.authenticated.value = false
                        }
                    }
                )
            },
            supportingContent = {
                Text(
                    text = stringResource(R.string.password_description)
                )
            }
        )

        AnimatedVisibility(settingsState.password.isNotEmpty()) {
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Fingerprint,
                        contentDescription = "Fingerprint"
                    )
                },
                headlineContent = { Text(text = stringResource(R.string.biometric)) },
                trailingContent = {
                    Switch(
                        checked = settingsState.biometricAuthEnabled,
                        onCheckedChange = { checked ->
                            if (checked)
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            else
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
                            val keyguardManager =
                                context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                            if (keyguardManager.isKeyguardSecure) {
                                sharedViewModel.putPreferenceValue(
                                    Constants.Preferences.BIOMETRIC_AUTH_ENABLED,
                                    checked
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.no_password_set),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )
                }
            )
        }

        Spacer(Modifier.navigationBarsPadding())
    }
}