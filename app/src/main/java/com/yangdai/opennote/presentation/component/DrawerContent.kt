package com.yangdai.opennote.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R
import com.yangdai.opennote.data.local.entity.FolderEntity
import com.yangdai.opennote.presentation.navigation.Route
import kotlinx.collections.immutable.ImmutableList

@Composable
fun DrawerContent(
    folderList: ImmutableList<FolderEntity>,
    selectedDrawerIndex: Int,
    navigateTo: (String) -> Unit,
    onClick: (Int, FolderEntity) -> Unit
) {

    // Record whether the folder list is expanded
    var isFoldersExpended by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                modifier = Modifier.padding(12.dp),
                onClick = { navigateTo(Route.SETTINGS) }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Open Settings",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        DrawerItem(
            icon = Icons.Outlined.Book,
            label = stringResource(R.string.all_notes),
            isSelected = selectedDrawerIndex == 0
        ) {
            onClick(0, FolderEntity())
        }

        DrawerItem(
            icon = Icons.Outlined.Delete,
            label = stringResource(R.string.trash),
            isSelected = selectedDrawerIndex == 1
        ) {
            onClick(1, FolderEntity())
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        DrawerItem(
            icon = if (!isFoldersExpended) Icons.AutoMirrored.Outlined.KeyboardArrowRight else Icons.Outlined.KeyboardArrowDown,
            label = stringResource(R.string.folders),
            badge = folderList.size.toString(),
            isSelected = false
        ) {
            isFoldersExpended = !isFoldersExpended
        }

        AnimatedVisibility(visible = isFoldersExpended) {
            Column {
                folderList.forEachIndexed { index, folder ->
                    DrawerItem(
                        icon = Icons.Outlined.FolderOpen,
                        iconTint = if (folder.color != null) Color(folder.color) else MaterialTheme.colorScheme.onSurface,
                        label = folder.name,
                        isSelected = selectedDrawerIndex == index + 2
                    ) {
                        onClick(index + 2, folder)
                    }
                }
            }
        }

        TextButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NavigationDrawerItemDefaults.ItemPadding),
            onClick = { navigateTo(Route.FOLDERS) }) {
            Text(text = stringResource(R.string.manage_folders), textAlign = TextAlign.Center)
        }
    }
}
