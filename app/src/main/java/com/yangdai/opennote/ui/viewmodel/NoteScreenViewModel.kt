package com.yangdai.opennote.ui.viewmodel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.TextFieldState
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.domain.operations.Operations
import com.yangdai.opennote.ui.event.NoteEvent
import com.yangdai.opennote.ui.state.NoteState
import com.yangdai.opennote.ui.event.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalFoundationApi::class)
@HiltViewModel
class NoteScreenViewModel @Inject constructor(
    private val operations: Operations,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var textFieldState: TextFieldState = TextFieldState("")

    private val _state = MutableStateFlow(NoteState())
    val stateFlow = _state.asStateFlow()

    private val _event = Channel<UiEvent>()
    val event = _event.receiveAsFlow()

    private var queryFoldersJob: Job? = null

    private fun sendEvent(event: UiEvent) {
        viewModelScope.launch {
            _event.send(event)
        }
    }

    private var oTitle = ""
    private var oContent = ""
    private var oFolderId: Long? = null

    init {
        savedStateHandle.get<String>("id")?.let {
            val id = it.toLong()
            viewModelScope.launch {
                operations.findNote(id)?.let { note ->
                    oTitle = note.title
                    oContent = note.content
                    oFolderId = note.folderId
                    textFieldState = TextFieldState(note.content)
                    _state.update { noteState ->
                        noteState.copy(
                            id = note.id,
                            title = note.title,
                            content = note.content,
                            folderId = note.folderId,
                            timestamp = note.timestamp
                        )
                    }
                }
            }
        }
        getFolders()
    }

    fun undo() {
        textFieldState.undoState.undo()
    }

    fun redo() {
        textFieldState.undoState.redo()
    }

    fun addLink(link: String) {
        val initialSelection = textFieldState.text.selectionInChars
        textFieldState.edit {
            replace(initialSelection.min, initialSelection.max, link)
            selectCharsIn(
                TextRange(
                    initialSelection.min,
                    initialSelection.min + link.length
                )
            )
        }
    }

    fun addScannedText(text: String) {
        textFieldState.edit {
            append(text)
        }
    }

    fun canUndo() = textFieldState.undoState.canUndo

    fun canRedo() = textFieldState.undoState.canRedo

    private fun getFolders() {
        queryFoldersJob?.cancel()
        queryFoldersJob = operations.getFolders()
            .onEach { folders ->
                _state.update {
                    it.copy(folders = folders)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: NoteEvent) {
        when (event) {

            is NoteEvent.FolderChanged -> {
                _state.update {
                    it.copy(
                        folderId = event.value
                    )
                }
            }

            is NoteEvent.NavigateBack -> {
                viewModelScope.launch {
                    val noteState = stateFlow.value
                    val note = NoteEntity(
                        id = noteState.id,
                        title = noteState.title,
                        content = textFieldState.text.toString(),
                        folderId = noteState.folderId,
                        timestamp = System.currentTimeMillis()
                    )
                    if (note.id == null) {
                        if (note.title.isNotEmpty() || note.content.isNotEmpty()) {
                            operations.addNote(note)
                        }
                    } else {
                        if (note.title != oTitle || note.content != oContent || note.folderId != oFolderId)
                            operations.updateNote(note)
                    }
                    sendEvent(UiEvent.NavigateBack)
                }
            }

            NoteEvent.Delete -> {
                viewModelScope.launch {
                    val note = stateFlow.value
                    note.id?.let {
                        operations.updateNote(
                            NoteEntity(
                                id = it,
                                title = note.title,
                                content = note.content,
                                folderId = note.folderId,
                                timestamp = System.currentTimeMillis(),
                                isDeleted = true
                            )
                        )
                    }
                    sendEvent(UiEvent.NavigateBack)
                }
            }

            is NoteEvent.TitleChanged -> {
                _state.update {
                    it.copy(
                        title = event.value
                    )
                }
            }
        }
    }
}