package com.yangdai.opennote.presentation.component

import android.content.ClipData
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.content.MediaType
import androidx.compose.foundation.content.consume
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.content.hasMediaType
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.util.add

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteEditTextField(
    modifier: Modifier,
    state: TextFieldState,
    readMode: Boolean,
    onFocusChanged: (Boolean) -> Unit
) = BasicTextField(
    // The contentReceiver modifier is used to receive text content from the clipboard or drag-and-drop operations.
    modifier = modifier.contentReceiver { transferableContent ->
        if (transferableContent.hasMediaType(MediaType.Text)) {
            transferableContent.consume { item: ClipData.Item ->
                val hasText = item.text.isNotEmpty()
                if (hasText) {
                    state.edit { add(item.text.toString()) }
                }
                hasText
            }
        }
        null
    }.onFocusChanged {
        onFocusChanged(it.isFocused)
    },
    readOnly = readMode,
    state = state,
    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
    decorator = { innerTextField ->
        Box {
            if (state.text.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.content),
                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            innerTextField()
        }
    }
)
