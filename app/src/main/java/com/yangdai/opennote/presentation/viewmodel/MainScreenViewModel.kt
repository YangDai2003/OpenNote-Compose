package com.yangdai.opennote.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.domain.usecase.NoteOrder
import com.yangdai.opennote.domain.usecase.Operations
import com.yangdai.opennote.domain.usecase.OrderType
import com.yangdai.opennote.domain.repository.DataStoreRepository
import com.yangdai.opennote.presentation.event.FolderEvent
import com.yangdai.opennote.presentation.event.ListEvent
import com.yangdai.opennote.presentation.state.ListState
import com.yangdai.opennote.presentation.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val operations: Operations,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ListState())
    val stateFlow = _state.asStateFlow()

    private var queryNotesJob: Job? = null
    private var queryFoldersJob: Job? = null

    init {
        getNotes(NoteOrder.Date(OrderType.Descending))
        getFolders()
    }

    fun onListEvent(event: ListEvent) {
        when (event) {

            is ListEvent.Sort -> getNotes(event.noteOrder, event.trash, event.filterFolder, event.folderId)

            is ListEvent.DeleteNotesByIds -> {
                viewModelScope.launch {
                    if (event.recycle) {
                        event.ids.forEach {
                            val note = operations.findNote(it)
                            if (note != null) {
                                operations.updateNote(
                                    NoteEntity(
                                        id = note.id,
                                        title = note.title,
                                        content = note.content,
                                        folderId = note.folderId,
                                        isDeleted = true,
                                        timestamp = note.timestamp
                                    )
                                )
                            }
                        }
                    } else {
                        event.ids.forEach {
                            operations.deleteNoteById(it)
                        }
                    }
                }
            }

            is ListEvent.RestoreNotes -> {
                viewModelScope.launch {
                    event.ids.forEach {
                        val note = operations.findNote(it)
                        if (note != null)
                            operations.updateNote(
                                NoteEntity(
                                    id = note.id,
                                    title = note.title,
                                    content = note.content,
                                    folderId = note.folderId,
                                    isDeleted = false,
                                    timestamp = note.timestamp
                                )
                            )
                    }
                }
            }

            is ListEvent.ToggleOrderSection -> {
                _state.value = stateFlow.value.copy(
                    isOrderSectionVisible = !stateFlow.value.isOrderSectionVisible
                )
            }

            is ListEvent.Search -> searchNotes(event.key)
            is ListEvent.MoveNotes -> {
                viewModelScope.launch {
                    event.ids.forEach {
                        val note = operations.findNote(it)
                        if (note != null) {
                            operations.updateNote(
                                NoteEntity(
                                    id = note.id,
                                    title = note.title,
                                    content = note.content,
                                    folderId = event.folderId,
                                    isDeleted = false,
                                    timestamp = note.timestamp
                                )
                            )
                        }
                    }
                }
            }

            is ListEvent.DeleteNotesByFolderId -> {
                viewModelScope.launch {
                    operations.deleteNotesByFolderId(event.folderId)
                }
            }
        }
    }

    fun onFolderEvent(event: FolderEvent) {
        when (event) {
            is FolderEvent.AddFolder -> {
                viewModelScope.launch {
                    operations.addFolder(event.folder)
                }
            }

            is FolderEvent.DeleteFolder -> {
                viewModelScope.launch {
                    operations.deleteFolder(event.id)
                }

            }

            is FolderEvent.UpdateFolder -> {
                viewModelScope.launch {
                    operations.updateFolder(event.folder)
                }

            }
        }
    }

    private fun getNotes(
        noteOrder: NoteOrder,
        trash: Boolean = false,
        filterFolder: Boolean = false,
        folderId: Long? = null,
    ) {
        queryNotesJob?.cancel()
        queryNotesJob = operations.getNotes(noteOrder, trash, filterFolder, folderId)
            .onEach { notes ->
                _state.value = stateFlow.value.copy(
                    notes = notes,
                    noteOrder = noteOrder,
                    trash = trash,
                    filterFolder = filterFolder,
                    folderId = folderId
                )
            }
            .launchIn(viewModelScope)
    }

    private fun getFolders() {
        queryFoldersJob?.cancel()
        queryFoldersJob = operations.getFolders()
            .onEach { folders ->
                _state.value = stateFlow.value.copy(
                    folders = folders
                )
            }
            .launchIn(viewModelScope)
    }

    private fun searchNotes(keyWord: String) {
        queryNotesJob?.cancel()
        queryNotesJob = operations.searchNotes(keyWord)
            .onEach { notes ->
                _state.value = stateFlow.value.copy(
                    notes = notes
                )
            }
            .launchIn(viewModelScope)
    }

    fun putHistoryStringSet(value: Set<String>) {
        viewModelScope.launch {
            dataStoreRepository.putStringSet(Constants.HISTORY, value)
        }
    }

    val historyStateFlow: StateFlow<Set<String>> = getData()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = setOf()
        )

    private fun getData(): Flow<Set<String>> = dataStoreRepository.stringSetFlow(Constants.HISTORY)
}
