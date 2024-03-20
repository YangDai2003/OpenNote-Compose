package com.yangdai.opennote.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.yangdai.opennote.R
import com.yangdai.opennote.Route
import com.yangdai.opennote.list.ListEvent
import com.yangdai.opennote.ui.components.NoteCard
import com.yangdai.opennote.list.NoteListViewModel
import com.yangdai.opennote.ui.components.DrawerItem
import com.yangdai.opennote.ui.components.OrderSection
import com.yangdai.opennote.ui.components.TopSearchbar

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: NoteListViewModel = hiltViewModel()
) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var isFolderExpended by remember {
        mutableStateOf(false)
    }

    val folderNum = 20

    val notesState = viewModel.stateFlow.collectAsStateWithLifecycle().value

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
                            Icon(imageVector = Icons.Outlined.Settings, contentDescription = "")
                        }
                    }

                    DrawerItem(
                        icon = Icons.Outlined.Book,
                        label = stringResource(R.string.all_notes),
                        badge = notesState.notes.size.toString(),
                        selected = false
                    ) {

                    }

                    DrawerItem(
                        icon = Icons.Outlined.Delete,
                        label = stringResource(R.string.trash),
                        badge = "0",
                        selected = false
                    ) {

                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    DrawerItem(
                        icon = if (!isFolderExpended) Icons.AutoMirrored.Outlined.KeyboardArrowRight else Icons.Outlined.KeyboardArrowDown,
                        label = stringResource(R.string.folders),
                        badge = folderNum.toString(),
                        selected = false
                    ) {
                        isFolderExpended = !isFolderExpended
                    }

                    AnimatedVisibility(visible = isFolderExpended) {
                        Column {
                            repeat(folderNum) {

                                DrawerItem(
                                    icon = Icons.Outlined.Folder,
                                    label = "文件夹 ${it + 1}",
                                    badge = "0",
                                    selected = false
                                ) {

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
                            onClick = { }) {
                            Text(text = stringResource(R.string.manage_folders))
                        }
                    }
                }
            }
        },
    ) {

        val gridState = rememberLazyStaggeredGridState()

        val density = LocalDensity.current

        Scaffold(
            topBar = {
                TopSearchbar(
                    scope = scope,
                    drawerState = drawerState,
                    onSearch = { viewModel.onEvent(ListEvent.Search(it)) }) {
                    viewModel.onEvent(ListEvent.ToggleOrderSection)
                }
            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = !gridState.isScrollInProgress,
                    enter = slideInHorizontally { with(density) { 100.dp.roundToPx() } },
                    exit = slideOutHorizontally { with(density) { 100.dp.roundToPx() } }) {
                    FloatingActionButton(onClick = { navController.navigate(Route.NOTE) }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "")
                    }
                }

            }) { innerPadding ->

            if (notesState.isOrderSectionVisible) {
                AlertDialog(
                    title = { Text(text = stringResource(R.string.sort_by)) },
                    text = {
                        OrderSection(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            noteOrder = notesState.noteOrder,
                            onOrderChange = {
                                viewModel.onEvent(ListEvent.Order(it))
                            }
                        )
                    },
                    onDismissRequest = { viewModel.onEvent(ListEvent.ToggleOrderSection) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.onEvent(ListEvent.ToggleOrderSection)
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
                    items(notesState.notes) { note ->
                        NoteCard(
                            note = note,
                            onNoteClick = {
                                navController.navigate(
                                    Route.NOTE.replace(
                                        "{id}",
                                        note.id.toString()
                                    )
                                )
                            })
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp)
            )
        }
    }
}