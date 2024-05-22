package com.yangdai.opennote.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yangdai.opennote.MainActivity
import com.yangdai.opennote.R
import com.yangdai.opennote.data.local.entity.FolderEntity
import com.yangdai.opennote.presentation.event.FolderEvent
import com.yangdai.opennote.presentation.component.FolderItem
import com.yangdai.opennote.presentation.component.ModifyFolderDialog
import com.yangdai.opennote.presentation.component.TopBarTitle
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderScreen(
    sharedViewModel: SharedViewModel = hiltViewModel(LocalContext.current as MainActivity),
    navigateUp: () -> Unit
) {

    val folderList by sharedViewModel.foldersStateFlow.collectAsStateWithLifecycle()

    var showAddFolderDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    TopBarTitle(title = stringResource(id = R.string.folders))
                },
                navigationIcon = {
                    IconButton(onClick = navigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddFolderDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add"
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors()
                    .copy(scrolledContainerColor = TopAppBarDefaults.largeTopAppBarColors().containerColor),
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->

        LazyVerticalGrid(
            columns = GridCells.Adaptive(360.dp),
            contentPadding = paddingValues
        ) {
            items(folderList) {
                FolderItem(
                    folder = it,
                    onModify = { folderEntity ->
                        sharedViewModel.onFolderEvent(
                            FolderEvent.UpdateFolder(folderEntity)
                        )
                    }
                ) {
                    sharedViewModel.onFolderEvent(FolderEvent.DeleteFolder(it))
                }
            }
        }

        if (showAddFolderDialog) {
            ModifyFolderDialog(
                folder = FolderEntity(),
                onDismissRequest = { showAddFolderDialog = false }
            ) {
                sharedViewModel.onFolderEvent(
                    FolderEvent.AddFolder(it)
                )
            }
        }
    }
}
