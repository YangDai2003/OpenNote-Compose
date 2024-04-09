package com.yangdai.opennote.presentation.viewmodel

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.domain.usecase.Operations
import com.yangdai.opennote.presentation.event.NoteEvent
import com.yangdai.opennote.presentation.state.NoteState
import com.yangdai.opennote.presentation.event.UiEvent
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
class NoteScreenViewModel @Inject constructor(
    private val operations: Operations,
    savedStateHandle: SavedStateHandle
) : ViewModel() {


    val textFieldState: TextFieldState = TextFieldState("")
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val html = snapshotFlow { textFieldState.text }
        .debounce(100)
        .mapLatest {
            val document: Node = parser.parse(it.toString())
            renderer.render(document) ?: ""
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )

    private val extensions: List<Extension> = listOf(
        TablesExtension.create(),
        StrikethroughExtension.create(),
        TaskListItemsExtension.create()
    )

    private val parser: Parser by lazy { Parser.builder().extensions(extensions).build() }
    private val renderer: HtmlRenderer by lazy {
        HtmlRenderer.builder().extensions(extensions).build()
    }

    private val _state = MutableStateFlow(NoteState())
    val stateFlow = _state.asStateFlow()

    private val _event = MutableSharedFlow<UiEvent>()
    val event = _event.asSharedFlow()

    private var queryFoldersJob: Job? = null

    private var oNote: NoteEntity? = null

    init {
        savedStateHandle.get<String>("id")?.let {
            val id = it.toLong()
            viewModelScope.launch {
                operations.findNote(id)?.let { note ->
                    oNote = note
                    textFieldState.setTextAndPlaceCursorAtEnd(note.content)
                    _state.update { noteState ->
                        noteState.copy(
                            id = note.id,
                            title = note.title,
                            content = note.content,
                            folderId = note.folderId,
                            isMarkdown = note.isMarkdown,
                            timestamp = note.timestamp
                        )
                    }
                }
            }
        }
        savedStateHandle.get<Intent>(NavController.KEY_DEEP_LINK_INTENT)?.let {
            val content = it.parseSharedContent().trim()
            if (content.isNotEmpty()) {
                textFieldState.setTextAndPlaceCursorAtEnd(content)
            }
        }
        getFolders()
    }

    fun addTask(task: String, checked: Boolean) {
        textFieldState.edit { addTask(task, checked) }
    }

    fun addLink(link: String) {
        textFieldState.edit { addLink(link) }
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
                viewModelScope.launch(Dispatchers.IO) {
                    val noteState = stateFlow.value
                    val note = NoteEntity(
                        id = noteState.id,
                        title = noteState.title,
                        content = textFieldState.text.toString(),
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
                    _event.emit(UiEvent.NavigateBack)
                }
            }

            NoteEvent.Delete -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val note = stateFlow.value
                    note.id?.let {
                        operations.updateNote(
                            NoteEntity(
                                id = it,
                                title = note.title,
                                content = note.content,
                                folderId = note.folderId,
                                isMarkdown = note.isMarkdown,
                                timestamp = System.currentTimeMillis(),
                                isDeleted = true
                            )
                        )
                    }
                    _event.emit(UiEvent.NavigateBack)
                }
            }

            is NoteEvent.TitleChanged -> {
                _state.update {
                    it.copy(
                        title = event.value
                    )
                }
            }

            NoteEvent.SwitchType -> {
                _state.update {
                    it.copy(
                        isMarkdown = it.isMarkdown.not()
                    )
                }
            }

            is NoteEvent.Edit -> {
                when (event.value) {
                    Constants.EDITOR_UNDO -> textFieldState.undoState.undo()
                    Constants.EDITOR_REDO -> textFieldState.undoState.redo()
                    Constants.EDITOR_TITLE -> textFieldState.edit { add("#") }
                    Constants.EDITOR_BOLD -> textFieldState.edit { bold() }
                    Constants.EDITOR_ITALIC -> textFieldState.edit { italic() }
                    Constants.EDITOR_UNDERLINE -> textFieldState.edit { underline() }
                    Constants.EDITOR_STRIKETHROUGH -> textFieldState.edit { strikeThrough() }
                    Constants.EDITOR_MARK -> textFieldState.edit { mark() }
                    Constants.EDITOR_INLINE_CODE -> textFieldState.edit { inlineCode() }
                    Constants.EDITOR_INLINE_FUNC -> textFieldState.edit { inlineFunction() }
                    Constants.EDITOR_QUOTE -> textFieldState.edit { quote() }
                    Constants.EDITOR_DIAGRAM -> textFieldState.edit { diagram() }
                }
            }
        }
    }
}
