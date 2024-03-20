package com.yangdai.opennote.note

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(NoteState())
    val state = _state.asStateFlow()

    private val _event = Channel<UiEvent>()
    val event = _event.receiveAsFlow()

    private fun sendEvent(event: UiEvent) {
        viewModelScope.launch {
            _event.send(event)
        }
    }

    private var oTitle = ""
    private var oContent = ""

    init {
        savedStateHandle.get<String>("id")?.let {
            val id = it.toLong()
            viewModelScope.launch {
                repository.getNoteById(id)?.let { note ->
                    oTitle = note.title
                    oContent = note.content
                    _state.update { noteState ->
                        noteState.copy(
                            id = note.id,
                            title = note.title,
                            content = note.content,
                            timestamp = note.timestamp
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: NoteEvent) {
        when (event) {
            is NoteEvent.ContentChanged -> {
                _state.update {
                    it.copy(
                        content = event.value
                    )
                }
            }

            is NoteEvent.TitleChanged -> {
                _state.update {
                    it.copy(
                        title = event.value
                    )
                }
            }

            NoteEvent.NavigateBack -> {
                viewModelScope.launch {
                    val noteState = state.value
                    val note = NoteEntity(
                        id = noteState.id,
                        title = noteState.title,
                        content = noteState.content,
                        timestamp = System.currentTimeMillis()
                    )
                    if (noteState.id == null) {
                        if (noteState.title.isNotEmpty()) {
                            repository.insertNote(
                                note
                            )
                        }
                    } else {
                        if (note.title != oTitle || note.content != oContent)
                            repository.updateNote(
                                note
                            )
                    }
                    sendEvent(UiEvent.NavigateBack)
                }
            }

            NoteEvent.Delete -> {
                viewModelScope.launch {
                    val note = state.value
                    note.id?.let { repository.deleteNote(it) }
                    sendEvent(UiEvent.NavigateBack)
                }
            }
        }
    }
}