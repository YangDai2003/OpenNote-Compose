package com.yangdai.opennote.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R
import com.yangdai.opennote.data.local.entity.FolderEntity
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderListSheet(
    hint: String = "",
    oFolderId: Long?,
    folders: ImmutableList<FolderEntity>,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onCloseClick: () -> Unit,
    onSelect: (Long?) -> Unit
) = ModalBottomSheet(
    windowInsets = BottomSheetDefaults.windowInsets.exclude(WindowInsets.statusBars),
    onDismissRequest = onDismissRequest,
    sheetState = sheetState
) {

    var selectedFolderId by remember { mutableStateOf(oFolderId) }

    if (hint.isNotEmpty()) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            maxLines = 1,
            text = hint
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCloseClick) {
            Icon(imageVector = Icons.Outlined.Close, contentDescription = "Cancel")
        }
        IconButton(onClick = {
            onSelect(selectedFolderId)
            onCloseClick()
        }) {
            Icon(imageVector = Icons.Outlined.Done, contentDescription = "Confirm")
        }
    }

    LazyColumn {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedFolderId = null
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    selected = null == selectedFolderId,
                    onClick = {

                    }
                )

                Icon(imageVector = Icons.Outlined.FolderOpen, contentDescription = "")

                Text(
                    text = stringResource(id = R.string.all_notes),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
        items(folders) { folder ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedFolderId = folder.id
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    selected = folder.id == selectedFolderId,
                    onClick = {

                    }
                )

                Icon(
                    imageVector = Icons.Outlined.FolderOpen,
                    tint = if (folder.color != null) Color(folder.color) else MaterialTheme.colorScheme.onSurface,
                    contentDescription = "Leading Icon"
                )

                Text(
                    text = folder.name,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
