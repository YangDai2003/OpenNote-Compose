package com.yangdai.opennote.presentation.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yangdai.opennote.data.local.entity.FolderEntity
import kotlinx.collections.immutable.ImmutableList

@Composable
fun PermanentNavigationScreen(
    folderList: ImmutableList<FolderEntity>,
    selectedDrawerIndex: Int,
    content: @Composable () -> Unit,
    navigateTo: (Any) -> Unit,
    selectDrawer: (Int, FolderEntity) -> Unit
) = PermanentNavigationDrawer(
    modifier = Modifier.fillMaxSize(),
    drawerContent = {
        PermanentDrawerSheet {
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