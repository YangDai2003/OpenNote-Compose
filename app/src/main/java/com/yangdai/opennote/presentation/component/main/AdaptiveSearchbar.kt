package com.yangdai.opennote.presentation.component.main

import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.ExpandedDockedSearchBar
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopSearchBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
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
import kotlinx.coroutines.launch

// 根据新api的实现，仍存在键盘闪烁bug
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveSearchbar2(
    isLargeScreen: Boolean,
    enabled: Boolean,
    viewModel: SharedViewModel = hiltViewModel(LocalActivity.current as MainActivity),
    searchBarState: SearchBarState,
    onDrawerStateChange: () -> Unit
) {
    val historySet by viewModel.historyStateFlow.collectAsStateWithLifecycle()
    val settingsState by viewModel.settingsStateFlow.collectAsStateWithLifecycle()

    val textFieldState = rememberTextFieldState()
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val orientation = remember(configuration) { configuration.orientation }

    fun search(text: String) {
        if (text.isNotEmpty()) {
            val newSet = historySet.toMutableSet()
            while (newSet.size > 30) {
                val iterator = newSet.iterator()
                if (iterator.hasNext()) {
                    iterator.next()
                    iterator.remove()
                } else {
                    break
                }
            }
            newSet.add(text)
            viewModel.putPreferenceValue(Constants.Preferences.SEARCH_HISTORY, newSet.toSet())
            viewModel.onListEvent(ListEvent.Search(text))
        } else {
            viewModel.onListEvent(
                ListEvent.Sort(
                    viewModel.mainScreenDataStateFlow.value.noteOrder,
                    false,
                    null,
                    false
                )
            )
        }
        scope.launch { searchBarState.animateToCollapsed() }
    }

    val inputField = @Composable {
        SearchBarDefaults.InputField(
            searchBarState = searchBarState,
            textFieldState = textFieldState,
            onSearch = { search(it) },
            placeholder = { Text(text = stringResource(R.string.search)) },
            leadingIcon = {
                if (!isLargeScreen) AnimatedContent(
                    targetState = searchBarState.currentValue == SearchBarValue.Expanded,
                    label = "leading"
                ) {
                    if (it) IconButton(onClick = { search(textFieldState.text.toString()) }) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search"
                        )
                    } else IconButton(
                        enabled = enabled,
                        onClick = onDrawerStateChange
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = "Open Menu"
                        )
                    }
                } else Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                AnimatedContent(
                    targetState = searchBarState.currentValue == SearchBarValue.Expanded,
                    label = "trailing"
                ) {
                    if (it) IconButton(onClick = {
                        if (textFieldState.text.isNotEmpty()) textFieldState.clearText()
                        else search("")
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Clear,
                            contentDescription = "Clear"
                        )
                    } else Row(
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
            },
        )
    }

    TopSearchBar(
        state = searchBarState,
        inputField = inputField
    )
    AdaptiveExpandedSearchBar(
        isDocked = orientation != Configuration.ORIENTATION_PORTRAIT || isLargeScreen,
        state = searchBarState,
        inputField = inputField
    ) {
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

        FlowRow(
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            maxLines = DEFAULT_MAX_LINES
        ) {
            historySet.reversed().forEach {
                SuggestionChip(
                    modifier = Modifier.defaultMinSize(48.dp),
                    onClick = { textFieldState.setTextAndPlaceCursorAtEnd(it) },
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdaptiveExpandedSearchBar(
    isDocked: Boolean,
    state: SearchBarState,
    inputField: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    AnimatedContent(isDocked) {
        if (it) ExpandedDockedSearchBar(state = state, inputField = inputField, content = content)
        else ExpandedFullScreenSearchBar(state = state, inputField = inputField, content = content)
    }
}