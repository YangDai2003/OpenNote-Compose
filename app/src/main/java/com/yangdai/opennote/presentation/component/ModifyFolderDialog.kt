package com.yangdai.opennote.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R
import com.yangdai.opennote.data.local.entity.FolderEntity

@Composable
fun ModifyFolderDialog(
    showDialog: Boolean,
    folder: FolderEntity,
    onDismissRequest: () -> Unit,
    onModify: (FolderEntity) -> Unit
) {

    if (!showDialog) return

    var text by remember { mutableStateOf(folder.name) }
    var color by remember { mutableStateOf(folder.color) }

    val initValue =
        if (folder.color == null) 0 else FolderEntity.folderColors.indexOf(Color(folder.color)) + 1
    var selectedIndex by remember { mutableIntStateOf(initValue) }

    AlertDialog(
        title = {
            Text(text = stringResource(R.string.modify))
        },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    placeholder = { Text(text = stringResource(R.string.name)) },
                )
                LazyRow(
                    modifier = Modifier.padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    items(FolderEntity.folderColors.size + 1) {
                        if (it == 0) {
                            ColoredCircle2(selected = 0 == selectedIndex,
                                onClick = { selectedIndex = 0 })
                        } else {
                            ColoredCircle(
                                color = FolderEntity.folderColors[it - 1],
                                selected = it == selectedIndex,
                                onClick = { selectedIndex = it }
                            )
                        }
                    }
                }
            }
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {

                    color = if (selectedIndex == 0) null
                    else FolderEntity.folderColors[selectedIndex - 1].toArgb()

                    onModify(
                        FolderEntity(
                            id = folder.id,
                            name = text,
                            color = color
                        )
                    )

                    onDismissRequest()
                }
            ) {
                Text(stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        }
    )
}

@Composable
fun ColoredCircle(color: Color, selected: Boolean, onClick: () -> Unit) {

    val background = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .size(50.dp)
            .drawBehind {
                if (selected)
                    drawCircle(
                        color = background
                    )
            }
            .clip(shape = CircleShape)
            .clickable(onClick = onClick)
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .clip(shape = CircleShape)
                .background(color = color)
        )
    }
}

@Composable
fun ColoredCircle2(selected: Boolean, onClick: () -> Unit) {

    val background = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .size(50.dp)
            .drawBehind {
                if (selected)
                    drawCircle(
                        color = background
                    )
            }
            .clip(shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "A")
    }
}