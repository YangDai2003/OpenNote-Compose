package com.yangdai.opennote.presentation.component.note

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationSearching
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R

@Stable
data class FindAndReplaceState(
    val searchWord: String = "",
    val replaceWord: String = "",
    val matchCount: Int = 0,
    val scrollDirection: ScrollDirection? = null,
    val replaceType: ReplaceType? = null
)

enum class ScrollDirection {
    NEXT, PREVIOUS
}

enum class ReplaceType {
    ALL, CURRENT
}

@Composable
fun FindAndReplaceField(
    isStandard: Boolean,
    state: FindAndReplaceState,
    onStateUpdate: (FindAndReplaceState) -> Unit
) = Column(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
) {
    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        CustomTextField(
            modifier = Modifier.weight(1f),
            value = state.searchWord,
            onValueChange = { onStateUpdate(state.copy(searchWord = it)) },
            leadingIcon = Icons.Outlined.LocationSearching,
            suffix = {
                Text(
                    text = if (state.searchWord.isBlank()) "" else state.matchCount.toString(),
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            },
            placeholderText = stringResource(R.string.find)
        )
        if (isStandard) {
            IconButton(onClick = {
                onStateUpdate(state.copy(scrollDirection = ScrollDirection.PREVIOUS))
            }) {
                Icon(
                    imageVector = Icons.Outlined.ArrowUpward, contentDescription = "PREVIOUS"
                )
            }
            IconButton(onClick = {
                onStateUpdate(state.copy(scrollDirection = ScrollDirection.NEXT))
            }) {
                Icon(
                    imageVector = Icons.Outlined.ArrowDownward, contentDescription = "Next"
                )
            }
        }

        IconButton(onClick = {
            onStateUpdate(
                state.copy(
                    searchWord = "",
                    replaceWord = ""
                )
            )
        }) {
            Icon(
                imageVector = Icons.Outlined.Close, contentDescription = "Clear"
            )
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        CustomTextField(
            modifier = Modifier
                .padding(end = 8.dp)
                .weight(1f),
            value = state.replaceWord,
            onValueChange = { onStateUpdate(state.copy(replaceWord = it)) },
            leadingIcon = Icons.Outlined.Autorenew,
            placeholderText = stringResource(R.string.replace)
        )
        IconButton(onClick = { onStateUpdate(state.copy(replaceType = ReplaceType.CURRENT)) }) {
            Icon(
                painter = painterResource(id = R.drawable.replace),
                contentDescription = "Replace"
            )
        }
        IconButton(onClick = { onStateUpdate(state.copy(replaceType = ReplaceType.ALL)) }) {
            Icon(
                painter = painterResource(id = R.drawable.replace_all),
                contentDescription = "Replace all"
            )
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}

// 由于OutlinedTextField有诡异的边距和大小，因此自定义BasicTextField来实现
@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: ImageVector? = null,
    suffix: @Composable (() -> Unit)? = null,
    placeholderText: String = ""
) {
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }
    BasicTextField(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small
            )
            .border(
                width = 2.dp,
                color = if (isFocused) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            )
            .onFocusChanged {
                isFocused = it.isFocused
            },
        value = value,
        onValueChange = {
            onValueChange(it)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Row(
                modifier
                    .fillMaxWidth()
                    .padding(horizontal = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.8f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) Text(
                        modifier = Modifier.align(Alignment.CenterStart),
                        text = placeholderText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                    innerTextField()
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (suffix != null) suffix()
            }
        })
}
