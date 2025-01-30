package com.yangdai.opennote.presentation.component.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicSecureTextField
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yangdai.opennote.R

@Composable
fun AccountPane() {

    val webDAVTextState = rememberTextFieldState()
    var showWebDAVPassword by remember { mutableStateOf(false) }

    val dropboxTextState = rememberTextFieldState()
    var showDropboxPassword by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
                Text(text = "WebDAV " + stringResource(R.string.url))
            })

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = stringResource(R.string.account)
                )
            },
            headlineContent = {
                Text(text = "WebDAV " + stringResource(R.string.account))
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
                    state = webDAVTextState,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    decorator = { innerTextField ->
                        Box {
                            if (webDAVTextState.text.isEmpty()) {
                                Text(text = "WebDAV " + stringResource(id = R.string.pass))
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

        SettingsHeader(text = "Dropbox")

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Http,
                    contentDescription = stringResource(R.string.url)
                )
            },
            headlineContent = {
                Text(text = "Dropbox " + stringResource(R.string.url))
            })

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = stringResource(R.string.account)
                )
            },
            headlineContent = {
                Text(text = "Dropbox " + stringResource(R.string.account))
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
                    state = dropboxTextState,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    decorator = { innerTextField ->
                        Box {
                            if (dropboxTextState.text.isEmpty()) {
                                Text(text = "Dropbox " + stringResource(id = R.string.pass))
                            }
                            innerTextField()
                        }
                    },
                    textObfuscationMode = if (showDropboxPassword) TextObfuscationMode.Visible else TextObfuscationMode.RevealLastTyped
                )
            },
            trailingContent = {
                IconButton(
                    onClick = { showDropboxPassword = !showDropboxPassword }
                ) {
                    Icon(
                        imageVector = if (showDropboxPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = "Visibility toggle"
                    )
                }
            })
    }
}