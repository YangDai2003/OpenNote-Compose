package com.yangdai.opennote.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import com.yangdai.opennote.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSearchbar(
    scope: CoroutineScope,
    drawerState: DrawerState,
    onSearch: (String) -> Unit,
    onSort: () -> Unit
) {
    var text by remember {
        mutableStateOf("")
    }
    var active by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current
    val defaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

    val dataList =
        defaultSharedPrefs.getStringSet("history", setOf())!!.sorted().toMutableList()

    val configuration = LocalConfiguration.current
    val orientation = remember(configuration) { configuration.orientation }

    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchBar(
                query = text,
                onQueryChange = {
                    text = it
                },
                onSearch = {
                    if (!dataList.contains(it) && it.isNotEmpty()) {
                        dataList.add(it)
                        defaultSharedPrefs.edit().putStringSet("history", dataList.toSet()).apply()
                    }
                    active = false
                    onSearch(text)
                },
                active = active,
                onActiveChange = {
                    active = it
                },
                placeholder = {
                    Text(text = stringResource(R.string.search))
                },
                leadingIcon = {
                    AnimatedVisibility(visible = !active) {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.apply {
                                    if (isClosed) open() else close()
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuOpen,
                                contentDescription = ""
                            )
                        }
                    }
                    AnimatedVisibility(visible = active) {
                        IconButton(onClick = {
                            active = false
                            onSearch(text)
                        }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "")
                        }

                    }
                },
                trailingIcon = {

                    AnimatedVisibility(visible = active) {
                        IconButton(onClick = {
                            if (text.isNotEmpty()) {
                                text = ""
                            } else {
                                active = false
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = ""
                            )
                        }
                    }
                    AnimatedVisibility(visible = !active) {
                        IconButton(onClick = onSort) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Sort,
                                contentDescription = ""
                            )
                        }
                    }
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                        .padding(end = 8.dp)
                        .padding(top = 12.dp)
                        .height(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(text = stringResource(R.string.search_history))

                    IconButton(onClick = {
                        dataList.clear()
                        defaultSharedPrefs.edit().putStringSet("history", setOf()).apply()
                    }) {
                        Icon(imageVector = Icons.Outlined.DeleteForever, contentDescription = "")
                    }

                }

                dataList.forEach {
                    ListItem(
                        modifier = Modifier.clickable { text = it },
                        headlineContent = { Text(text = it) },
                        leadingContent = {
                            Icon(
                                modifier = Modifier.padding(end = 12.dp),
                                imageVector = Icons.Default.History,
                                contentDescription = ""
                            )
                        })
                }
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 12.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockedSearchBar(
                query = text,
                onQueryChange = {
                    text = it
                },
                onSearch = {
                    if (!dataList.contains(it) && it.isNotEmpty()) {
                        dataList.add(it)
                        defaultSharedPrefs.edit().putStringSet("history", dataList.toSet()).apply()
                    }
                    active = false
                    onSearch(text)
                },
                active = active,
                onActiveChange = {
                    active = it
                },
                placeholder = {
                    Text(text = "Search")
                },
                leadingIcon = {
                    AnimatedVisibility(visible = !active) {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.apply {
                                    if (isClosed) open() else close()
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuOpen,
                                contentDescription = ""
                            )
                        }
                    }
                    AnimatedVisibility(visible = active) {
                        IconButton(onClick = {
                            active = false
                            onSearch(text)
                        }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "")
                        }

                    }
                },
                trailingIcon = {

                    AnimatedVisibility(visible = active) {
                        IconButton(onClick = {
                            if (text.isNotEmpty()) {
                                text = ""
                            } else {
                                active = false
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = ""
                            )
                        }
                    }
                    AnimatedVisibility(visible = !active) {
                        IconButton(onClick = onSort) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = ""
                            )
                        }
                    }
                }
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                        .padding(end = 8.dp)
                        .padding(top = 12.dp)
                        .height(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(text = stringResource(R.string.search_history))

                    IconButton(onClick = {
                        dataList.clear()
                        defaultSharedPrefs.edit().putStringSet("history", setOf()).apply()
                    }) {
                        Icon(imageVector = Icons.Outlined.DeleteForever, contentDescription = "")
                    }

                }

                dataList.forEach {
                    ListItem(
                        modifier = Modifier.clickable { text = it },
                        headlineContent = { Text(text = it) },
                        leadingContent = {
                            Icon(
                                modifier = Modifier.padding(end = 12.dp),
                                imageVector = Icons.Default.History,
                                contentDescription = ""
                            )
                        })
                }
            }
        }
    }
}
