package com.yangdai.opennote.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yangdai.opennote.MainActivity
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.navigation.Route
import com.yangdai.opennote.data.local.entity.FolderEntity
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.presentation.event.ListEvent
import com.yangdai.opennote.presentation.component.NoteCard
import com.yangdai.opennote.presentation.component.DrawerItem
import com.yangdai.opennote.presentation.component.FolderListSheet
import com.yangdai.opennote.presentation.component.OrderSection
import com.yangdai.opennote.presentation.component.TopSearchbar
import com.yangdai.opennote.presentation.state.ListState
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    sharedViewModel: SharedViewModel = hiltViewModel(LocalContext.current as MainActivity),
    isLargeScreen: Boolean,
    navigateTo: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val listState by sharedViewModel.listStateFlow.collectAsStateWithLifecycle()

    // Search bar state
    var isSearchBarActive by remember { mutableStateOf(false) }

    // Staggered grid state, used to control the scroll state of the note grid
    val gridState = rememberLazyStaggeredGridState()

    // Selected drawer item and folder, 0 for all, 1 for trash, others for folder index
    var selectedDrawer by rememberSaveable { mutableIntStateOf(0) }
    var selectedFolder by remember { mutableStateOf(FolderEntity()) }
    var folderName by rememberSaveable { mutableStateOf("") }
    // Prevent the folder name from being lost after the configuration changes
    LaunchedEffect(selectedFolder) {
        if (selectedFolder.id != null) {
            folderName = selectedFolder.name
        }
    }

    // Record whether multi-select mode has been enabled, selected items and whether all items have been selected
    var isEnabled by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf<Set<NoteEntity>>(emptySet()) }
    var selectAll by remember { mutableStateOf(false) }

    // Whether to show the floating button, determined by the scroll state of the grid, the selected drawer, the search bar, and whether multi-select mode is enabled
    val showFloatingButton by remember {
        derivedStateOf {
            !gridState.isScrollInProgress && selectedDrawer == 0 && !isSearchBarActive && !isEnabled
        }
    }

    // Bottom sheet pop-up status for moving notes
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    // Reset multi-select mode
    fun initSelect() {
        isEnabled = false
        selectedItems = emptySet()
        selectAll = false
    }

    // Operation caused by selecting the drawer item
    fun selectDrawer(position: Int, onSelect: () -> Unit) {
        if (selectedDrawer != position) {
            selectedDrawer = position
            initSelect()
            onSelect()
        }
    }

    // select all and deselect all, triggered by the checkbox in the bottom bar
    LaunchedEffect(selectAll) {
        selectedItems = if (selectAll) selectedItems.plus(listState.notes)
        else selectedItems.minus(listState.notes.toSet())
    }

    // Back logic for better user experience
    BackHandler(isEnabled) {
        if (isEnabled) {
            initSelect()
        }
    }

    // Navigation drawer state, confirmStateChange is used to prevent drawer from closing when search bar is active
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    fun closeDrawer() {
        scope.launch {
            drawerState.apply {
                close()
            }
        }
    }

    BackHandler(!isLargeScreen && drawerState.isOpen) {
        closeDrawer()
    }

    @Composable
    fun mainContent() {
        MainContent(
            scope = scope,
            selectedDrawer = selectedDrawer,
            drawerState = drawerState,
            viewModel = sharedViewModel,
            isEnabled = isEnabled,
            isLargeScreen = isLargeScreen,
            folderName = folderName,
            listState = listState,
            selectAll = selectAll,
            selectedItems = selectedItems,
            showFloatingButton = showFloatingButton,
            showBottomSheet = showBottomSheet,
            sheetState = sheetState,
            gridState = gridState,
            selectedFolder = selectedFolder,
            navigateTo = navigateTo,
            initSelect = { initSelect() },
            onActiveChange = { isSearchBarActive = it },
            onCheckedChange = { selectAll = it },
            onShowBottomSheet = { showBottomSheet = true },
            onDismissRequest = { showBottomSheet = false },
            onCloseClick = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        showBottomSheet = false
                    }
                }
            },
            onEnableChange = { isEnabled = it },
            onNoteClick = {
                if (isEnabled) {
                    selectedItems =
                        if (selectedItems.contains(it)) selectedItems.minus(it)
                        else selectedItems.plus(it)
                } else {
                    if (selectedDrawer != 1) {
                        sharedViewModel.onListEvent(ListEvent.ClickNote(it))
                        navigateTo(Route.NOTE)
                    } else {
                        Unit
                    }
                }
            }
        )
    }

    if (!isLargeScreen) {

        ModalNavigationDrawer(
            gesturesEnabled = !isEnabled && !isSearchBarActive,
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(drawerState = drawerState) {
                    DrawerContent(
                        listState = listState,
                        selectedDrawer = selectedDrawer,
                        navigateTo = { navigateTo(it) }
                    ) { position, folder ->
                        when (position) {
                            0 -> {
                                selectDrawer(position) {
                                    selectedFolder = folder
                                    sharedViewModel.onListEvent(ListEvent.Sort(trash = false))
                                }
                                closeDrawer()
                            }

                            1 -> {
                                selectDrawer(position) {
                                    sharedViewModel.onListEvent(ListEvent.Sort(trash = true))
                                }
                                closeDrawer()
                            }

                            else -> {
                                selectDrawer(position) {
                                    selectedFolder = folder
                                    sharedViewModel.onListEvent(
                                        ListEvent.Sort(
                                            filterFolder = true,
                                            folderId = folder.id
                                        )
                                    )
                                }
                                closeDrawer()
                            }
                        }
                    }
                }
            }
        ) {
            mainContent()
        }

    } else {
        PermanentNavigationDrawer(
            drawerContent = {
                PermanentDrawerSheet {
                    DrawerContent(
                        listState = listState,
                        selectedDrawer = selectedDrawer,
                        navigateTo = { navigateTo(it) }
                    ) { position, folder ->
                        when (position) {
                            0 -> {
                                selectDrawer(position) {
                                    selectedFolder = folder
                                    sharedViewModel.onListEvent(ListEvent.Sort(trash = false))
                                }
                            }

                            1 -> {
                                selectDrawer(position) {
                                    sharedViewModel.onListEvent(ListEvent.Sort(trash = true))
                                }
                            }

                            else -> {
                                selectDrawer(position) {
                                    selectedFolder = folder
                                    sharedViewModel.onListEvent(
                                        ListEvent.Sort(
                                            filterFolder = true,
                                            folderId = folder.id
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) {
            mainContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainContent(
    scope: CoroutineScope,
    selectedDrawer: Int,
    drawerState: DrawerState,
    viewModel: SharedViewModel,
    isEnabled: Boolean,
    isLargeScreen: Boolean,
    folderName: String,
    listState: ListState,
    selectAll: Boolean,
    selectedItems: Set<NoteEntity>,
    showFloatingButton: Boolean,
    showBottomSheet: Boolean,
    sheetState: SheetState,
    gridState: LazyStaggeredGridState,
    selectedFolder: FolderEntity,
    navigateTo: (String) -> Unit,
    initSelect: () -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onShowBottomSheet: () -> Unit,
    onDismissRequest: () -> Unit,
    onCloseClick: () -> Unit,
    onEnableChange: (Boolean) -> Unit,
    onNoteClick: (NoteEntity) -> Unit
) {
    Scaffold(
        topBar = {
            AnimatedContent(targetState = selectedDrawer == 0, label = "") {
                if (it) {
                    TopSearchbar(
                        scope = scope,
                        drawerState = drawerState,
                        viewModel = viewModel,
                        enabled = !isEnabled,
                        isSmallScreen = !isLargeScreen,
                        onActiveChange = onActiveChange
                    )
                } else {
                    var showMenu by remember {
                        mutableStateOf(false)
                    }

                    TopAppBar(
                        title = {
                            Text(
                                text = if (selectedDrawer == 1) stringResource(id = R.string.trash)
                                else folderName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        navigationIcon = {
                            if (!isLargeScreen) {
                                IconButton(
                                    enabled = !isEnabled,
                                    onClick = {
                                        scope.launch {
                                            drawerState.apply {
                                                if (isClosed) open() else close()
                                            }
                                        }
                                    }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Menu,
                                        contentDescription = "Open Menu"
                                    )
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.onListEvent(ListEvent.ToggleOrderSection) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Sort,
                                    contentDescription = "Sort"
                                )
                            }
                            if (selectedDrawer == 1) {
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
                                            viewModel.onListEvent(ListEvent.RestoreNotes(listState.notes))
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
                                            viewModel.onListEvent(
                                                ListEvent.DeleteNotes(
                                                    listState.notes,
                                                    false
                                                )
                                            )
                                        })
                                }

                            }
                        })
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = isEnabled,
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
                                checked = selectAll,
                                onCheckedChange = onCheckedChange
                            )

                            Text(text = stringResource(R.string.checked))

                            Text(text = selectedItems.size.toString())
                        }

                        Row(
                            modifier = Modifier.fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            if (selectedDrawer == 1) {
                                TextButton(onClick = {
                                    viewModel.onListEvent(
                                        ListEvent.RestoreNotes(selectedItems.toList())
                                    )
                                    initSelect()
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
                                TextButton(onClick = onShowBottomSheet) {
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
                                viewModel.onListEvent(
                                    ListEvent.DeleteNotes(
                                        selectedItems.toList(),
                                        selectedDrawer != 1
                                    )
                                )
                                initSelect()
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
                visible = showFloatingButton,
                enter = slideInHorizontally { fullWidth -> fullWidth * 3 / 2 },
                exit = slideOutHorizontally { fullWidth -> fullWidth * 3 / 2 }) {
                FloatingActionButton(onClick = {
                    viewModel.onListEvent(ListEvent.AddNote)
                    navigateTo(Route.NOTE)
                }) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = "Add")
                }
            }

        }) { innerPadding ->

        if (showBottomSheet) {
            FolderListSheet(
                hint = stringResource(R.string.select_destination_folder),
                oFolderId = selectedFolder.id,
                folders = listState.folders.toImmutableList(),
                sheetState = sheetState,
                onDismissRequest = onDismissRequest,
                onCloseClick = onCloseClick
            ) {
                viewModel.onListEvent(ListEvent.MoveNotes(selectedItems.toList(), it))
                initSelect()
            }
        }

        if (listState.isOrderSectionVisible) {
            AlertDialog(
                title = { Text(text = stringResource(R.string.sort_by)) },
                text = {
                    OrderSection(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        noteOrder = listState.noteOrder,
                        onOrderChange = {
                            viewModel.onListEvent(
                                ListEvent.Sort(
                                    noteOrder = it,
                                    trash = selectedDrawer == 1,
                                    filterFolder = selectedDrawer != 0 && selectedDrawer != 1,
                                    folderId = selectedFolder.id
                                )
                            )
                        }
                    )
                },
                onDismissRequest = { viewModel.onListEvent(ListEvent.ToggleOrderSection) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.onListEvent(ListEvent.ToggleOrderSection)
                        }
                    ) {
                        Text(stringResource(id = android.R.string.ok))
                    }
                })
        }

        LazyVerticalStaggeredGrid(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                // The top padding is used to prevent the top of the grid from being blocked by the search bar(56.dp)
                .padding(top = 74.dp),
            state = gridState,
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
                items(listState.notes, key = { item: NoteEntity -> item.id!! }) { note ->
                    NoteCard(
                        modifier = Modifier.animateItemPlacement(), // Add animation to the item
                        note = note,
                        isEnabled = isEnabled,
                        isSelected = selectedItems.contains(note),
                        onEnableChange = onEnableChange,
                        onNoteClick = onNoteClick
                    )
                }
            }
        )
    }
}

@Composable
fun DrawerContent(
    listState: ListState,
    selectedDrawer: Int,
    navigateTo: (String) -> Unit,
    onClick: (Int, FolderEntity) -> Unit
) {
    // Record whether the folder list is expanded
    var isFoldersExpended by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.verticalScroll(scrollState)) {
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
            isSelected = selectedDrawer == 0
        ) {
            onClick(0, FolderEntity())
        }

        Spacer(modifier = Modifier.padding(vertical = 2.dp))

        DrawerItem(
            icon = Icons.Outlined.Delete,
            label = stringResource(R.string.trash),
            isSelected = selectedDrawer == 1
        ) {
            onClick(1, FolderEntity())
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        DrawerItem(
            icon = if (!isFoldersExpended) Icons.AutoMirrored.Outlined.KeyboardArrowRight else Icons.Outlined.KeyboardArrowDown,
            label = stringResource(R.string.folders),
            badge = listState.folders.size.toString(),
            isSelected = false
        ) {
            isFoldersExpended = !isFoldersExpended
        }

        AnimatedVisibility(visible = isFoldersExpended) {
            Column {
                listState.folders.forEachIndexed { index, folder ->
                    DrawerItem(
                        icon = Icons.Outlined.FolderOpen,
                        iconTint = if (folder.color != null) Color(folder.color) else MaterialTheme.colorScheme.onSurface,
                        label = folder.name,
                        isSelected = selectedDrawer == index + 2
                    ) {
                        onClick(index + 2, folder)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                onClick = { navigateTo(Route.FOLDERS) }) {
                Text(text = stringResource(R.string.manage_folders))
            }
        }
    }
}
