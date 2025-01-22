package com.yangdai.opennote.presentation.component.setting

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CloudCircle
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.ModeEdit
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PermDeviceInformation
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.component.TopBarTitle
import com.yangdai.opennote.presentation.glance.NoteListWidgetReceiver
import com.yangdai.opennote.presentation.screen.SettingsItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsListPane(
    navigateUp: () -> Unit,
    navigateToDetail: (SettingsItem) -> Unit
) {

    val context = LocalContext.current

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeTopAppBar(
                navigationIcon = {
                    IconButton(onClick = navigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(id = R.string.navigate_back)
                        )
                    }
                },
                title = {
                    TopBarTitle(title = stringResource(id = R.string.settings))
                },
                actions = {
                    val scope = rememberCoroutineScope()
                    IconButton(onClick = {
                        scope.launch {
                            GlanceAppWidgetManager(context).requestPinGlanceAppWidget(
                                NoteListWidgetReceiver::class.java
                            )
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Widgets,
                            contentDescription = "Widgets"
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors()
                    .copy(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->

        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            SettingsSection {
                SettingItem(
                    modifier = Modifier.clickable {
                        navigateToDetail(SettingsItem(0, R.string.style))
                    },
                    headlineText = stringResource(R.string.style),
                    supportingText = stringResource(R.string.dark_mode) + "  •  " + stringResource(R.string.color),
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = "Style"
                        )
                    }
                )

                SettingsSectionDivider()

                SettingItem(
                    modifier = Modifier.clickable {
                        navigateToDetail(SettingsItem(4, R.string.editor))
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.ModeEdit,
                            contentDescription = "Editor"
                        )
                    },
                    headlineText = stringResource(R.string.editor),
                    supportingText = stringResource(R.string.editor_description)
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    SettingsSectionDivider()

                    SettingItem(
                        modifier = Modifier.clickable {

                            try {
                                val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
                                intent.setData(
                                    Uri.fromParts(
                                        "package",
                                        context.packageName,
                                        null
                                    )
                                )
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                try {
                                    val intent =
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    intent.setData(
                                        Uri.fromParts(
                                            "package", context.packageName, null
                                        )
                                    )
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                }
                            }

                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = "Language"
                            )
                        },
                        headlineText = stringResource(R.string.language),
                        supportingText = stringResource(R.string.language_description)
                    )
                }
            }

            SettingsSection {
                SettingItem(
                    modifier = Modifier.clickable {
                        navigateToDetail(SettingsItem(1, R.string.data_security))
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.SdStorage,
                            contentDescription = "Storage"
                        )
                    },
                    headlineText = stringResource(R.string.data_security),
                    supportingText = stringResource(R.string.backup) + "  •  " + stringResource(R.string.recovery) + "  •  " + stringResource(
                        R.string.password
                    )
                )

                SettingsSectionDivider()

                SettingItem(
                    modifier = Modifier.clickable {
                        navigateToDetail(SettingsItem(2, R.string.account_cloud))
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.CloudCircle,
                            contentDescription = "Account"
                        )
                    },
                    headlineText = stringResource(R.string.account_cloud),
                    supportingText = "WebDAV" + "  •  " + "Dropbox" + "  •  " + stringResource(R.string.sync)
                )
            }

            SettingsSection {
                SettingItem(
                    modifier = Modifier.clickable {
                        navigateToDetail(SettingsItem(3, R.string.app_info))
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.PermDeviceInformation,
                            contentDescription = "App Info"
                        )
                    },
                    headlineText = stringResource(R.string.app_info),
                    supportingText = stringResource(R.string.version) + "  •  " + stringResource(R.string.guide) + "  •  " + stringResource(
                        R.string.privacy_policy
                    )
                )
            }
        }
    }
}
