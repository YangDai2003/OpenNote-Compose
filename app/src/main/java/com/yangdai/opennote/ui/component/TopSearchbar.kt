package com.yangdai.opennote.ui.component

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuOpen
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    onActiveChange: (Boolean) -> Unit,
    onSort: () -> Unit
) {
    var inputText by remember {
        mutableStateOf("")
    }
    var active by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(active) {
        onActiveChange(active)
    }

    val context = LocalContext.current
    val defaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
    val dataList = remember {
        defaultSharedPrefs.getStringSet("history", setOf())?.sorted()?.toMutableList()
            ?: mutableListOf()
    }

    val configuration = LocalConfiguration.current
    val orientation = remember(configuration) { configuration.orientation }

    fun search(text: String) {
        if (!dataList.contains(text) && text.isNotEmpty()) {
            dataList.add(text)
            defaultSharedPrefs.edit().putStringSet("history", dataList.toSet()).apply()
        }
        active = false
        onSearch(inputText)
    }

    @Composable
    fun LeadingIcon() {
        AnimatedVisibility(visible = !active, enter = fadeIn(), exit = fadeOut()) {
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
        AnimatedVisibility(visible = active, enter = fadeIn(), exit = fadeOut()) {
            IconButton(onClick = {
                active = false
                onSearch(inputText)
            }) {
                Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search")
            }

        }
    }

    @Composable
    fun TrailingIcon() {
        AnimatedVisibility(visible = active, enter = fadeIn(), exit = fadeOut()) {
            IconButton(onClick = {
                if (inputText.isNotEmpty()) {
                    inputText = ""
                } else {
                    active = false
                    onSearch(inputText)
                }
            }) {
                Icon(
                    imageVector = Icons.Outlined.Clear,
                    contentDescription = "Clear"
                )
            }
        }
        AnimatedVisibility(visible = !active, enter = fadeIn(), exit = fadeOut()) {
            IconButton(onClick = onSort) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Sort,
                    contentDescription = "Sort"
                )
            }
        }
    }

    @Composable
    fun History() {
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
                Icon(
                    imageVector = Icons.Outlined.DeleteForever,
                    contentDescription = "Clear History"
                )
            }

        }

        repeat(dataList.size) {
            ListItem(
                modifier = Modifier.clickable { inputText = dataList[it] },
                headlineContent = { Text(text = dataList[it]) },
                leadingContent = {
                    Icon(
                        modifier = Modifier.padding(end = 12.dp),
                        imageVector = Icons.Outlined.History,
                        contentDescription = "History"
                    )
                })
        }
    }

    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchBar(
                query = inputText,
                onQueryChange = { inputText = it },
                onSearch = { search(it) },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text(text = stringResource(R.string.search)) },
                leadingIcon = { LeadingIcon() },
                trailingIcon = { TrailingIcon() }
            ) {
                History()
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockedSearchBar(
                query = inputText,
                onQueryChange = { inputText = it },
                onSearch = { search(it) },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text(text = "Search") },
                leadingIcon = { LeadingIcon() },
                trailingIcon = { TrailingIcon() }
            ) {
                History()
            }
        }
    }
}
