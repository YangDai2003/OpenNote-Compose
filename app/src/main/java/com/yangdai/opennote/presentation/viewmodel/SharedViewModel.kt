package com.yangdai.opennote.presentation.viewmodel

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.presentation.util.Constants.APP_COLOR
import com.yangdai.opennote.presentation.util.Constants.APP_THEME
import com.yangdai.opennote.presentation.util.Constants.NEED_PASSWORD
import com.yangdai.opennote.presentation.state.SettingsState
import com.yangdai.opennote.domain.repository.DataStoreRepository
import com.yangdai.opennote.domain.usecase.NoteOrder
import com.yangdai.opennote.domain.usecase.Operations
import com.yangdai.opennote.domain.usecase.OrderType
import com.yangdai.opennote.presentation.event.FolderEvent
import com.yangdai.opennote.presentation.event.ListEvent
import com.yangdai.opennote.presentation.event.NoteEvent
import com.yangdai.opennote.presentation.event.UiEvent
import com.yangdai.opennote.presentation.state.DataActionState
import com.yangdai.opennote.presentation.state.DataState
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
import com.yangdai.opennote.presentation.util.quote
import com.yangdai.opennote.presentation.util.strikeThrough
import com.yangdai.opennote.presentation.util.underline
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.commonmark.Extension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalFoundationApi::class)
@HiltViewModel
class SharedViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val operations: Operations
) : ViewModel() {

    // 起始页加载状态，初始值为 true
    private val _isLoadingData = MutableStateFlow(true)
    val isLoadingDate = _isLoadingData.asStateFlow()

    // 列表状态, 包含笔记列表、文件夹列表、排序方式等
    private val _dataState = MutableStateFlow(DataState())
    val dataStateFlow = _dataState.asStateFlow()

    // 笔记状态, 包含笔记的 id、文件夹 id、是否为 Markdown 笔记、时间戳等
    private val _noteState = MutableStateFlow(NoteState())
    val noteStateFlow = _noteState.asStateFlow()

    val foldersStateFlow = operations.getFolders()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // UI 事件
    private val _event = MutableSharedFlow<UiEvent>()
    val event = _event.asSharedFlow()

    // 查询笔记的任务
    private var queryNotesJob: Job? = null

    // Markdown 解析器和渲染器
    private lateinit var extensions: List<Extension>
    private lateinit var parser: Parser
    private lateinit var renderer: HtmlRenderer
    lateinit var textRecognizer: TextRecognizer

    // 笔记标题和内容的状态
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

    // 当前笔记的初始化状态，用于比较是否有修改
    private var oNote: NoteEntity? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // 延迟显示主屏幕，等待数据加载完成
            extensions = listOf(
                TablesExtension.create(),
                StrikethroughExtension.create(),
                TaskListItemsExtension.create()
            )
            parser = Parser.builder().extensions(extensions).build()
            renderer = HtmlRenderer.builder().extensions(extensions).build()
            textRecognizer = if (Locale.getDefault().language == Locale.CHINESE.language) {
                TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            } else {
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            }
            delay(500)
            //任务完成后将 isLoading 设置为 false 以隐藏启动屏幕
            _isLoadingData.value = false
        }
        getNotes()
    }

    // Setting Section
    val settingsStateFlow: StateFlow<SettingsState> = combine(
        dataStoreRepository.intFlow(APP_THEME),
        dataStoreRepository.intFlow(APP_COLOR),
        dataStoreRepository.booleanFlow(NEED_PASSWORD),
    ) { theme, color, needPassword ->
        SettingsState(
            theme = theme,
            color = color,
            needPassword = needPassword
        )
    }.flowOn(Dispatchers.IO).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsState()
    )

    fun preferencesFlow(): Flow<Preferences> {
        return dataStoreRepository.preferencesFlow()
    }

    fun <T> putPreferenceValue(key: String, value: T) {
        viewModelScope.launch(Dispatchers.IO) {
            when (value) {
                is Int -> dataStoreRepository.putInt(key, value)
                is Boolean -> dataStoreRepository.putBoolean(key, value)
                else -> throw IllegalArgumentException("Unsupported value type")
            }
        }
    }

    fun putHistoryStringSet(value: Set<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.putStringSet(Constants.HISTORY, value)
        }
    }

    fun getInt(key: String): Int? = runBlocking {
        dataStoreRepository.getInt(key)
    }

    fun getBoolean(key: String): Boolean? = runBlocking {
        dataStoreRepository.getBoolean(key)
    }

    fun getDataStore() = dataStoreRepository.getDataStore()


    val historyStateFlow: StateFlow<Set<String>> =
        dataStoreRepository.stringSetFlow(Constants.HISTORY)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = setOf()
            )


    // Event Section

    fun onListEvent(event: ListEvent) {
        when (event) {

            is ListEvent.Sort -> getNotes(
                event.noteOrder,
                event.trash,
                event.filterFolder,
                event.folderId
            )

            is ListEvent.DeleteNotes -> {
                viewModelScope.launch(Dispatchers.IO) {
                    if (event.recycle) {
                        event.noteEntities.forEach {
                            operations.updateNote(
                                NoteEntity(
                                    id = it.id,
                                    title = it.title,
                                    content = it.content,
                                    folderId = it.folderId,
                                    isMarkdown = it.isMarkdown,
                                    isDeleted = true,
                                    timestamp = it.timestamp
                                )
                            )
                        }
                    } else {
                        event.noteEntities.forEach {
                            operations.deleteNote(it)
                        }
                    }
                }
            }

            is ListEvent.RestoreNotes -> {
                viewModelScope.launch(Dispatchers.IO) {
                    event.noteEntities.forEach {
                        operations.updateNote(
                            NoteEntity(
                                id = it.id,
                                title = it.title,
                                content = it.content,
                                folderId = it.folderId,
                                isMarkdown = it.isMarkdown,
                                isDeleted = false,
                                timestamp = it.timestamp
                            )
                        )
                    }
                }
            }

            is ListEvent.ToggleOrderSection -> {
                _dataState.update {
                    it.copy(
                        isOrderSectionVisible = !it.isOrderSectionVisible
                    )
                }
            }

            is ListEvent.Search -> searchNotes(event.key)
            is ListEvent.MoveNotes -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val folderId = event.folderId
                    event.noteEntities.forEach {
                        operations.updateNote(
                            NoteEntity(
                                id = it.id,
                                title = it.title,
                                content = it.content,
                                folderId = folderId,
                                isMarkdown = it.isMarkdown,
                                isDeleted = false,
                                timestamp = it.timestamp
                            )
                        )
                    }
                }
            }

            is ListEvent.OpenNote -> {
                val note = event.noteEntity
                oNote = note
                _noteState.update { noteState ->
                    noteState.copy(
                        id = note.id,
                        folderId = note.folderId,
                        isMarkdown = note.isMarkdown,
                        timestamp = note.timestamp
                    )
                }
                titleState.setTextAndPlaceCursorAtEnd(note.title)
                contentState.setTextAndPlaceCursorAtEnd(note.content)
            }

            ListEvent.AddNote -> {
                titleState.clearText()
                contentState.clearText()
                _noteState.value = NoteState()
                oNote = null
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
                    operations.deleteNotesByFolderId(event.folder.id)
                    operations.deleteFolder(event.folder)
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
                _dataState.update {
                    it.copy(
                        notes = notes,
                        noteOrder = noteOrder,
                        filterTrash = trash,
                        filterFolder = filterFolder,
                        folderId = folderId
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private val _dataActionState = MutableStateFlow(DataActionState())
    val dataActionState = _dataActionState.asStateFlow()
    private var addNotesJob: Job? = null
    fun addNotes(folderId: Long?, uriList: List<Uri>, contentResolver: ContentResolver) {
        _dataActionState.update { it.copy(loading = true) }
        addNotesJob?.cancel()
        addNotesJob = viewModelScope.launch(Dispatchers.IO) {
            uriList.forEach { uri ->
                _dataActionState.update {
                    it.copy(
                        progress = uriList.indexOf(uri).toFloat() / uriList.size
                    )
                }
                val inputStream = contentResolver.openInputStream(uri)
                val fileName = getFileName(contentResolver, uri)
                val content = inputStream?.bufferedReader().use { it?.readText() }
                val note = NoteEntity(
                    title = fileName ?: "",
                    content = content ?: "",
                    folderId = folderId,
                    isMarkdown = (fileName?.endsWith(".md") == true) || (fileName?.endsWith(".markdown") == true),
                    timestamp = System.currentTimeMillis()
                )
                operations.addNote(note)
            }
            _dataActionState.update {
                it.copy(
                    progress = 1f
                )
            }
        }
    }

    fun cancelAddNotes() {
        addNotesJob?.cancel()
        _dataActionState.value = DataActionState()
    }

    // 获取文件名的函数
    @SuppressLint("Range")
    fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor.use {
                if (it != null && it.moveToFirst()) {
                    result = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result
    }

    private fun searchNotes(keyWord: String) {
        queryNotesJob?.cancel()
        queryNotesJob = operations.searchNotes(keyWord)
            .flowOn(Dispatchers.IO)
            .onEach { notes ->
                _dataState.update {
                    it.copy(
                        notes = notes
                    )
                }
            }
            .launchIn(viewModelScope)
    }


    fun addTask(task: String, checked: Boolean) {
        contentState.edit { addTask(task, checked) }
    }

    fun addLink(link: String) {
        contentState.edit { addLink(link) }
    }

    fun addText(text: String) {
        contentState.edit { add(text) }
    }

    fun canUndo() = contentState.undoState.canUndo

    fun canRedo() = contentState.undoState.canRedo

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
