package com.yangdai.opennote.presentation.component.setting

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.window.core.layout.WindowWidthSizeClass
import com.yangdai.opennote.MainActivity
import com.yangdai.opennote.presentation.component.TopBarTitle
import com.yangdai.opennote.presentation.glance.NoteListWidgetReceiver
import com.yangdai.opennote.presentation.screen.SettingsItem
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDetailPane(
    sharedViewModel: SharedViewModel = hiltViewModel(LocalActivity.current as MainActivity),
    selectedSettingsItem: SettingsItem,
    navigateBackToList: () -> Unit
) {
    val activity = LocalActivity.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isExpended =
        currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
    val scrollBehavior =
        if (isExpended) TopAppBarDefaults.pinnedScrollBehavior() else TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val modifier =
        remember(scrollBehavior) { Modifier.nestedScroll(scrollBehavior.nestedScrollConnection) }
    Scaffold(
        modifier = modifier,
        topBar = {
            if (isExpended)
                TopAppBar(
                    title = {
                        TopBarTitle(title = stringResource(selectedSettingsItem.titleId))
                    },
                    actions = {
                        if (selectedSettingsItem.index == 4)
                            IconButton(onClick = {
                                activity?.requestShowKeyboardShortcuts()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                    contentDescription = "Helper"
                                )
                            }
                        else if (selectedSettingsItem.index == 8)
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
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors()
                        .copy(scrolledContainerColor = TopAppBarDefaults.topAppBarColors().containerColor)
                )
            else
                LargeTopAppBar(
                    navigationIcon = {
                        val haptic = LocalHapticFeedback.current
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
                    actions = {
                        if (selectedSettingsItem.index == 4)
                            IconButton(onClick = {
                                activity?.requestShowKeyboardShortcuts()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                    contentDescription = "Helper"
                                )
                            }
                        else if (selectedSettingsItem.index == 8)
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
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors()
                        .copy(scrolledContainerColor = TopAppBarDefaults.topAppBarColors().containerColor)
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
                    CloudPane()
                }

                3 -> {
                    AboutPane()
                }

                4 -> {
                    EditorPane(sharedViewModel = sharedViewModel)
                }

                5 -> {
                    TemplatePane(sharedViewModel = sharedViewModel)
                }

                6 -> {
                    SecurityPane(sharedViewModel = sharedViewModel)
                }

                7 -> {
                    ListPane(sharedViewModel = sharedViewModel)
                }

                8 -> {
                    WidgetPane()
                }
            }
        }
    }
}
