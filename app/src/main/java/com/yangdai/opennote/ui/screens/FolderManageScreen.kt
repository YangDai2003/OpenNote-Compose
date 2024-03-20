package com.yangdai.opennote.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yangdai.opennote.note.NoteEvent
import com.yangdai.opennote.note.NoteState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderManageScreen(
    state: NoteState,
    onEvent: (NoteEvent) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "Folder Manage") }, navigationIcon = {
                IconButton(onClick = { onEvent(NoteEvent.NavigateBack) }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "")
                }
            }, actions = {
                IconButton(onClick = { onEvent(NoteEvent.Delete) }) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "")
                }
            })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            OutlinedTextField(
                value = state.title,
                onValueChange = { onEvent(NoteEvent.TitleChanged(it)) })

            OutlinedTextField(
                value = state.content,
                onValueChange = { onEvent(NoteEvent.ContentChanged(it)) })

        }
    }
}