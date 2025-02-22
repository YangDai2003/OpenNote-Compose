package com.yangdai.opennote.presentation.component.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Http
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.yangdai.opennote.R

@Composable
fun CloudPane() {

    val webDAVUrlState = rememberTextFieldState()
    val webDAVAccountState = rememberTextFieldState()
    val webDAVPasswordState = rememberTextFieldState()
    var showWebDAVPassword by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.WarningAmber,
                    contentDescription = "Notification",
                    tint = MaterialTheme.colorScheme.error
                )
            },
            headlineContent = { Text(text = stringResource(R.string.the_feature_is_still_under_construction_and_is_still_unavailable)) },
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.errorContainer)
        )

        SettingsHeader(text = "WebDAV")

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Http,
                    contentDescription = stringResource(R.string.url)
                )
            },
            headlineContent = {
                BasicTextField(
                    modifier = Modifier.fillMaxWidth(),
                    state = webDAVUrlState,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    keyboardOptions = KeyboardOptions(
                        autoCorrectEnabled = true,
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done
                    ),
                    onKeyboardAction = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    decorator = { innerTextField ->
                        Box {
                            if (webDAVUrlState.text.isEmpty()) {
                                Text(
                                    text = stringResource(id = R.string.url),
                                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            })

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = stringResource(R.string.username)
                )
            },
            headlineContent = {
                BasicTextField(
                    modifier = Modifier.fillMaxWidth(),
                    state = webDAVAccountState,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    keyboardOptions = KeyboardOptions(
                        autoCorrectEnabled = true,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    onKeyboardAction = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    decorator = { innerTextField ->
                        Box {
                            if (webDAVAccountState.text.isEmpty()) {
                                Text(
                                    text = stringResource(id = R.string.username),
                                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            })

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Password,
                    contentDescription = stringResource(id = R.string.pass)
                )
            },
            headlineContent = {
                BasicSecureTextField(
                    modifier = Modifier.fillMaxWidth(),
                    state = webDAVPasswordState,
                    keyboardOptions = KeyboardOptions(
                        autoCorrectEnabled = true,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    onKeyboardAction = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    decorator = { innerTextField ->
                        Box {
                            if (webDAVPasswordState.text.isEmpty()) {
                                Text(
                                    text = stringResource(id = R.string.pass),
                                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            innerTextField()
                        }
                    },
                    textObfuscationMode = if (showWebDAVPassword) TextObfuscationMode.Visible else TextObfuscationMode.RevealLastTyped
                )
            },
            trailingContent = {
                IconButton(
                    onClick = { showWebDAVPassword = !showWebDAVPassword }
                ) {
                    Icon(
                        imageVector = if (showWebDAVPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = "Visibility toggle"
                    )
                }
            })

        TextButton(
            colors = ButtonDefaults.textButtonColors()
                .copy(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            onClick = {

            }
        ) {
            Text(text = "Test connection")
        }
    }
}