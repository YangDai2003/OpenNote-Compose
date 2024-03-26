package com.yangdai.opennote.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.MenuOpen
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Checkbox
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.navigation.Route
import com.yangdai.opennote.data.local.entity.FolderEntity
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.presentation.event.ListEvent
import com.yangdai.opennote.presentation.component.NoteCard
import com.yangdai.opennote.presentation.viewmodel.MainScreenViewModel
import com.yangdai.opennote.presentation.component.DrawerItem
import com.yangdai.opennote.presentation.component.FolderListSheet
import com.yangdai.opennote.presentation.component.OrderSection
import com.yangdai.opennote.presentation.component.TopSearchbar
import com.yangdai.opennote.presentation.state.ListState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainScreenViewModel,
    windowSize: WindowSizeClass
) {
    val scope = rememberCoroutineScope()
    val listState by viewModel.stateFlow.collectAsStateWithLifecycle()

    // Search bar state
    var isSearchBarActive by remember { mutableStateOf(false) }

    // Staggered grid state, used to control the scroll state of the note grid
    val gridState = rememberLazyStaggeredGridState()
    val density = LocalDensity.current

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
    var selectedItems by remember { mutableStateOf<Set<Long>>(emptySet()) }
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
    fun selectDrawer(num: Int, onSelect: () -> Unit) {
        if (selectedDrawer != num) {
            selectedDrawer = num
            initSelect()
            onSelect()
        }
    }

    // select all and deselect all, triggered by the checkbox in the bottom bar
    LaunchedEffect(key1 = selectAll) {
        selectedItems = if (selectAll)
            selectedItems.plus(listState.notes.mapNotNull { noteEntity -> noteEntity.id })
        else
            selectedItems.minus(listState.notes.mapNotNull { noteEntity -> noteEntity.id }.toSet())
    }

    // Initialize the selected drawer item, triggered by the drawer item
    LaunchedEffect(selectedDrawer) { initSelect() }

    // Back logic for better user experience
    BackHandler(isEnabled || selectedDrawer != 0) {
        if (isEnabled) {
            initSelect()
            return@BackHandler
        }

        if (selectedDrawer != 0) {
            selectedDrawer = 0
            viewModel.onListEvent(ListEvent.Sort(trash = false))
            return@BackHandler
        }
    }

    val smallScreen = windowSize.widthSizeClass < WindowWidthSizeClass.Expanded

    // Navigation drawer state, confirmStateChange is used to prevent drawer from closing when search bar is active
    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed,
        confirmStateChange = { !isSearchBarActive })

    @Composable
    fun MainContent() {
        Scaffold(
            topBar = {
                AnimatedContent(targetState = selectedDrawer == 0, label = "") {
                    if (it) {
                        TopSearchbar(
                            scope = scope,
                            showMenuIcon = smallScreen,
                            drawerState = drawerState,
                            viewModel = viewModel,
                            onActiveChange = { active ->
                                initSelect()
                                isSearchBarActive = active
                            }
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
                                if (smallScreen) {
                                    IconButton(onClick = {
                                        scope.launch {
                                            drawerState.apply {
                                                if (isClosed) open() else close()
                                            }
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.MenuOpen,
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
                                                val ids =
                                                    listState.notes.mapNotNull { noteEntity -> noteEntity.id }
                                                viewModel.onListEvent(ListEvent.RestoreNotes(ids))
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
                                                val ids =
                                                    listState.notes.mapNotNull { noteEntity -> noteEntity.id }
                                                viewModel.onListEvent(
                                                    ListEvent.DeleteNotesByIds(
                                                        ids,
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
                                    onCheckedChange = { selectAll = it })

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
                                    TextButton(onClick = {
                                        showBottomSheet = true
                                    }) {
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
                                        ListEvent.DeleteNotesByIds(
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
                    enter = slideInHorizontally { with(density) { 100.dp.roundToPx() } },
                    exit = slideOutHorizontally { with(density) { 100.dp.roundToPx() } }) {
                    FloatingActionButton(onClick = { navController.navigate(Route.NOTE) }) {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = "Add")
                    }
                }

            }) { innerPadding ->

            if (showBottomSheet) {
                FolderListSheet(
                    oFolderId = selectedFolder.id,
                    folders = listState.folders,
                    sheetState = sheetState,
                    onDismissRequest = { showBottomSheet = false },
                    onCloseClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    }) {
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
                state = gridState,
                columns = StaggeredGridCells.Adaptive(180.dp),
                verticalItemSpacing = 8.dp,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = {
                    items(listState.notes, key = { item: NoteEntity -> item.id!! }) { note ->
                        NoteCard(
                            note = note,
                            isEnabled = isEnabled,
                            isSelected = selectedItems.contains(note.id),
                            onEnableChange = { value ->
                                isEnabled = value
                            },
                            onNoteClick = {
                                if (isEnabled) {
                                    val id = note.id!!
                                    selectedItems =
                                        if (selectedItems.contains(id)) selectedItems.minus(id)
                                        else selectedItems.plus(id)
                                } else {
                                    if (selectedDrawer != 1) {
                                        navController.navigate(
                                            Route.NOTE.replace(
                                                "{id}",
                                                note.id.toString()
                                            )
                                        )
                                    } else {
                                        Unit
                                    }
                                }
                            })
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp)
            )
        }
    }

    if (smallScreen) {

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    DrawerContent(
                        navController = navController,
                        listState = listState,
                        selectedDrawer = selectedDrawer
                    ) { position, folder ->
                        when (position) {
                            0 -> {
                                selectDrawer(position) {
                                    selectedFolder = folder
                                    viewModel.onListEvent(ListEvent.Sort(trash = false))
                                }

                                scope.launch {
                                    drawerState.apply {
                                        close()
                                    }
                                }
                            }

                            1 -> {
                                selectDrawer(position) {
                                    viewModel.onListEvent(ListEvent.Sort(trash = true))
                                }
                                scope.launch {
                                    drawerState.apply {
                                        close()
                                    }
                                }
                            }

                            else -> {
                                selectDrawer(position) {
                                    selectedFolder = folder
                                    viewModel.onListEvent(
                                        ListEvent.Sort(
                                            filterFolder = true,
                                            folderId = folder.id
                                        )
                                    )
                                }

                                scope.launch {
                                    drawerState.apply {
                                        close()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) {
            MainContent()
        }

    } else {
        PermanentNavigationDrawer(
            drawerContent = {
                PermanentDrawerSheet {
                    DrawerContent(
                        navController = navController,
                        listState = listState,
                        selectedDrawer = selectedDrawer
                    ) { position, folder ->
                        when (position) {
                            0 -> {
                                selectDrawer(position) {
                                    selectedFolder = folder
                                    viewModel.onListEvent(ListEvent.Sort(trash = false))
                                }
                            }

                            1 -> {
                                selectDrawer(position) {
                                    viewModel.onListEvent(ListEvent.Sort(trash = true))
                                }
                            }

                            else -> {
                                selectDrawer(position) {
                                    selectedFolder = folder
                                    viewModel.onListEvent(
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
            MainContent()
        }
    }
}

@Composable
fun DrawerContent(
    navController: NavController,
    listState: ListState,
    selectedDrawer: Int,
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
                onClick = { navController.navigate(Route.SETTINGS) }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Open Settings"
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
                onClick = {
                    navController.navigate(Route.FOLDERS)
                }) {
                Text(text = stringResource(R.string.manage_folders))
            }
        }
    }
}
