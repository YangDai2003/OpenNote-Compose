package com.yangdai.opennote.presentation.screen

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import com.yangdai.opennote.presentation.component.setting.SettingsDetailPane
import com.yangdai.opennote.presentation.component.setting.SettingsListPane
import kotlinx.parcelize.Parcelize

@Parcelize
class SettingsItem(val index: Int, val titleId: Int) : Parcelable

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SettingsScreen(navigateUp: () -> Unit) {

    val navigator = rememberListDetailPaneScaffoldNavigator<SettingsItem>()

    BackHandler(navigator.canNavigateBack()) {
        navigator.navigateBack()
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                SettingsListPane(navigateUp = navigateUp) {
                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it)
                }
            }
        },
        detailPane = {
            AnimatedPane {
                navigator.currentDestination?.content?.let { item ->
                    SettingsDetailPane(selectedSettingsItem = item) {
                        if (navigator.canNavigateBack()) {
                            navigator.navigateBack()
                        }
                    }
                }
            }
        }
    )
}
