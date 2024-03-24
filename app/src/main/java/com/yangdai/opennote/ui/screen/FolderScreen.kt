package com.yangdai.opennote.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.yangdai.opennote.R
import com.yangdai.opennote.data.local.entity.FolderEntity
import com.yangdai.opennote.ui.event.FolderEvent
import com.yangdai.opennote.ui.event.ListEvent
import com.yangdai.opennote.ui.viewmodel.MainScreenViewModel
import com.yangdai.opennote.ui.component.FolderItem
import com.yangdai.opennote.ui.component.ModifyDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderScreen(
    navController: NavController,
    viewModel: MainScreenViewModel
) {

    val listState by viewModel.stateFlow.collectAsStateWithLifecycle()

    var showAddFolderDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    if (showAddFolderDialog) {
        ModifyDialog(
            folder = FolderEntity(null, "", null),
            onDismissRequest = { showAddFolderDialog = false },
            onModify = {
                viewModel.onFolderEvent(
                    FolderEvent.AddFolder(it)
                )
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = stringResource(id = R.string.folders)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = TopAppBarDefaults.largeTopAppBarColors().navigationIconContentColor,
                    titleContentColor = TopAppBarDefaults.largeTopAppBarColors().titleContentColor,
                    actionIconContentColor = TopAppBarDefaults.largeTopAppBarColors().actionIconContentColor
                ),
                actions = {
                    IconButton(onClick = { showAddFolderDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->

        val state = rememberLazyListState()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            state = state
        ) {
            items(listState.folders) { folder ->
                FolderItem(
                    folder = folder,
                    onModify = { folderEntity ->
                        viewModel.onFolderEvent(
                            FolderEvent.UpdateFolder(folderEntity)
                        )
                    }) {

                    if (folder.id != null) {
                        viewModel.onListEvent(ListEvent.DeleteNotesByFolderId(folder.id))
                        viewModel.onFolderEvent(FolderEvent.DeleteFolder(folder.id))
                    }
                }
            }
        }
    }
}