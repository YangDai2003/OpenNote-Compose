package com.yangdai.opennote.presentation.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yangdai.opennote.data.local.entity.FolderEntity

@Composable
fun ModalNavigationScreen(
    drawerState: DrawerState,
    gesturesEnabled: Boolean,
    folderList: List<FolderEntity>,
    selectedDrawerIndex: Int,
    content: @Composable () -> Unit,
    navigateTo: (String) -> Unit,
    selectDrawer: (Int, FolderEntity)-> Unit
) = ModalNavigationDrawer(
    modifier = Modifier.fillMaxSize(),
    drawerState = drawerState,
    gesturesEnabled = gesturesEnabled,
    drawerContent = {
        ModalDrawerSheet(drawerState = drawerState) {
            DrawerContent(
                folderList = folderList,
                selectedDrawerIndex = selectedDrawerIndex,
                navigateTo = { navigateTo(it) }
            ) { position, folder ->
                selectDrawer(position, folder)
            }
        }
    }
) {
    content()
}