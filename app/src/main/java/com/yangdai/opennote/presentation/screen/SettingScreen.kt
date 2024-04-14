package com.yangdai.opennote.presentation.screen

import android.Manifest
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.ImportExport
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.PermDeviceInformation
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material.icons.outlined.TipsAndUpdates
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.analytics.FirebaseAnalytics
import com.yangdai.opennote.MainActivity
import com.yangdai.opennote.R
import com.yangdai.opennote.data.di.AppModule
import com.yangdai.opennote.presentation.component.AnimatedArrowIcon
import com.yangdai.opennote.presentation.component.FolderListSheet
import com.yangdai.opennote.presentation.component.ProgressDialog
import com.yangdai.opennote.presentation.component.RatingDialog
import com.yangdai.opennote.presentation.component.SelectableColorPlatte
import com.yangdai.opennote.presentation.component.SettingsHeader
import com.yangdai.opennote.presentation.component.WarningDialog
import com.yangdai.opennote.presentation.theme.DarkBlueColors
import com.yangdai.opennote.presentation.theme.DarkGreenColors
import com.yangdai.opennote.presentation.theme.DarkOrangeColors
import com.yangdai.opennote.presentation.theme.DarkPurpleColors
import com.yangdai.opennote.presentation.util.Constants.APP_COLOR
import com.yangdai.opennote.presentation.util.Constants.APP_THEME
import com.yangdai.opennote.presentation.util.Constants.FIREBASE
import com.yangdai.opennote.presentation.util.Constants.IS_APP_IN_DARK_MODE
import com.yangdai.opennote.presentation.util.Constants.IS_SWITCH_ACTIVE
import com.yangdai.opennote.presentation.util.Constants.MASK_CLICK_X
import com.yangdai.opennote.presentation.util.Constants.MASK_CLICK_Y
import com.yangdai.opennote.presentation.util.Constants.NEED_PASSWORD
import com.yangdai.opennote.presentation.util.Constants.SHOULD_FOLLOW_SYSTEM
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    sharedViewModel: SharedViewModel = hiltViewModel(LocalContext.current as MainActivity),
    navigateUp: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val customTabsIntent = CustomTabsIntent.Builder()
        .setShowTitle(true)
        .build()

    val modeOptions = listOf(
        stringResource(R.string.system_default),
        stringResource(R.string.light),
        stringResource(R.string.dark)
    )
    var modeExpended by rememberSaveable { mutableStateOf(false) }
    val initModeSelect = sharedViewModel.getInt(APP_THEME) ?: 0
    val (selectedMode, onModeSelected) = rememberSaveable {
        mutableStateOf(
            modeOptions[initModeSelect]
        )
    }

    var colorExpended by rememberSaveable { mutableStateOf(false) }
    val initColorSelect = sharedViewModel.getInt(APP_COLOR) ?: 0
    var selectedScheme by rememberSaveable { mutableIntStateOf(initColorSelect) }
    val colorSchemes = listOf(
        Pair(1, DarkPurpleColors),
        Pair(2, DarkBlueColors),
        Pair(3, DarkGreenColors),
        Pair(4, DarkOrangeColors)
    )
    LaunchedEffect(selectedScheme) {
        sharedViewModel.putPreferenceValue(APP_COLOR, selectedScheme)
    }

    val initPasswordSelect = sharedViewModel.getBoolean(NEED_PASSWORD) ?: false
    var passwordChecked by rememberSaveable {
        mutableStateOf(initPasswordSelect)
    }

    val initFirebaseSelect = sharedViewModel.getBoolean(FIREBASE) ?: false
    var firebaseEnabled by rememberSaveable {
        mutableStateOf(initFirebaseSelect)
    }

    var appInfoExpended by rememberSaveable { mutableStateOf(false) }

    val isAppInDarkTheme by sharedViewModel.preferencesFlow()
        .map { preferences ->
            preferences[booleanPreferencesKey(IS_APP_IN_DARK_MODE)] ?: false
        }
        .collectAsState(initial = false)

    fun switchTheme() {
        scope.launch {
            sharedViewModel
                .getDataStore()
                .edit {
                    it[floatPreferencesKey(MASK_CLICK_X)] = 0f
                    it[floatPreferencesKey(MASK_CLICK_Y)] = 0f
                    it[booleanPreferencesKey(IS_SWITCH_ACTIVE)] = true
                }
        }
    }

    val isSystemDarkTheme = isSystemInDarkTheme()

    var showRatingDialog by rememberSaveable { mutableStateOf(false) }
    var showWarningDialog by rememberSaveable { mutableStateOf(false) }
    var dataActionExpended by rememberSaveable { mutableStateOf(false) }
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val listState by sharedViewModel.listStateFlow.collectAsStateWithLifecycle()
    val actionState by sharedViewModel.dataActionState.collectAsStateWithLifecycle()
    var folderId: Long? = null
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uriList ->
        if (uriList.isNotEmpty()) {
            val contentResolver = context.contentResolver
            sharedViewModel.addNotes(folderId, uriList, contentResolver)
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        navigateUp()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = { Text(text = stringResource(R.string.settings)) },
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

            SettingsHeader(text = stringResource(R.string.style))

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
                    AnimatedArrowIcon(expended = modeExpended)
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

                                        val mode = sharedViewModel.getInt(APP_THEME)

                                        if (mode != index) {
                                            when (index) {
                                                0 -> {
                                                    if (isSystemDarkTheme != isAppInDarkTheme) {
                                                        switchTheme()
                                                    } else {
                                                        sharedViewModel.putPreferenceValue(
                                                            APP_THEME,
                                                            0
                                                        )
                                                    }
                                                    sharedViewModel.putPreferenceValue(
                                                        SHOULD_FOLLOW_SYSTEM, true
                                                    )
                                                }

                                                1 -> {
                                                    if (isAppInDarkTheme) {
                                                        switchTheme()
                                                    }
                                                    sharedViewModel.putPreferenceValue(
                                                        SHOULD_FOLLOW_SYSTEM,
                                                        false
                                                    )
                                                    sharedViewModel.putPreferenceValue(APP_THEME, 1)
                                                }

                                                2 -> {
                                                    if (!isAppInDarkTheme) {
                                                        switchTheme()
                                                    }
                                                    sharedViewModel.putPreferenceValue(
                                                        SHOULD_FOLLOW_SYSTEM,
                                                        false
                                                    )
                                                    sharedViewModel.putPreferenceValue(APP_THEME, 2)
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
                                onClick = null
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
                    AnimatedArrowIcon(expended = colorExpended)
                })

            AnimatedVisibility(visible = colorExpended) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clickable { selectedScheme = 0 },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedScheme == 0,
                                onClick = null
                            )
                            Text(
                                text = stringResource(R.string.dynamic_only_android_12),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
                    ) {
                        colorSchemes.forEach { colorSchemePair ->
                            SelectableColorPlatte(
                                selected = selectedScheme == colorSchemePair.first,
                                colorScheme = colorSchemePair.second
                            ) {
                                selectedScheme = colorSchemePair.first
                            }
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

            SettingsHeader(text = stringResource(R.string.data))

            ListItem(
                modifier = Modifier.clickable {
                    dataActionExpended = !dataActionExpended
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.ImportExport,
                        contentDescription = "ImportExport"
                    )
                },
                headlineContent = { Text(text = stringResource(R.string.ImportExport)) },
                trailingContent = {
                    AnimatedArrowIcon(expended = dataActionExpended)
                }
            )
            AnimatedVisibility(visible = dataActionExpended) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    ListItem(
                        modifier = Modifier.clickable {
                            if (!hasStoragePermissions(context)) {
                                ActivityCompat.requestPermissions(
                                    context as Activity, STORAGE_PERMISSIONS, 0
                                )
                            } else {
                                showBottomSheet = true
                            }
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Outlined.FileDownload,
                                contentDescription = "Import"
                            )
                        },
                        headlineContent = { Text(text = stringResource(R.string.import_files)) }
                    )
                    ListItem(
                        modifier = Modifier.clickable {
                            Toast.makeText(
                                context,
                                context.getString(R.string.coming_soon),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Outlined.FileUpload,
                                contentDescription = "Export"
                            )
                        },
                        headlineContent = { Text(text = stringResource(R.string.export_files)) }
                    )
                }
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
                            val keyguardManager =
                                context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                            if (keyguardManager.isKeyguardSecure) {
                                passwordChecked = it
                                sharedViewModel.putPreferenceValue(NEED_PASSWORD, it)
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.no_password_set),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
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
                headlineContent = { Text(text = stringResource(R.string.reset_database)) },
                trailingContent = {
                    TextButton(
                        onClick = {
                            showWarningDialog = true
                        },
                        colors = ButtonDefaults.textButtonColors().copy(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(text = stringResource(id = R.string.reset))
                    }
                })


            SettingsHeader(text = stringResource(R.string.other))

            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Analytics,
                        contentDescription = "Firebase"
                    )
                },
                headlineContent = { Text(text = stringResource(R.string.firebase)) },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.firebase_description),
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                },
                trailingContent = {
                    Switch(
                        checked = firebaseEnabled,
                        onCheckedChange = {
                            firebaseEnabled = it
                            sharedViewModel.putPreferenceValue(FIREBASE, it)
                            FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(it)
                        }
                    )
                })

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
                    AnimatedArrowIcon(expended = appInfoExpended)
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
                            customTabsIntent.launchUrl(
                                context,
                                Uri.parse("https://github.com/YangDai2003/OpenNote-Compose/blob/master/Guide.md")
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Outlined.TipsAndUpdates,
                                contentDescription = "Guide"
                            )
                        },
                        headlineContent = {
                            Text(text = stringResource(R.string.guide))
                        }
                    )
                    ListItem(
                        modifier = Modifier.clickable {
                            customTabsIntent.launchUrl(
                                context,
                                Uri.parse("https://github.com/YangDai2003/OpenNote-Compose/blob/master/PRIVACY_POLICY.md")
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Outlined.PrivacyTip,
                                contentDescription = "Privacy Policy"
                            )
                        },
                        headlineContent = { Text(text = stringResource(R.string.privacy_policy)) })
                }
            }
        }
        RatingDialog(
            showDialog = showRatingDialog,
            onDismissRequest = { showRatingDialog = false }) {
            if (it > 3) {
                customTabsIntent.launchUrl(
                    context,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.yangdai.opennote")
                )
            } else {
                // 获取当前应用的版本号
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val appVersion = packageInfo.versionName
                val deviceModel = Build.MODEL
                val systemVersion = Build.VERSION.SDK_INT

                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("dy15800837435@gmail.com"))
                    putExtra(Intent.EXTRA_SUBJECT, "Feedback - Open Note")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Version: $appVersion\nDevice: $deviceModel\nSystem: $systemVersion\n"
                    )
                }
                context.startActivity(Intent.createChooser(emailIntent, "Feedback (E-mail)"))
            }
        }
        WarningDialog(
            showDialog = showWarningDialog,
            message = stringResource(R.string.reset_database_warning),
            onDismissRequest = { showWarningDialog = false }) {
            scope.clear(context.applicationContext)
        }
        ProgressDialog(isLoading = actionState.loading, progress = actionState.progress) {
            sharedViewModel.cancelAddNotes()

        }
        if (showBottomSheet) {
            FolderListSheet(
                hint = stringResource(R.string.select_destination_folder),
                oFolderId = folderId,
                folders = listState.folders.toImmutableList(),
                sheetState = sheetState,
                onDismissRequest = { showBottomSheet = false },
                onCloseClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showBottomSheet = false
                        }
                    }
                }) {
                folderId = it
                importLauncher.launch(arrayOf("text/*"))
            }
        }
    }
}

fun hasStoragePermissions(
    context: Context
): Boolean {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
        return true
    }
    return STORAGE_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}

val STORAGE_PERMISSIONS = arrayOf(
    Manifest.permission.READ_EXTERNAL_STORAGE
)

private fun CoroutineScope.clear(context: Context) {
    launch(Dispatchers.IO) {
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
