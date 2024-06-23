package com.yangdai.opennote.presentation.component.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ScreenSearchDesktop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.window.core.layout.WindowWidthSizeClass
import com.yangdai.opennote.MainActivity
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.component.TopBarTitle
import com.yangdai.opennote.presentation.screen.SettingsItem
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDetailPane(
    sharedViewModel: SharedViewModel = hiltViewModel(LocalContext.current as MainActivity),
    selectedSettingsItem: SettingsItem,
    navigateBackToList: () -> Unit
) {

    val haptic = LocalHapticFeedback.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED)
                TopAppBar(
                    title = {
                        TopBarTitle(title = stringResource(selectedSettingsItem.titleId))
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors()
                        .copy(scrolledContainerColor = TopAppBarDefaults.largeTopAppBarColors().containerColor)
                )
            else
                LargeTopAppBar(
                    navigationIcon = {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navigateBackToList()
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Close"
                            )
                        }
                    },
                    title = {
                        TopBarTitle(title = stringResource(selectedSettingsItem.titleId))
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors()
                        .copy(scrolledContainerColor = TopAppBarDefaults.largeTopAppBarColors().containerColor)
                )
        }
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.Center
        ) {
            when (selectedSettingsItem.index) {
                0 -> {
                    StylePane(sharedViewModel = sharedViewModel)
                }

                1 -> {
                    DataPane(sharedViewModel = sharedViewModel)
                }

                2 -> {
                    AccountPane()
                }

                3 -> {
                    AboutPane()
                }

                else -> {
                    SettingsDetailPlaceHolder()
                }
            }
        }
    }
}

@Composable
fun SettingsDetailPlaceHolder() =
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {}
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = Icons.Outlined.ScreenSearchDesktop,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = ""
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(R.string.settings_hint))
    }