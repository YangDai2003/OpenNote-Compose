package com.yangdai.opennote.presentation.viewmodel

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.TextFieldState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.domain.usecase.Operations
import com.yangdai.opennote.presentation.event.NoteEvent
import com.yangdai.opennote.presentation.state.NoteState
import com.yangdai.opennote.presentation.event.UiEvent
import com.yangdai.opennote.presentation.util.Constants.MIME_TYPE_TEXT
import com.yangdai.opennote.presentation.util.addLink
import com.yangdai.opennote.presentation.util.addTask
import com.yangdai.opennote.presentation.util.bold
import com.yangdai.opennote.presentation.util.inlineCode
import com.yangdai.opennote.presentation.util.italic
import com.yangdai.opennote.presentation.util.mark
import com.yangdai.opennote.presentation.util.quote
import com.yangdai.opennote.presentation.util.strikeThrough
import com.yangdai.opennote.presentation.util.underline
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
import org.commonmark.Extension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import javax.inject.Inject

@OptIn(ExperimentalFoundationApi::class)
@HiltViewModel
class NoteScreenViewModel @Inject constructor(
    private val operations: Operations,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var textFieldState: TextFieldState = TextFieldState("")

    private fun Intent.parseSharedContent(): String {
        if (action != Intent.ACTION_SEND && action != Intent.ACTION_VIEW) return ""

        return if (isTextMimeType()) {
            getStringExtra(Intent.EXTRA_TEXT) ?: ""
        } else {
            ""
        }
    }

    private fun Intent.isTextMimeType() = type?.startsWith(MIME_TYPE_TEXT) == true

    private val extensions: List<Extension> = listOf(
        TablesExtension.create(),
        StrikethroughExtension.create(),
        TaskListItemsExtension.create()
    )

    val parser: Parser by lazy { Parser.builder().extensions(extensions).build() }
    val renderer: HtmlRenderer by lazy { HtmlRenderer.builder().extensions(extensions).build() }

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
    private var oIsMarkdown = true

    init {
        savedStateHandle.get<String>("id")?.let {
            val id = it.toLong()
            viewModelScope.launch {
                operations.findNote(id)?.let { note ->
                    oTitle = note.title
                    oContent = note.content
                    oFolderId = note.folderId
                    oIsMarkdown = note.isMarkdown
                    textFieldState = TextFieldState(note.content)
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
                textFieldState = TextFieldState(content)
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

    fun bold() {
        textFieldState.edit { bold() }
    }

    fun italic() {
        textFieldState.edit { italic() }
    }

    fun underline() {
        textFieldState.edit { underline() }
    }

    fun strikethrough() {
        textFieldState.edit { strikeThrough() }
    }

    fun mark() {
        textFieldState.edit { mark() }
    }

    fun inlineCode() {
        textFieldState.edit { inlineCode() }
    }

    fun quote() {
        textFieldState.edit { quote() }
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
                viewModelScope.launch {
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
                        if (note.title != oTitle || note.content != oContent || note.folderId != oFolderId || note.isMarkdown != oIsMarkdown)
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
                                isMarkdown = note.isMarkdown,
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

            NoteEvent.SwitchType -> {
                _state.update {
                    it.copy(
                        isMarkdown = it.isMarkdown.not()
                    )
                }
            }
        }
    }
}
