package com.yangdai.opennote.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yangdai.opennote.MainActivity
import com.yangdai.opennote.presentation.navigation.Route
import com.yangdai.opennote.data.local.entity.FolderEntity
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.presentation.event.ListEvent
import com.yangdai.opennote.presentation.component.MainContent
import com.yangdai.opennote.presentation.component.ModalNavigationScreen
import com.yangdai.opennote.presentation.component.PermanentNavigationScreen
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    sharedViewModel: SharedViewModel = hiltViewModel(LocalContext.current as MainActivity),
    isLargeScreen: Boolean,
    navigateTo: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    // Staggered grid state, used to control the scroll state of the note grid
    val staggeredGridState = rememberLazyStaggeredGridState()

    val dataState by sharedViewModel.dataStateFlow.collectAsStateWithLifecycle()
    val folderList by sharedViewModel.foldersStateFlow.collectAsStateWithLifecycle()

    // Bottom sheet visibility, reset when configuration changes, no need to use rememberSaveable
    var isFolderDialogVisible by remember { mutableStateOf(false) }
    // Search bar state, reset when configuration changes, no need to use rememberSaveable
    var isSearchBarActivated by remember { mutableStateOf(false) }

    // Selected drawer item and folder, 0 for all, 1 for trash, others for folder index
    var selectedDrawerIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedFolder by rememberSaveable(stateSaver = object :
        Saver<FolderEntity, Triple<Long?, String, Int?>> {
        override fun restore(value: Triple<Long?, String, Int?>): FolderEntity {
            return FolderEntity(value.first, value.second, value.third)
        }

        override fun SaverScope.save(value: FolderEntity): Triple<Long?, String, Int?> {
            return Triple(value.id, value.name, value.color)
        }
    }) { mutableStateOf(FolderEntity()) }

    // Record whether multi-select mode has been enabled, selected items and whether all items have been selected
    var isMultiSelectionModeEnabled by remember { mutableStateOf(false) }
    var selectedNotes by remember { mutableStateOf<Set<NoteEntity>>(emptySet()) }
    var allNotesSelected by remember { mutableStateOf(false) }

    // Whether to show the floating button, determined by the scroll state of the grid, the selected drawer, the search bar, and whether multi-select mode is enabled
    val isFloatingButtonVisible by remember {
        derivedStateOf {
            !staggeredGridState.isScrollInProgress && selectedDrawerIndex == 0 && !isSearchBarActivated && !isMultiSelectionModeEnabled
        }
    }

    // Reset multi-select mode
    fun initializeNoteSelection() {
        isMultiSelectionModeEnabled = false
        selectedNotes = emptySet()
        allNotesSelected = false
    }

    // Operation caused by selecting the drawer item
    fun selectDrawer(position: Int, folderEntity: FolderEntity) {
        if (selectedDrawerIndex != position) {
            initializeNoteSelection()
            selectedDrawerIndex = position
            selectedFolder = folderEntity
            when (position) {
                0 -> sharedViewModel.onListEvent(ListEvent.Sort(trash = false))

                1 -> sharedViewModel.onListEvent(ListEvent.Sort(trash = true))

                else -> sharedViewModel.onListEvent(
                    ListEvent.Sort(
                        filterFolder = true,
                        folderId = folderEntity.id
                    )
                )
            }
        }
    }

    // select all and deselect all, triggered by the checkbox in the bottom bar
    LaunchedEffect(allNotesSelected) {
        selectedNotes = if (allNotesSelected) selectedNotes.plus(dataState.notes)
        else selectedNotes.minus(dataState.notes.toSet())
    }

    // Back logic for better user experience
    BackHandler(isMultiSelectionModeEnabled) {
        if (isMultiSelectionModeEnabled) {
            initializeNoteSelection()
        }
    }

    // Navigation drawer state, confirmStateChange is used to prevent drawer from closing when search bar is active
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    fun closeDrawer() {
        coroutineScope.launch {
            drawerState.apply {
                close()
            }
        }
    }

    BackHandler(!isLargeScreen && drawerState.isOpen) {
        closeDrawer()
    }

    val movableContent = remember {
        movableContentOf {
            MainContent(
                selectedDrawerIndex = selectedDrawerIndex,
                selectedFolder = selectedFolder,
                selectedNotes = selectedNotes.toImmutableList(),
                allNotesSelected = allNotesSelected,
                isMultiSelectionModeEnabled = isMultiSelectionModeEnabled,
                isLargeScreen = isLargeScreen,
                dataState = dataState,
                folderList = folderList.toImmutableList(),
                isFloatingButtonVisible = isFloatingButtonVisible,
                isFolderDialogVisible = isFolderDialogVisible,
                staggeredGridState = staggeredGridState,
                navigateTo = navigateTo,
                initializeNoteSelection = { initializeNoteSelection() },
                onSearchBarActivationChange = { isSearchBarActivated = it },
                onAllNotesSelectionChange = { allNotesSelected = it },
                onFolderDialogVisibilityChange = { isFolderDialogVisible = true },
                onFolderDialogDismissRequest = { isFolderDialogVisible = false },
                onMultiSelectionModeChange = { isMultiSelectionModeEnabled = it },
                onNoteClick = {
                    if (isMultiSelectionModeEnabled) {
                        selectedNotes =
                            if (selectedNotes.contains(it)) selectedNotes.minus(it)
                            else selectedNotes.plus(it)
                    } else {
                        if (selectedDrawerIndex != 1) {
                            sharedViewModel.onListEvent(ListEvent.OpenNote(it))
                            navigateTo(Route.NOTE)
                        } else {
                            Unit
                        }
                    }
                },
                onListEvent = { sharedViewModel.onListEvent(it) },
                onDrawerStateChange = {
                    coroutineScope.launch {
                        drawerState.apply {
                            if (isClosed) open() else close()
                        }
                    }
                }
            )
        }
    }

    if (!isLargeScreen) {
        ModalNavigationScreen(
            drawerState = drawerState,
            gesturesEnabled = !isMultiSelectionModeEnabled && !isSearchBarActivated,
            folderList = folderList.toImmutableList(),
            selectedDrawerIndex = selectedDrawerIndex,
            content = movableContent,
            navigateTo = navigateTo
        ) { position, folder ->
            selectDrawer(position, folder)
            closeDrawer()
        }
    } else {
        PermanentNavigationScreen(
            folderList = folderList.toImmutableList(),
            selectedDrawerIndex = selectedDrawerIndex,
            content = movableContent,
            navigateTo = navigateTo
        ) { position, folder ->
            selectDrawer(position, folder)
        }
    }
}
