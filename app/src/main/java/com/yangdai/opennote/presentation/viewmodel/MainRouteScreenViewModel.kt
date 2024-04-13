package com.yangdai.opennote.presentation.viewmodel

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.domain.usecase.NoteOrder
import com.yangdai.opennote.domain.usecase.Operations
import com.yangdai.opennote.domain.usecase.OrderType
import com.yangdai.opennote.domain.repository.DataStoreRepository
import com.yangdai.opennote.presentation.event.FolderEvent
import com.yangdai.opennote.presentation.event.ListEvent
import com.yangdai.opennote.presentation.event.NoteEvent
import com.yangdai.opennote.presentation.event.UiEvent
import com.yangdai.opennote.presentation.state.ListState
import com.yangdai.opennote.presentation.state.NoteState
import com.yangdai.opennote.presentation.util.Constants
import com.yangdai.opennote.presentation.util.add
import com.yangdai.opennote.presentation.util.addLink
import com.yangdai.opennote.presentation.util.addTask
import com.yangdai.opennote.presentation.util.bold
import com.yangdai.opennote.presentation.util.diagram
import com.yangdai.opennote.presentation.util.inlineCode
import com.yangdai.opennote.presentation.util.inlineFunction
import com.yangdai.opennote.presentation.util.italic
import com.yangdai.opennote.presentation.util.mark
import com.yangdai.opennote.presentation.util.parseSharedContent
import com.yangdai.opennote.presentation.util.quote
import com.yangdai.opennote.presentation.util.strikeThrough
import com.yangdai.opennote.presentation.util.underline
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.commonmark.Extension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import javax.inject.Inject

@OptIn(ExperimentalFoundationApi::class)
@HiltViewModel
class MainRouteScreenViewModel @Inject constructor(
    private val operations: Operations,
    private val dataStoreRepository: DataStoreRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _listState = MutableStateFlow(ListState())
    val listStateFlow = _listState.asStateFlow()

    private var queryNotesJob: Job? = null
    private var queryFoldersJob: Job? = null

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()
    fun setLoading(value: Boolean) {
        _isLoading.value = value
    }

    private val extensions: List<Extension> by lazy {
        listOf(
            TablesExtension.create(),
            StrikethroughExtension.create(),
            TaskListItemsExtension.create()
        )
    }
    private val parser: Parser by lazy {
        Parser.builder().extensions(extensions).build()
    }
    private val renderer: HtmlRenderer by lazy {
        HtmlRenderer.builder().extensions(extensions).build()
    }

    val titleState = TextFieldState()
    val contentState = TextFieldState()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val html = snapshotFlow { contentState.text }
        .debounce(100)
        .mapLatest {
            val document: Node = parser.parse(it.toString())
            renderer.render(document) ?: ""
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )

    private val _noteState = MutableStateFlow(NoteState())
    val noteStateFlow = _noteState.asStateFlow()

    private val _event = MutableSharedFlow<UiEvent>()
    val event = _event.asSharedFlow()

    private var oNote: NoteEntity? = null

    fun getNoteById(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            operations.findNote(id)?.let { note ->
                oNote = note
                withContext(Dispatchers.Main) {
                    titleState.setTextAndPlaceCursorAtEnd(note.title)
                    contentState.setTextAndPlaceCursorAtEnd(note.content)
                    _noteState.update { noteState ->
                        noteState.copy(
                            id = note.id,
                            folderId = note.folderId,
                            isMarkdown = note.isMarkdown,
                            timestamp = note.timestamp
                        )
                    }
                    _isLoading.value = false
                }
            }
        }
    }

    init {
        savedStateHandle.get<Intent>(NavController.KEY_DEEP_LINK_INTENT)?.let {
            val content = it.parseSharedContent().trim()
            if (content.isNotEmpty()) {
                contentState.setTextAndPlaceCursorAtEnd(content)
            }
        }
        getNotes()
        getFolders()
    }

    fun onListEvent(event: ListEvent) {
        when (event) {

            is ListEvent.Sort -> getNotes(
                event.noteOrder,
                event.trash,
                event.filterFolder,
                event.folderId
            )

            is ListEvent.DeleteNotesByIds -> {
                viewModelScope.launch(Dispatchers.IO) {
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
                                        isMarkdown = note.isMarkdown,
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
                viewModelScope.launch(Dispatchers.IO) {
                    event.ids.forEach {
                        val note = operations.findNote(it)
                        if (note != null)
                            operations.updateNote(
                                NoteEntity(
                                    id = note.id,
                                    title = note.title,
                                    content = note.content,
                                    folderId = note.folderId,
                                    isMarkdown = note.isMarkdown,
                                    isDeleted = false,
                                    timestamp = note.timestamp
                                )
                            )
                    }
                }
            }

            is ListEvent.ToggleOrderSection -> {
                _listState.value = listStateFlow.value.copy(
                    isOrderSectionVisible = !listStateFlow.value.isOrderSectionVisible
                )
            }

            is ListEvent.Search -> searchNotes(event.key)
            is ListEvent.MoveNotes -> {
                viewModelScope.launch(Dispatchers.IO) {
                    event.ids.forEach {
                        val note = operations.findNote(it)
                        if (note != null) {
                            operations.updateNote(
                                NoteEntity(
                                    id = note.id,
                                    title = note.title,
                                    content = note.content,
                                    folderId = event.folderId,
                                    isMarkdown = note.isMarkdown,
                                    isDeleted = false,
                                    timestamp = note.timestamp
                                )
                            )
                        }
                    }
                }
            }

            is ListEvent.DeleteNotesByFolderId -> {
                viewModelScope.launch(Dispatchers.IO) {
                    operations.deleteNotesByFolderId(event.folderId)
                }
            }
        }
    }

    fun onFolderEvent(event: FolderEvent) {
        when (event) {
            is FolderEvent.AddFolder -> {
                viewModelScope.launch(Dispatchers.IO) {
                    operations.addFolder(event.folder)
                }
            }

            is FolderEvent.DeleteFolder -> {
                viewModelScope.launch(Dispatchers.IO) {
                    operations.deleteFolder(event.id)
                }
            }

            is FolderEvent.UpdateFolder -> {
                viewModelScope.launch(Dispatchers.IO) {
                    operations.updateFolder(event.folder)
                }
            }
        }
    }

    private fun getNotes(
        noteOrder: NoteOrder = NoteOrder.Date(OrderType.Descending),
        trash: Boolean = false,
        filterFolder: Boolean = false,
        folderId: Long? = null,
    ) {
        queryNotesJob?.cancel()
        queryNotesJob = operations.getNotes(noteOrder, trash, filterFolder, folderId)
            .flowOn(Dispatchers.IO)
            .onEach { notes ->
                _listState.value = listStateFlow.value.copy(
                    notes = notes,
                    noteOrder = noteOrder,
                    filterTrash = trash,
                    filterFolder = filterFolder,
                    folderId = folderId
                )
            }
            .launchIn(viewModelScope)
    }

    private fun getFolders() {
        queryFoldersJob?.cancel()
        queryFoldersJob = operations.getFolders()
            .flowOn(Dispatchers.IO)
            .onEach { folders ->
                _listState.value = listStateFlow.value.copy(
                    folders = folders
                )
            }
            .launchIn(viewModelScope)
    }

    private fun searchNotes(keyWord: String) {
        queryNotesJob?.cancel()
        queryNotesJob = operations.searchNotes(keyWord)
            .flowOn(Dispatchers.IO)
            .onEach { notes ->
                _listState.value = listStateFlow.value.copy(
                    notes = notes
                )
            }
            .launchIn(viewModelScope)
    }

    fun putHistoryStringSet(value: Set<String>) {
        viewModelScope.launch(Dispatchers.IO) {
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

    fun addTask(task: String, checked: Boolean) {
        contentState.edit { addTask(task, checked) }
    }

    fun addLink(link: String) {
        contentState.edit { addLink(link) }
    }

    fun addScannedText(text: String) {
        contentState.edit { add(text) }
    }

    fun canUndo() = contentState.undoState.canUndo

    fun canRedo() = contentState.undoState.canRedo

    private fun resetState() {
        titleState.clearText()
        contentState.clearText()
        oNote = null
        _noteState.value = NoteState()
    }

    fun onNoteEvent(event: NoteEvent) {
        when (event) {

            is NoteEvent.FolderChanged -> {
                _noteState.update {
                    it.copy(
                        folderId = event.value
                    )
                }
            }

            is NoteEvent.NavigateBack -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val noteState = noteStateFlow.value
                    val note = NoteEntity(
                        id = noteState.id,
                        title = titleState.text.toString(),
                        content = contentState.text.toString(),
                        folderId = noteState.folderId,
                        isMarkdown = noteState.isMarkdown,
                        timestamp = System.currentTimeMillis()
                    )
                    if (note.id == null) {
                        if (note.title.isNotEmpty() || note.content.isNotEmpty()) {
                            operations.addNote(note)
                        }
                    } else {
                        if (note.title != oNote?.title || note.content != oNote?.content || note.isMarkdown != oNote?.isMarkdown || note.folderId != oNote?.folderId)
                            operations.updateNote(note)
                    }
                    resetState()
                }
            }

            NoteEvent.Delete -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val note = noteStateFlow.value
                    note.id?.let {
                        operations.updateNote(
                            NoteEntity(
                                id = it,
                                title = titleState.text.toString(),
                                content = contentState.text.toString(),
                                folderId = note.folderId,
                                isMarkdown = note.isMarkdown,
                                timestamp = System.currentTimeMillis(),
                                isDeleted = true
                            )
                        )
                    }
                    _event.emit(UiEvent.NavigateBack)
                    resetState()
                }
            }

            NoteEvent.SwitchType -> {
                _noteState.update {
                    it.copy(
                        isMarkdown = it.isMarkdown.not()
                    )
                }
            }

            is NoteEvent.Edit -> {
                when (event.value) {
                    Constants.EDITOR_UNDO -> contentState.undoState.undo()
                    Constants.EDITOR_REDO -> contentState.undoState.redo()
                    Constants.EDITOR_TITLE -> contentState.edit { add("#") }
                    Constants.EDITOR_BOLD -> contentState.edit { bold() }
                    Constants.EDITOR_ITALIC -> contentState.edit { italic() }
                    Constants.EDITOR_UNDERLINE -> contentState.edit { underline() }
                    Constants.EDITOR_STRIKETHROUGH -> contentState.edit { strikeThrough() }
                    Constants.EDITOR_MARK -> contentState.edit { mark() }
                    Constants.EDITOR_INLINE_CODE -> contentState.edit { inlineCode() }
                    Constants.EDITOR_INLINE_FUNC -> contentState.edit { inlineFunction() }
                    Constants.EDITOR_QUOTE -> contentState.edit { quote() }
                    Constants.EDITOR_DIAGRAM -> contentState.edit { diagram() }
                }
            }
        }
    }
}
