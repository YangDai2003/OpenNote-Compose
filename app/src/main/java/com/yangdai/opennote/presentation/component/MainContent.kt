package com.yangdai.opennote.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R
import com.yangdai.opennote.data.local.entity.FolderEntity
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.presentation.event.ListEvent
import com.yangdai.opennote.presentation.navigation.Note
import com.yangdai.opennote.presentation.state.DataActionState
import com.yangdai.opennote.presentation.state.DataState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    isListViewMode: Boolean,
    dataActionState: DataActionState,
    isFloatingButtonVisible: Boolean,
    selectedFolder: FolderEntity,
    selectedDrawerIndex: Int,
    allNotesSelected: Boolean,
    selectedNotes: ImmutableList<NoteEntity>,
    isMultiSelectionModeEnabled: Boolean,
    isLargeScreen: Boolean,
    dataState: DataState,
    folderList: ImmutableList<FolderEntity>,
    navigateTo: (Any) -> Unit,
    initializeNoteSelection: () -> Unit,
    onSearchBarActivationChange: (Boolean) -> Unit,
    onAllNotesSelectionChange: (Boolean) -> Unit,
    onMultiSelectionModeChange: (Boolean) -> Unit,
    onNoteClick: (NoteEntity) -> Unit,
    onListEvent: (ListEvent) -> Unit,
    onDrawerStateChange: () -> Unit,
    onExportClick: (String) -> Unit,
    onExportCancelled: () -> Unit
) {

    val staggeredGridState = rememberLazyStaggeredGridState()
    val lazyListState = rememberLazyListState()

    var folderName by rememberSaveable {
        mutableStateOf("")
    }

    // Bottom sheet visibility, reset when configuration changes, no need to use rememberSaveable
    var isFolderDialogVisible by remember {
        mutableStateOf(false)
    }

    // Whether to show the export dialog
    var isExportDialogVisible by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(selectedFolder) {
        if (selectedFolder.id != null) {
            folderName = selectedFolder.name
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AnimatedContent(targetState = selectedDrawerIndex == 0, label = "") {
                if (it) {
                    TopSearchbar(
                        enabled = !isMultiSelectionModeEnabled,
                        isLargeScreen = isLargeScreen,
                        onSearchBarActivationChange = onSearchBarActivationChange,
                        onDrawerStateChange = onDrawerStateChange
                    )
                } else {

                    var showMenu by remember {
                        mutableStateOf(false)
                    }

                    TopAppBar(
                        title = {
                            Text(
                                text = if (selectedDrawerIndex == 1) stringResource(id = R.string.trash)
                                else folderName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        navigationIcon = {
                            if (!isLargeScreen) {
                                IconButton(
                                    enabled = !isMultiSelectionModeEnabled,
                                    onClick = onDrawerStateChange
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Menu,
                                        contentDescription = "Open Menu"
                                    )
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = { onListEvent(ListEvent.ChangeViewMode) }) {
                                Icon(
                                    imageVector = if (isListViewMode) Icons.Outlined.ViewAgenda else Icons.Outlined.GridView,
                                    contentDescription = "View Mode"
                                )
                            }
                            IconButton(onClick = { onListEvent(ListEvent.ToggleOrderSection) }) {
                                Icon(
                                    imageVector = Icons.Outlined.SortByAlpha,
                                    contentDescription = "Sort"
                                )
                            }
                            if (selectedDrawerIndex == 1) {
                                IconButton(onClick = { showMenu = !showMenu }) {
                                    Icon(
                                        imageVector = Icons.Outlined.MoreVert,
                                        contentDescription = "More"
                                    )
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Outlined.RestartAlt,
                                                contentDescription = "Restore"
                                            )
                                        },
                                        text = { Text(text = stringResource(id = R.string.restore_all)) },
                                        onClick = {
                                            onListEvent(ListEvent.RestoreNotes(dataState.notes.toImmutableList()))
                                        })

                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
                                                contentDescription = "Delete"
                                            )
                                        },
                                        text = { Text(text = stringResource(id = R.string.delete_all)) },
                                        onClick = {
                                            onListEvent(
                                                ListEvent.DeleteNotes(
                                                    dataState.notes.toImmutableList(),
                                                    false
                                                )
                                            )
                                        })
                                }
                            }
                        }
                    )
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = isMultiSelectionModeEnabled,
                enter = slideInVertically { fullHeight -> fullHeight },
                exit = slideOutVertically { fullHeight -> fullHeight }
            ) {
                BottomAppBar {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Row(
                            modifier = Modifier.fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Checkbox(
                                checked = allNotesSelected,
                                onCheckedChange = onAllNotesSelectionChange
                            )

                            Text(text = stringResource(R.string.checked))

                            Text(text = selectedNotes.size.toString())
                        }

                        Row(
                            modifier = Modifier.fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            if (selectedDrawerIndex == 1) {
                                TextButton(onClick = {
                                    onListEvent(ListEvent.RestoreNotes(selectedNotes))
                                    initializeNoteSelection()
                                }) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Outlined.RestartAlt,
                                            contentDescription = "Restore"
                                        )
                                        Text(text = stringResource(id = R.string.restore))
                                    }
                                }
                            } else {
                                TextButton(onClick = {
                                    isExportDialogVisible = true
                                }) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Outlined.Upload,
                                            contentDescription = "Export"
                                        )
                                        Text(text = stringResource(id = R.string.export))
                                    }
                                }

                                TextButton(onClick = { isFolderDialogVisible = true }) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.DriveFileMove,
                                            contentDescription = "Move"
                                        )
                                        Text(text = stringResource(id = R.string.move))
                                    }
                                }
                            }

                            TextButton(onClick = {
                                onListEvent(
                                    ListEvent.DeleteNotes(
                                        selectedNotes,
                                        selectedDrawerIndex != 1
                                    )
                                )
                                initializeNoteSelection()
                            }) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Delete"
                                    )
                                    Text(text = stringResource(id = R.string.delete))
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isFloatingButtonVisible && !staggeredGridState.isScrollInProgress && !lazyListState.isScrollInProgress,
                enter = slideInHorizontally { fullWidth -> fullWidth * 3 / 2 },
                exit = slideOutHorizontally { fullWidth -> fullWidth * 3 / 2 }) {
                FloatingActionButton(
                    onClick = {
                        onListEvent(ListEvent.AddNote)
                        navigateTo(Note)
                    }
                ) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = "Add")
                }
            }

        }) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 72.dp)
        ) {

            AnimatedContent(targetState = isListViewMode, label = "") {
                if (!it) {
                    LazyVerticalStaggeredGrid(
                        modifier = Modifier
                            .fillMaxSize(),
                        state = staggeredGridState,
                        // The staggered grid layout is adaptive, with a minimum column width of 160dp(mdpi)
                        columns = StaggeredGridCells.Adaptive(160.dp),
                        verticalItemSpacing = 8.dp,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        // for better edgeToEdge experience
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = innerPadding.calculateBottomPadding()
                        ),
                        content = {
                            items(
                                dataState.notes,
                                key = { item: NoteEntity -> item.id!! }) { note ->
                                GridNoteCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem(), // Add animation to the item
                                    note = note,
                                    isEnabled = isMultiSelectionModeEnabled,
                                    isSelected = selectedNotes.contains(note),
                                    onEnableChange = onMultiSelectionModeChange,
                                    onNoteClick = onNoteClick
                                )
                            }
                        }
                    )
                } else {
                    if (dataState.notes.isEmpty()) {
                        return@AnimatedContent
                    }
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        VerticalDivider(
                            Modifier
                                .align(Alignment.TopStart)
                                .fillMaxHeight()
                                .padding(start = 15.dp),
                            thickness = 2.dp
                        )
                        LazyColumn(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxSize(),
                            state = lazyListState,
                            contentPadding = PaddingValues(
                                start = 12.dp,
                                end = 16.dp,
                                bottom = innerPadding.calculateBottomPadding()
                            )
                        ) {
                            items(
                                dataState.notes,
                                key = { item: NoteEntity -> item.id!! }) { note ->
                                ColumnNoteCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem(), // Add animation to the item
                                    note = note,
                                    isEnabled = isMultiSelectionModeEnabled,
                                    isSelected = selectedNotes.contains(note),
                                    onEnableChange = onMultiSelectionModeChange,
                                    onNoteClick = onNoteClick
                                )
                            }
                        }
                    }
                }
            }
        }

        if (dataState.isOrderSectionVisible) {
            AlertDialog(
                title = { Text(text = stringResource(R.string.sort_by)) },
                text = {
                    OrderSection(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        noteOrder = dataState.noteOrder,
                        onOrderChange = {
                            onListEvent(
                                ListEvent.Sort(
                                    noteOrder = it,
                                    trash = selectedDrawerIndex == 1,
                                    filterFolder = selectedDrawerIndex != 0 && selectedDrawerIndex != 1,
                                    folderId = selectedFolder.id
                                )
                            )
                        }
                    )
                },
                onDismissRequest = { onListEvent(ListEvent.ToggleOrderSection) },
                confirmButton = {
                    TextButton(onClick = { onListEvent(ListEvent.ToggleOrderSection) }) {
                        Text(stringResource(id = android.R.string.ok))
                    }
                })
        }

        if (isExportDialogVisible) {
            ExportDialog(onDismissRequest = { isExportDialogVisible = false }) {
                onExportClick(it)
                isExportDialogVisible = false
            }
        }

        if (isFolderDialogVisible) {
            FolderListDialog(
                hint = stringResource(R.string.destination_folder),
                oFolderId = selectedFolder.id,
                folders = folderList,
                onDismissRequest = { isFolderDialogVisible = false }
            ) {
                onListEvent(ListEvent.MoveNotes(selectedNotes, it))
                initializeNoteSelection()
            }
        }

        ProgressDialog(isLoading = dataActionState.loading, progress = dataActionState.progress) {
            onExportCancelled()
        }
    }
}
