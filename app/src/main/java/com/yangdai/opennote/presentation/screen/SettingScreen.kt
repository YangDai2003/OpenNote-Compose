package com.yangdai.opennote.presentation.screen

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

import com.yangdai.opennote.presentation.component.SettingsDetailPane
import com.yangdai.opennote.presentation.component.SettingsListPane

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SettingsScreen(navigateUp: () -> Unit) {

    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()
    var selectedListItem by rememberSaveable { mutableStateOf(Pair(-1, -1)) }

    NavigableListDetailPaneScaffold(
        navigator = navigator,
        listPane = {
            AnimatedPane {
                SettingsListPane(navigateUp = navigateUp) {
                    selectedListItem = it
                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                }
            }
        },
        detailPane = {
            AnimatedPane {
                SettingsDetailPane(selectedListItem = selectedListItem) {
                    if (navigator.canNavigateBack()) {
                        navigator.navigateBack()
                    } else {
                        selectedListItem = Pair(-1, -1)
                    }
                }
            }
        }
    )
}
