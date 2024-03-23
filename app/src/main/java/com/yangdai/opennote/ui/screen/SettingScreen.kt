package com.yangdai.opennote.ui.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowRight
import androidx.compose.material.icons.outlined.ArrowDropDown
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yangdai.opennote.Constants.APP_COLOR
import com.yangdai.opennote.Constants.APP_THEME
import com.yangdai.opennote.Constants.NEED_PASSWORD
import com.yangdai.opennote.Constants.IS_APP_IN_DARK_MODE
import com.yangdai.opennote.Constants.IS_DARK_SWITCH_ACTIVE
import com.yangdai.opennote.Constants.MASK_CLICK_X
import com.yangdai.opennote.Constants.MASK_CLICK_Y
import com.yangdai.opennote.Constants.SHOULD_FOLLOW_SYSTEM
import com.yangdai.opennote.R
import com.yangdai.opennote.ui.viewmodel.SettingScreenViewModel
import com.yangdai.opennote.data.di.AppModule
import com.yangdai.opennote.ui.component.RatingDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingScreenViewModel: SettingScreenViewModel = hiltViewModel(),
    navController: NavController
) {
    val context = LocalContext.current

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val modeOptions = listOf(
        stringResource(R.string.system_default),
        stringResource(R.string.light),
        stringResource(R.string.dark)
    )
    var modeExpended by rememberSaveable { mutableStateOf(false) }
    val initModeSelect = settingScreenViewModel.getInt(APP_THEME) ?: 0
    val (selectedMode, onModeSelected) = rememberSaveable {
        mutableStateOf(
            modeOptions[initModeSelect]
        )
    }

    val colorOptions = listOf(
        stringResource(R.string.dynamic_only_android_12),
        stringResource(R.string.purple),
        stringResource(R.string.blue),
        stringResource(R.string.green)
    )
    var colorExpended by rememberSaveable { mutableStateOf(false) }
    val initColorSelect = settingScreenViewModel.getInt(APP_COLOR) ?: 0
    val (selectedColor, onColorSelected) = rememberSaveable {
        mutableStateOf(
            colorOptions[initColorSelect]
        )
    }

    val initPasswordSelect = settingScreenViewModel.getBoolean(NEED_PASSWORD) ?: false
    var passwordChecked by rememberSaveable {
        mutableStateOf(initPasswordSelect)
    }

    var appInfoExpended by rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val isAppInDarkTheme by settingScreenViewModel.getFlow()
        .map { preferences ->
            preferences[booleanPreferencesKey(IS_APP_IN_DARK_MODE)] ?: false
        }
        .collectAsState(initial = false)

    fun switchDarkTheme() {
        scope.launch {
            settingScreenViewModel
                .getDataStore()
                .edit {
                    it[floatPreferencesKey(MASK_CLICK_X)] = 0f
                    it[floatPreferencesKey(MASK_CLICK_Y)] = 0f
                    it[booleanPreferencesKey(IS_DARK_SWITCH_ACTIVE)] = true
                }
        }
    }

    val isSystemDarkTheme = isSystemInDarkTheme()

    var showRatingDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
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
                headlineContent = {
                    Text(
                        text = stringResource(R.string.theme)
                    )
                },
                trailingContent = {
                    Icon(
                        imageVector = if (!modeExpended) Icons.AutoMirrored.Outlined.ArrowRight else Icons.Outlined.ArrowDropDown,
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

                                        val mode = settingScreenViewModel.getInt(APP_THEME)

                                        if (mode != index) {
                                            when (index) {
                                                0 -> {
                                                    if (isSystemDarkTheme != isAppInDarkTheme) {
                                                        switchDarkTheme()

                                                    }
                                                    settingScreenViewModel.putBoolean(
                                                        SHOULD_FOLLOW_SYSTEM, true
                                                    )
                                                }

                                                1 -> {
                                                    if (isAppInDarkTheme) {
                                                        switchDarkTheme()
                                                    }
                                                    settingScreenViewModel.putBoolean(
                                                        SHOULD_FOLLOW_SYSTEM,
                                                        false
                                                    )
                                                    settingScreenViewModel.putInt(APP_THEME, 1)
                                                }

                                                2 -> {
                                                    if (!isAppInDarkTheme) {
                                                        switchDarkTheme()
                                                    }
                                                    settingScreenViewModel.putBoolean(
                                                        SHOULD_FOLLOW_SYSTEM,
                                                        false
                                                    )
                                                    settingScreenViewModel.putInt(APP_THEME, 2)
                                                }
                                            }
                                        }
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
                                modifier = Modifier
                                    .padding(start = 16.dp)

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
                headlineContent = {
                    Text(text = stringResource(R.string.color))
                },
                trailingContent = {
                    Icon(
                        imageVector = if (!colorExpended) Icons.AutoMirrored.Outlined.ArrowRight else Icons.Outlined.ArrowDropDown,
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
                                        settingScreenViewModel.putInt(APP_COLOR, index)
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
                            imageVector = Icons.AutoMirrored.Outlined.ArrowRight,
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
                            settingScreenViewModel.putBoolean(NEED_PASSWORD, it)
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
                    TextButton(
                        onClick = {
                            clear(context)
                        },
                        colors = ButtonDefaults.textButtonColors().copy(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error
                        )
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
            ListItem(
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data =
                        Uri.parse("https://github.com/YangDai2003/OpenNote-Compose/blob/master/PRIVACY_POLICY.md")
                    context.startActivity(intent)
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.PrivacyTip,
                        contentDescription = "Privacy Policy"
                    )
                },
                headlineContent = { Text(text = stringResource(R.string.privacy_policy)) })
            ListItem(
                modifier = Modifier.clickable {
                    showRatingDialog = true
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.StarRate,
                        contentDescription = "Rate"
                    )
                }, headlineContent = { Text(text = stringResource(R.string.rate_this_app)) })
            ListItem(
                modifier = Modifier.clickable {
                    val sendIntent = Intent(Intent.ACTION_SEND)
                    sendIntent.setType("text/plain")
                    sendIntent.putExtra(Intent.EXTRA_TITLE, context.getString(R.string.app_name))
                    sendIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.shareContent))
                    context.startActivity(Intent.createChooser(sendIntent, null))
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
                        imageVector = if (!appInfoExpended) Icons.AutoMirrored.Outlined.ArrowRight else Icons.Outlined.ArrowDropDown,
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
                                imageVector = Icons.AutoMirrored.Outlined.ArrowRight,
                                contentDescription = "Arrow"
                            )
                        }
                    )
                }
            }
        }
        RatingDialog(
            showDialog = showRatingDialog,
            onDismissRequest = { showRatingDialog = false }) {

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