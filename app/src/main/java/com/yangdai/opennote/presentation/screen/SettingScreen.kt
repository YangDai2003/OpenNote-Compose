package com.yangdai.opennote.presentation.screen

import android.os.Parcelable
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import com.yangdai.opennote.presentation.component.setting.SettingsDetailPane
import com.yangdai.opennote.presentation.component.setting.SettingsListPane
import com.yangdai.opennote.presentation.navigation.INITIAL_OFFSET_FACTOR
import com.yangdai.opennote.presentation.navigation.sharedAxisXIn
import com.yangdai.opennote.presentation.navigation.sharedAxisXOut
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import kotlin.coroutines.cancellation.CancellationException

@Parcelize
class SettingsItem(val index: Int, val titleId: Int) : Parcelable

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SettingsScreen(navigateUp: () -> Unit) {

    val navigator = rememberListDetailPaneScaffoldNavigator<SettingsItem>()
    val coroutineScope = rememberCoroutineScope()

    key(navigator) {
        PredictiveBackHandler(enabled = navigator.canNavigateBack()) { progress ->
            try {
                progress.collect { backEvent ->
                    navigator.seekBack(fraction = backEvent.progress)
                }
                navigator.navigateBack()
            } catch (_: CancellationException) {
                withContext(NonCancellable) { navigator.seekBack(fraction = 0f) }
            }
        }
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        scaffoldState = navigator.scaffoldState,
        listPane = {
            AnimatedPane(
                enterTransition = sharedAxisXIn(initialOffsetX = { -(it * INITIAL_OFFSET_FACTOR).toInt() }),
                exitTransition = sharedAxisXOut(targetOffsetX = { -(it * INITIAL_OFFSET_FACTOR).toInt() }),
            ) {
                SettingsListPane(navigateUp = navigateUp) {
                    coroutineScope.launch {
                        navigator.navigateTo(
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
                exitTransition = sharedAxisXOut(targetOffsetX = { (it * INITIAL_OFFSET_FACTOR).toInt() }),
            ) {
                navigator.currentDestination?.contentKey?.let { item ->
                    SettingsDetailPane(selectedSettingsItem = item) {
                        if (navigator.canNavigateBack()) {
                            coroutineScope.launch { navigator.navigateBack() }
                        }
                    }
                }
            }
        }
    )
}
