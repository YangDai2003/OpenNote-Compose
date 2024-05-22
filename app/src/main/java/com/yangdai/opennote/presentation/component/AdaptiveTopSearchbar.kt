package com.yangdai.opennote.presentation.component

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ContextualFlowRowOverflow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material.icons.outlined.ViewAgenda
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
import androidx.compose.runtime.mutableIntStateOf
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
import com.yangdai.opennote.presentation.util.Constants
import com.yangdai.opennote.presentation.util.Constants.DEFAULT_MAX_LINES
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdaptiveTopSearchbar(
    viewModel: SharedViewModel = hiltViewModel(LocalContext.current as MainActivity),
    isLargeScreen: Boolean,
    enabled: Boolean,
    onSearchBarActivationChange: (Boolean) -> Unit,
    onDrawerStateChange: () -> Unit
) {

    val historySet by viewModel.historyStateFlow.collectAsStateWithLifecycle()
    val settingsState by viewModel.settingsStateFlow.collectAsStateWithLifecycle()

    var inputText by rememberSaveable {
        mutableStateOf("")
    }
    var expanded by rememberSaveable {
        mutableStateOf(false)
    }
    var maxLines by rememberSaveable {
        mutableIntStateOf(DEFAULT_MAX_LINES)
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
            viewModel.putPreferenceValue(Constants.Preferences.SEARCH_HISTORY, newSet.toSet())
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

    AdaptiveSearchBar(
        isDocked = orientation != Configuration.ORIENTATION_PORTRAIT || isLargeScreen,
        expanded = expanded,
        onExpandedChange = { expanded = it },
        inputField = {
            SearchBarDefaults.InputField(
                query = inputText,
                onQueryChange = { inputText = it },
                onSearch = { search(it) },
                enabled = enabled,
                expanded = expanded,
                onExpandedChange = { expanded = it },
                placeholder = { Text(text = stringResource(R.string.search)) },
                leadingIcon = {
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
                },
                trailingIcon = {
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { viewModel.onListEvent(ListEvent.ChangeViewMode) }) {
                                    Icon(
                                        imageVector = if (!settingsState.isListView) Icons.Outlined.ViewAgenda else Icons.Outlined.GridView,
                                        contentDescription = "View Mode"
                                    )
                                }
                                IconButton(onClick = { viewModel.onListEvent(ListEvent.ToggleOrderSection) }) {
                                    Icon(
                                        imageVector = Icons.Outlined.SortByAlpha,
                                        contentDescription = "Sort"
                                    )
                                }
                            }
                        }
                    }
                },
            )
        }
    ) {

        if (historySet.isEmpty()) return@AdaptiveSearchBar

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
                Icon(
                    modifier = Modifier.clickable {
                        viewModel.putPreferenceValue(
                            Constants.Preferences.SEARCH_HISTORY,
                            setOf<String>()
                        )
                    },
                    imageVector = Icons.Outlined.DeleteForever,
                    contentDescription = "Clear History"
                )
            }
        )

        ContextualFlowRow(
            itemCount = historySet.size,
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            maxLines = maxLines,
            overflow = ContextualFlowRowOverflow.expandOrCollapseIndicator(
                minRowsToShowCollapse = DEFAULT_MAX_LINES + 1,
                expandIndicator = {
                    IconButton(onClick = { maxLines = Int.MAX_VALUE }) {
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = "Expand"
                        )
                    }
                },
                collapseIndicator = {
                    IconButton(onClick = { maxLines = DEFAULT_MAX_LINES }) {
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowUp,
                            contentDescription = "Collapse"
                        )
                    }
                }
            )
        ) { index ->
            SuggestionChip(
                modifier = Modifier.defaultMinSize(48.dp),
                onClick = { inputText = historySet.elementAt(index) },
                label = {
                    Text(
                        text = historySet.elementAt(index),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                })
        }
    }
}

/**
 * A composable function that adapts the search bar based on the screen size and orientation.
 *
 * @param isDocked A boolean indicating whether the search bar is docked or not.
 * @param expanded A boolean indicating whether the search bar is expanded or not.
 * @param onExpandedChange A callback that is called when the search bar is expanded or collapsed.
 * @param inputField The input field of the search bar.
 * @param content The content of the search bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveSearchBar(
    isDocked: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    inputField: @Composable () -> Unit,
    content: @Composable (ColumnScope.() -> Unit)
) = if (!isDocked) {

    // Animate search bar padding when active state changes
    val searchBarPadding by animateDpAsState(
        targetValue = if (expanded) 0.dp else 16.dp,
        label = "searchBarPadding"
    )

    SearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = searchBarPadding),
        inputField = inputField,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        content = content
    )
} else {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        DockedSearchBar(
            inputField = inputField,
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            content = content
        )
    }
}