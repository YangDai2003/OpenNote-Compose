package com.yangdai.opennote.presentation.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush.Companion.horizontalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yangdai.opennote.MainActivity
import com.yangdai.opennote.R
import com.yangdai.opennote.data.local.entity.FolderEntity
import com.yangdai.opennote.presentation.component.TopBarTitle
import com.yangdai.opennote.presentation.component.dialog.ModifyFolderDialog
import com.yangdai.opennote.presentation.component.dialog.WarningDialog
import com.yangdai.opennote.presentation.event.FolderEvent
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderScreen(
    sharedViewModel: SharedViewModel = hiltViewModel(LocalActivity.current as MainActivity),
    navigateUp: () -> Unit
) {

    val folderNoteCounts by sharedViewModel.folderWithNoteCountsFlow.collectAsStateWithLifecycle()

    var showAddFolderDialog by rememberSaveable { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    TopBarTitle(title = stringResource(id = R.string.folders))
                },
                navigationIcon = {
                    IconButton(onClick = navigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(id = R.string.navigate_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddFolderDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.CreateNewFolder,
                            contentDescription = "Create New Folder"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
                    .copy(scrolledContainerColor = TopAppBarDefaults.topAppBarColors().containerColor),
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->

        LazyVerticalGrid(
            modifier = Modifier.padding(horizontal = 16.dp),
            columns = GridCells.Adaptive(360.dp),
            contentPadding = paddingValues,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(folderNoteCounts, key = { it.first.id!! }, contentType = { "FolderItem" }) {
                FolderItem(
                    folder = it.first,
                    notesCountInFolder = it.second,
                    onModify = { folderEntity ->
                        sharedViewModel.onFolderEvent(
                            FolderEvent.UpdateFolder(folderEntity)
                        )
                    },
                    onDelete = {
                        sharedViewModel.onFolderEvent(FolderEvent.DeleteFolder(it.first))
                    }
                )
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

@Composable
fun LazyGridItemScope.FolderItem(
    folder: FolderEntity,
    notesCountInFolder: Int,
    onModify: (FolderEntity) -> Unit,
    onDelete: () -> Unit,
    colorScheme: ColorScheme = MaterialTheme.colorScheme
) {
    var showModifyDialog by remember { mutableStateOf(false) }
    var showWarningDialog by remember { mutableStateOf(false) }
    val folderColor by remember(folder, colorScheme) {
        mutableStateOf(if (folder.color != null) Color(folder.color) else colorScheme.primary)
    }

    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { it * 0.3f },
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    // Edit action (right swipe)
                    showModifyDialog = true
                    false
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    // Delete action (left swipe)
                    showWarningDialog = true
                    false
                }

                SwipeToDismissBoxValue.Settled -> true
            }
        }
    )
    var showContextMenu by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    LaunchedEffect(isHovered) {
        delay(100L)
        showContextMenu = isHovered
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = horizontalGradient(
                            colors = listOf(
                                folderColor.copy(alpha = 0.2f),
                                folderColor.copy(alpha = 0.1f),
                                colorScheme.errorContainer
                            )
                        )
                    )
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.DriveFileRenameOutline,
                    contentDescription = null,
                    tint = folderColor,
                    modifier = Modifier.size(30.dp)
                )
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = colorScheme.onErrorContainer,
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        modifier = Modifier
            .padding(bottom = 16.dp)
            .clip(CardDefaults.elevatedShape)
            .animateItem()
            .hoverable(interactionSource)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        showContextMenu = true
                    }
                )
            }
    ) {
        ElevatedCard {
            // Folder Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(folderColor.copy(alpha = 0.1f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Folder",
                    tint = folderColor,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = folderColor
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val text =
                        "$notesCountInFolder${
                            if (notesCountInFolder == 1 || notesCountInFolder == 0)
                                stringResource(R.string.note)
                            else stringResource(R.string.notes)
                        }"
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Gray
                        )
                    )
                }
            }
        }

        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.modify)) },
                leadingIcon = {
                    Icon(Icons.Outlined.DriveFileRenameOutline, contentDescription = null)
                },
                onClick = {
                    showModifyDialog = true
                    showContextMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.delete)) },
                leadingIcon = {
                    Icon(Icons.Outlined.Delete, contentDescription = null)
                },
                onClick = {
                    showWarningDialog = true
                    showContextMenu = false
                }
            )
        }
    }

    if (showWarningDialog) {
        WarningDialog(
            message = stringResource(R.string.deleting_a_folder_will_also_delete_all_the_notes_it_contains_and_they_cannot_be_restored_do_you_want_to_continue),
            onDismissRequest = { showWarningDialog = false },
            onConfirm = onDelete
        )
    }

    if (showModifyDialog) {
        ModifyFolderDialog(
            folder = folder,
            onDismissRequest = { showModifyDialog = false }) {
            onModify(it)
        }
    }
}
