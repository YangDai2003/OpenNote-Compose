package com.yangdai.opennote.presentation.component.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Http
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yangdai.opennote.R

@Composable
fun AccountPane() {
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
            modifier = Modifier.clickable {

            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Http,
                    contentDescription = stringResource(R.string.url)
                )
            },
            headlineContent = {
                Text(text = "WebDAV " + stringResource(R.string.url))
            },
            supportingContent = {
                Text(text = "http://www.webdav.org/")
            })

        ListItem(
            modifier = Modifier.clickable {

            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = stringResource(R.string.account)
                )
            },
            headlineContent = {
                Text(text = "WebDAV " + stringResource(R.string.account))
            },
            supportingContent = {
                Text(text = "Admin123")
            })

        ListItem(
            modifier = Modifier.clickable {

            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Password,
                    contentDescription = stringResource(id = R.string.pass)
                )
            },
            headlineContent = {
                Text(text = "WebDAV " + stringResource(id = R.string.pass))
            },
            supportingContent = {
                Text(text = "******")
            })

        SettingsHeader(text = "Dropbox")

        ListItem(
            modifier = Modifier.clickable {

            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Http,
                    contentDescription = stringResource(R.string.url)
                )
            },
            headlineContent = {
                Text(text = "Dropbox " + stringResource(R.string.url))
            },
            supportingContent = {
                Text(text = "http://www.dropbox.com/")
            })

        ListItem(
            modifier = Modifier.clickable {

            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = stringResource(R.string.account)
                )
            },
            headlineContent = {
                Text(text = "Dropbox " + stringResource(R.string.account))
            },
            supportingContent = {
                Text(text = "Admin123")
            })

        ListItem(
            modifier = Modifier.clickable {

            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Password,
                    contentDescription = stringResource(id = R.string.pass)
                )
            },
            headlineContent = {
                Text(text = "Dropbox " + stringResource(id = R.string.pass))
            },
            supportingContent = {
                Text(text = "******")
            })
    }
}