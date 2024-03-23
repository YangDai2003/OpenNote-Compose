package com.yangdai.opennote.ui.screen

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
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.yangdai.opennote.R
import com.yangdai.opennote.Route
import com.yangdai.opennote.data.local.entity.FolderEntity
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.ui.event.ListEvent
import com.yangdai.opennote.ui.component.NoteCard
import com.yangdai.opennote.ui.viewmodel.MainScreenViewModel
import com.yangdai.opennote.ui.component.DrawerItem
import com.yangdai.opennote.ui.component.FolderListSheet
import com.yangdai.opennote.ui.component.OrderSection
import com.yangdai.opennote.ui.component.TopSearchbar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainScreenViewModel
) {
    // 协程作用域
    val scope = rememberCoroutineScope()
    val listState = viewModel.stateFlow.collectAsStateWithLifecycle().value

    // 搜索栏状态
    var isSearchBarActive by remember { mutableStateOf(false) }

    // 侧边栏状态, SearchBarActive为true时不允许打开侧边栏
    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed,
        confirmStateChange = { !isSearchBarActive })

    // 瀑布流状态
    val gridState = rememberLazyStaggeredGridState()
    val density = LocalDensity.current

    // 侧边栏选择的项和文件夹，0表示all 1表示回收站 其余为文件夹index
    var selectedDrawer by rememberSaveable { mutableIntStateOf(0) }
    var selectedFolder by remember { mutableStateOf(FolderEntity()) }
    var folderName by rememberSaveable { mutableStateOf("") }
    // 防止配置变化后文件夹名字丢失
    LaunchedEffect(selectedFolder) {
        if (selectedFolder.id != null) {
            folderName = selectedFolder.name
        }
    }

    // 记录是否已经长按开启多选模式
    var isEnabled by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var selectAll by remember { mutableStateOf(false) }

    // 是否展示悬浮按钮
    val showFloatingButton by remember {
        derivedStateOf {
            !gridState.isScrollInProgress && selectedDrawer == 0 && !isSearchBarActive && !isEnabled
        }
    }

    // 记录文件夹列表是否展开
    var isFoldersExpended by rememberSaveable { mutableStateOf(false) }

    // 文件夹列表弹窗状态
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }


    // 重置多选
    fun initSelect() {
        isEnabled = false
        selectedItems = emptySet()
        selectAll = false
    }

    // 选择侧边栏项引发的操作
    fun selectDrawer(num: Int, onSelect: () -> Unit) {
        if (selectedDrawer != num) {
            selectedDrawer = num
            initSelect()
            onSelect()
        }
    }

    // 全选和撤销全选
    LaunchedEffect(key1 = selectAll) {
        selectedItems = if (selectAll)
            selectedItems.plus(listState.notes.mapNotNull { noteEntity -> noteEntity.id })
        else
            selectedItems.minus(listState.notes.mapNotNull { noteEntity -> noteEntity.id }.toSet())
    }

    // 点击侧边栏重置多选模式
    LaunchedEffect(selectedDrawer) { initSelect() }

    // 拦截返回手势, 确保返回符合逻辑
    BackHandler(isEnabled || drawerState.isOpen || selectedDrawer != 0) {
        if (drawerState.isOpen) {
            scope.launch {
                drawerState.apply {
                    close()
                }
            }
            return@BackHandler
        }
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

    ModalNavigationDrawer(

        drawerState = drawerState,

        drawerContent = {
            ModalDrawerSheet {

                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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
                        selected = selectedDrawer == 0
                    ) {
                        selectDrawer(0) {
                            selectedFolder = FolderEntity()
                            viewModel.onListEvent(ListEvent.Sort(trash = false))
                        }

                        scope.launch {
                            drawerState.apply {
                                close()
                            }
                        }
                    }

                    DrawerItem(
                        icon = Icons.Outlined.Delete,
                        label = stringResource(R.string.trash),
                        selected = selectedDrawer == 1
                    ) {
                        selectDrawer(1) {
                            viewModel.onListEvent(ListEvent.Sort(trash = true))
                        }
                        scope.launch {
                            drawerState.apply {
                                close()
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    DrawerItem(
                        icon = if (!isFoldersExpended) Icons.AutoMirrored.Outlined.KeyboardArrowRight else Icons.Outlined.KeyboardArrowDown,
                        label = stringResource(R.string.folders),
                        badge = listState.folders.size.toString(),
                        selected = false
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
                                    selected = selectedDrawer == index + 2
                                ) {

                                    selectDrawer(index + 2) {
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
        },
    ) {


        Scaffold(
            topBar = {
                AnimatedContent(targetState = selectedDrawer == 0, label = "") {
                    if (it) {
                        TopSearchbar(
                            scope = scope,
                            drawerState = drawerState,
                            onSearch = { text -> viewModel.onListEvent(ListEvent.Search(text)) },
                            onActiveChange = { active -> isSearchBarActive = active }
                        ) {
                            viewModel.onListEvent(ListEvent.ToggleOrderSection)
                        }
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
}