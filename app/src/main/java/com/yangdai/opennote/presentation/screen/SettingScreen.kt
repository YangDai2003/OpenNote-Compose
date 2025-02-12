package com.yangdai.opennote.presentation.screen

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.yangdai.opennote.presentation.component.setting.SettingsDetailPane
import com.yangdai.opennote.presentation.component.setting.SettingsListPane
import com.yangdai.opennote.presentation.navigation.INITIAL_OFFSET_FACTOR
import com.yangdai.opennote.presentation.navigation.sharedAxisXIn
import com.yangdai.opennote.presentation.navigation.sharedAxisXOut
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@Parcelize
class SettingsItem(val index: Int, val titleId: Int) : Parcelable

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SettingsScreen(navigateUp: () -> Unit) {

    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator<SettingsItem>()
    val coroutineScope = rememberCoroutineScope()

    BackHandler(scaffoldNavigator.canNavigateBack()) {
        coroutineScope.launch { scaffoldNavigator.navigateBack() }
    }

    ListDetailPaneScaffold(
        directive = scaffoldNavigator.scaffoldDirective,
        value = scaffoldNavigator.scaffoldValue,
        listPane = {
            AnimatedPane(
                enterTransition = sharedAxisXIn(initialOffsetX = { -(it * INITIAL_OFFSET_FACTOR).toInt() }),
                exitTransition = sharedAxisXOut(targetOffsetX = { -(it * INITIAL_OFFSET_FACTOR).toInt() }),
            ) {
                SettingsListPane(navigateUp = navigateUp) {
                    coroutineScope.launch {
                        scaffoldNavigator.navigateTo(
                            ListDetailPaneScaffoldRole.Detail,
                            it
                        )
                    }
                }
            }
        },
        detailPane = {
            AnimatedPane(
                enterTransition = sharedAxisXIn(initialOffsetX = { (it * INITIAL_OFFSET_FACTOR).toInt() }),
                exitTransition = sharedAxisXOut(targetOffsetX = { -(it * INITIAL_OFFSET_FACTOR).toInt() }),
            ) {
                scaffoldNavigator.currentDestination?.contentKey?.let { item ->
                    SettingsDetailPane(selectedSettingsItem = item) {
                        if (scaffoldNavigator.canNavigateBack()) {
                            coroutineScope.launch { scaffoldNavigator.navigateBack() }
                        }
                    }
                }
            }
        }
    )
}
