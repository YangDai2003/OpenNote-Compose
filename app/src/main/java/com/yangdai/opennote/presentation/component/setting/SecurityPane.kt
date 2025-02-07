package com.yangdai.opennote.presentation.component.setting

import android.app.KeyguardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.util.Constants
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel

@Composable
fun SecurityPane(sharedViewModel: SharedViewModel) {

    val context = LocalContext.current
    val settingsState by sharedViewModel.settingsStateFlow.collectAsStateWithLifecycle()

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
                    imageVector = Icons.Outlined.Fingerprint,
                    contentDescription = "Password"
                )
            },
            headlineContent = { Text(text = stringResource(R.string.password)) },
            trailingContent = {
                Switch(
                    checked = settingsState.needPassword,
                    onCheckedChange = { checked ->
                        val keyguardManager =
                            context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                        if (keyguardManager.isKeyguardSecure) {
                            sharedViewModel.putPreferenceValue(
                                Constants.Preferences.NEED_PASSWORD,
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
            },
            supportingContent = {
                Text(
                    text = stringResource(R.string.password_description)
                )
            }
        )
    }
}