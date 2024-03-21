package com.yangdai.opennote.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.PermDeviceInformation
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import com.yangdai.opennote.R
import com.yangdai.opennote.data.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToMain: () -> Unit
) {
    val context = LocalContext.current

    val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val modeOptions = listOf(
        stringResource(R.string.system_default),
        stringResource(R.string.light), stringResource(R.string.dark)
    )
    var modeExpended by remember { mutableStateOf(false) }
    val initModeSelect = defaultSharedPreferences.getInt("APP_MODE", 0)
    val (selectedMode, onModeSelected) = remember {
        mutableStateOf(
            modeOptions[initModeSelect]
        )
    }

    val colorOptions = listOf(
        stringResource(R.string.dynamic_only_android_12),
        stringResource(R.string.purple), stringResource(R.string.blue),
        stringResource(R.string.green)
    )
    var colorExpended by remember { mutableStateOf(false) }
    val initColorSelect = defaultSharedPreferences.getInt("APP_COLOR", 0)
    val (selectedColor, onColorSelected) = remember {
        mutableStateOf(
            colorOptions[initColorSelect]
        )
    }

    val initPasswordSelect = defaultSharedPreferences.getBoolean("APP_PASSWORD", false)
    var passwordChecked by remember {
        mutableStateOf(initPasswordSelect)
    }

    var appInfoExpended by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateToMain) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = { Text(text = stringResource(R.string.settings)) },
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = TopAppBarDefaults.largeTopAppBarColors().navigationIconContentColor,
                    titleContentColor = TopAppBarDefaults.largeTopAppBarColors().titleContentColor,
                    actionIconContentColor = TopAppBarDefaults.largeTopAppBarColors().actionIconContentColor
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = stringResource(R.string.style),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            ListItem(
                modifier = Modifier.clickable {
                    modeExpended = !modeExpended
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.DarkMode,
                        contentDescription = "Theme"
                    )
                },
                headlineContent = { Text(text = stringResource(R.string.theme)) },
                trailingContent = {
                    Icon(
                        imageVector = if (!modeExpended) Icons.AutoMirrored.Filled.ArrowRight else Icons.Default.ArrowDropDown,
                        contentDescription = "Arrow"
                    )
                })

            AnimatedVisibility(visible = modeExpended) {

                Column(Modifier.selectableGroup()) {
                    modeOptions.forEachIndexed { index, text ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (text == selectedMode),
                                    onClick = {
                                        onModeSelected(text)
                                        defaultSharedPreferences
                                            .edit()
                                            .putInt("APP_MODE", index)
                                            .apply()
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(start = 32.dp, end = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (text == selectedMode),
                                onClick = null // null recommended for accessibility with screenreaders
                            )
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            }

            ListItem(
                modifier = Modifier.clickable {
                    colorExpended = !colorExpended
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = "Color"
                    )
                },
                headlineContent = { Text(text = stringResource(R.string.color)) },
                trailingContent = {
                    Icon(
                        imageVector = if (!colorExpended) Icons.AutoMirrored.Filled.ArrowRight else Icons.Default.ArrowDropDown,
                        contentDescription = "Arrow"
                    )
                })

            AnimatedVisibility(visible = colorExpended) {

                Column(Modifier.selectableGroup()) {
                    colorOptions.forEachIndexed { index, text ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (text == selectedColor),
                                    onClick = {
                                        onColorSelected(text)
                                        defaultSharedPreferences
                                            .edit()
                                            .putInt("APP_COLOR", index)
                                            .apply()
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(start = 32.dp, end = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (text == selectedColor),
                                onClick = null // null recommended for accessibility with screenreaders
                            )
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ListItem(
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
                        } catch (e: Exception) {
                            try {
                                val intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                intent.setData(
                                    Uri.fromParts(
                                        "package", context.packageName, null
                                    )
                                )
                                context.startActivity(intent)
                            } catch (ignored: Exception) {
                            }
                        }

                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.Language,
                            contentDescription = "Language"
                        )
                    },
                    headlineContent = { Text(text = stringResource(R.string.language)) },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                            contentDescription = "Arrow"
                        )
                    })
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = stringResource(R.string.data),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Password,
                        contentDescription = "Password"
                    )
                },
                headlineContent = { Text(text = stringResource(R.string.password)) },
                trailingContent = {
                    Switch(
                        checked = passwordChecked,
                        onCheckedChange = {
                            passwordChecked = it
                            defaultSharedPreferences
                                .edit()
                                .putBoolean("APP_PASSWORD", it)
                                .apply()
                        }
                    )
                })
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.CleaningServices,
                        contentDescription = "Clear"
                    )
                },
                headlineContent = { Text(text = stringResource(R.string.delete_all_notes)) },
                trailingContent = {
                    ElevatedButton(
                        onClick = {
                            clear(context)
                        },
                        colors = ButtonDefaults.buttonColors()
                            .copy(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(text = stringResource(id = R.string.delete))
                    }
                })


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = stringResource(R.string.other),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            ListItem(leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.PrivacyTip,
                    contentDescription = "Privacy Policy"
                )
            }, headlineContent = { Text(text = stringResource(R.string.privacy_policy)) })
            ListItem(leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.StarRate,
                    contentDescription = "Rate"
                )
            }, headlineContent = { Text(text = stringResource(R.string.rate_this_app)) })
            ListItem(
                modifier = Modifier.clickable {
                    val sendIntent = Intent(Intent.ACTION_SEND)
                    sendIntent.setType("text/plain")
                    sendIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.shareContent))
                    context.startActivity(Intent.createChooser(sendIntent, ""))
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.IosShare,
                        contentDescription = "Share"
                    )
                }, headlineContent = { Text(text = stringResource(R.string.share_this_app)) })

            ListItem(
                modifier = Modifier.clickable {
                    appInfoExpended = !appInfoExpended
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.PermDeviceInformation,
                        contentDescription = "App Info"
                    )
                },
                headlineContent = { Text(text = stringResource(R.string.app_info)) },
                trailingContent = {
                    Icon(
                        imageVector = if (!appInfoExpended) Icons.AutoMirrored.Filled.ArrowRight else Icons.Default.ArrowDropDown,
                        contentDescription = "Arrow"
                    )
                })

            AnimatedVisibility(visible = appInfoExpended) {

                Column(Modifier.padding(horizontal = 16.dp)) {
                    ListItem(
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Outlined.Numbers,
                                contentDescription = "Version"
                            )
                        },
                        headlineContent = {
                            Text(text = stringResource(R.string.version))
                        },
                        trailingContent = {
                            val packageInfo =
                                context.packageManager.getPackageInfo(context.packageName, 0)
                            val version = packageInfo.versionName
                            Text(text = version)
                        }
                    )
                    ListItem(
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data =
                                Uri.parse("https://github.com/YangDai2003/OpenNote-Compose")
                            context.startActivity(intent)
                        },
                        leadingContent = {
                            Icon(
                                painter = painterResource(id = R.drawable.github),
                                contentDescription = "GitHub"
                            )
                        },
                        headlineContent = {
                            Text(text = stringResource(R.string.github))
                        },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                                contentDescription = "Arrow"
                            )
                        }
                    )
                }
            }
        }
    }
}

private fun clear(context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            AppModule.provideNoteDatabase(context).clearAllTables()
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    context.getString(R.string.please_relaunch_the_app), Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            // 处理数据库操作的异常情况
            Log.e("DATABASE", "Error clearing database: ${e.message}")
        }
    }
}