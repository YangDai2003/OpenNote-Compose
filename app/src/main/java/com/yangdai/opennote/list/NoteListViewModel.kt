package com.yangdai.opennote.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yangdai.opennote.domain.use_case.NoteOrder
import com.yangdai.opennote.domain.use_case.NoteUseCases
import com.yangdai.opennote.domain.use_case.OrderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val noteUseCases: NoteUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(NotesState())
    val stateFlow = _state.asStateFlow()

    private var queryNotesJob: Job? = null

    init {
        getNotes(NoteOrder.Date(OrderType.Descending))
    }

    fun onEvent(event: ListEvent) {
        when (event) {
            is ListEvent.Order -> {
                if (stateFlow.value.noteOrder::class == event.noteOrder::class &&
                    stateFlow.value.noteOrder.orderType == event.noteOrder.orderType
                ) {
                    return
                }
                getNotes(event.noteOrder)
            }

            is ListEvent.DeleteNote -> {
                viewModelScope.launch {
                    noteUseCases.deleteNote(event.id)
                }
            }


            is ListEvent.ToggleOrderSection -> {
                _state.value = stateFlow.value.copy(
                    isOrderSectionVisible = !stateFlow.value.isOrderSectionVisible
                )
            }

            is ListEvent.Search -> searchNotes(event.key)
        }
    }

    private fun getNotes(noteOrder: NoteOrder) {
        queryNotesJob?.cancel()
        queryNotesJob = noteUseCases.getAllNotes(noteOrder)
            .onEach { notes ->
                _state.value = stateFlow.value.copy(
                    notes = notes,
                    noteOrder = noteOrder
                )
            }
            .launchIn(viewModelScope)
    }

    private fun searchNotes(keyWord: String) {
        queryNotesJob?.cancel()
        queryNotesJob = noteUseCases.searchNotes(keyWord)
            .onEach { notes ->
                _state.value = stateFlow.value.copy(
                    notes = notes
                )
            }
            .launchIn(viewModelScope)
    }

}