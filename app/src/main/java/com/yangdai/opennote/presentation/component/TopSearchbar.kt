package com.yangdai.opennote.presentation.component

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yangdai.opennote.MainActivity
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.event.ListEvent
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TopSearchbar(
    viewModel: SharedViewModel = hiltViewModel(LocalContext.current as MainActivity),
    isLargeScreen: Boolean,
    enabled: Boolean,
    onSearchBarActivationChange: (Boolean) -> Unit,
    onDrawerStateChange: () -> Unit
) {

    val historySet by viewModel.historyStateFlow.collectAsStateWithLifecycle()

    var inputText by rememberSaveable {
        mutableStateOf("")
    }
    var expanded by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(expanded) {
        onSearchBarActivationChange(expanded)
    }

    val configuration = LocalConfiguration.current
    val orientation = remember(configuration) { configuration.orientation }

    fun search(text: String) {
        if (text.isNotEmpty()) {
            val newSet = historySet.toMutableSet()
            newSet.add(text)
            viewModel.putHistoryStringSet(newSet)
            viewModel.onListEvent(ListEvent.Search(text))
        } else {
            viewModel.onListEvent(
                ListEvent.Sort(
                    viewModel.dataStateFlow.value.noteOrder,
                    false,
                    null,
                    false
                )
            )
        }
        expanded = false
    }

    @Composable
    fun LeadingIcon() {
        if (!isLargeScreen) {
            AnimatedContent(targetState = expanded, label = "leading") {
                if (it) {
                    IconButton(onClick = { search(inputText) }) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search"
                        )
                    }
                } else {
                    IconButton(
                        enabled = enabled,
                        onClick = onDrawerStateChange
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = "Open Menu"
                        )
                    }
                }
            }
        } else {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search"
            )
        }
    }

    @Composable
    fun TrailingIcon() {
        AnimatedContent(targetState = expanded, label = "trailing") {
            if (it) {
                IconButton(onClick = {
                    if (inputText.isNotEmpty()) {
                        inputText = ""
                    } else {
                        search("")
                    }
                }) {
                    Icon(
                        imageVector = Icons.Outlined.Clear,
                        contentDescription = "Clear"
                    )
                }
            } else {
                IconButton(
                    enabled = enabled,
                    onClick = { viewModel.onListEvent(ListEvent.ToggleOrderSection) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Sort,
                        contentDescription = "Sort"
                    )
                }
            }
        }
    }

    @Composable
    fun History() {

        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = "History"
                )
            },
            headlineContent = { Text(text = stringResource(R.string.search_history)) },
            trailingContent = {
                // To align the icon with search bar, use icon.clickable{} instead of IconButton onClick()
                Icon(
                    modifier = Modifier.clickable {
                        viewModel.putHistoryStringSet(emptySet())
                    },
                    imageVector = Icons.Outlined.DeleteForever,
                    contentDescription = "Clear History"
                )
            }
        )

        FlowRow(
            Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            historySet.forEach {
                SuggestionChip(
                    modifier = Modifier.defaultMinSize(48.dp),
                    onClick = { inputText = it },
                    label = {
                        Text(
                            text = it,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    })
            }
        }
    }

    // Search bar layout, switch between docked and expanded search bar based on window size and orientation
    if (orientation == Configuration.ORIENTATION_PORTRAIT && !isLargeScreen) {

        // Animate search bar padding when active state changes
        val searchBarPadding by animateDpAsState(
            targetValue = if (expanded) 0.dp else 16.dp,
            label = "searchBarPadding"
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            SearchBar(
                modifier = Modifier.padding(horizontal = searchBarPadding),
                inputField = {
                    SearchBarDefaults.InputField(
                        query = inputText,
                        onQueryChange = { inputText = it },
                        onSearch = { search(it) },
                        enabled = enabled,
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        placeholder = { Text(text = stringResource(R.string.search)) },
                        leadingIcon = { LeadingIcon() },
                        trailingIcon = { TrailingIcon() },
                    )
                },
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                History()
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            DockedSearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = inputText,
                        onQueryChange = { inputText = it },
                        onSearch = { search(it) },
                        enabled = enabled,
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        placeholder = { Text(text = stringResource(R.string.search)) },
                        leadingIcon = { LeadingIcon() },
                        trailingIcon = { TrailingIcon() },
                    )
                },
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                History()
            }
        }
    }
}
