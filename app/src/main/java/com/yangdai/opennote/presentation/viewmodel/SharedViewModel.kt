package com.yangdai.opennote.presentation.viewmodel

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.yangdai.opennote.data.local.Database
import com.yangdai.opennote.data.local.entity.BackupData
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.domain.repository.DataStoreRepository
import com.yangdai.opennote.domain.usecase.NoteOrder
import com.yangdai.opennote.domain.usecase.OrderType
import com.yangdai.opennote.domain.usecase.UseCases
import com.yangdai.opennote.presentation.component.dialog.ExportType
import com.yangdai.opennote.presentation.component.dialog.TaskItem
import com.yangdai.opennote.presentation.event.DatabaseEvent
import com.yangdai.opennote.presentation.event.FolderEvent
import com.yangdai.opennote.presentation.event.ListEvent
import com.yangdai.opennote.presentation.event.NoteEvent
import com.yangdai.opennote.presentation.event.UiEvent
import com.yangdai.opennote.presentation.state.AppColor
import com.yangdai.opennote.presentation.state.AppTheme
import com.yangdai.opennote.presentation.state.DataActionState
import com.yangdai.opennote.presentation.state.DataState
import com.yangdai.opennote.presentation.state.NoteState
import com.yangdai.opennote.presentation.state.SettingsState
import com.yangdai.opennote.presentation.util.Constants
import com.yangdai.opennote.presentation.util.add
import com.yangdai.opennote.presentation.util.addHeader
import com.yangdai.opennote.presentation.util.addMermaid
import com.yangdai.opennote.presentation.util.addRule
import com.yangdai.opennote.presentation.util.addTable
import com.yangdai.opennote.presentation.util.addTask
import com.yangdai.opennote.presentation.util.bold
import com.yangdai.opennote.presentation.util.inlineBraces
import com.yangdai.opennote.presentation.util.inlineBrackets
import com.yangdai.opennote.presentation.util.inlineCode
import com.yangdai.opennote.presentation.util.inlineMath
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.commonmark.Extension
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.footnotes.FootnotesExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension
import org.commonmark.ext.image.attributes.ImageAttributesExtension
import org.commonmark.ext.ins.InsExtension
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.io.OutputStreamWriter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val database: Database,
    private val dataStoreRepository: DataStoreRepository,
    private val useCases: UseCases
) : ViewModel() {

    // 起始页加载状态，初始值为 true
    val isLoading: StateFlow<Boolean>
        field = MutableStateFlow(true)

    // 列表状态, 包含笔记列表、文件夹列表、排序方式等
    val dataStateFlow: StateFlow<DataState>
        field = MutableStateFlow(DataState())

    // 笔记状态, 包含笔记的 id、文件夹 id、是否为 Markdown 笔记、时间戳等
    val noteStateFlow: StateFlow<NoteState>
        field = MutableStateFlow(NoteState())

    val foldersStateFlow = useCases.getFolders()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // UI 事件
    val uiEventFlow: SharedFlow<UiEvent>
        field = MutableSharedFlow<UiEvent>()

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
        .mapLatest { renderer.render(parser.parse(it.toString())) }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ""
        )

    // 当前笔记的初始化状态，用于比较是否有修改
    private var _oNote: NoteEntity = NoteEntity(timestamp = System.currentTimeMillis())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // 延迟显示主屏幕，等待数据加载完成
            extensions = listOf(
                TablesExtension.create(),
                AutolinkExtension.create(),
                FootnotesExtension.create(),
                HeadingAnchorExtension.create(),
                InsExtension.create(),
                ImageAttributesExtension.create(),
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
            delay(300)
            //任务完成后将 isLoading 设置为 false 以隐藏启动屏幕
            isLoading.value = false
        }
        getNotes()
    }

    // Setting Section
    val settingsStateFlow: StateFlow<SettingsState> = combine(
        dataStoreRepository.intFlow(Constants.Preferences.APP_THEME),
        dataStoreRepository.intFlow(Constants.Preferences.APP_COLOR),
        dataStoreRepository.booleanFlow(Constants.Preferences.NEED_PASSWORD),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_APP_IN_DARK_MODE),
        dataStoreRepository.booleanFlow(Constants.Preferences.SHOULD_FOLLOW_SYSTEM),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_SWITCH_ACTIVE),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_LIST_VIEW),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_APP_IN_AMOLED_MODE)
    ) { values ->
        SettingsState(
            theme = AppTheme.fromInt(values[0] as Int),
            color = AppColor.fromInt(values[1] as Int),
            needPassword = values[2] as Boolean,
            isAppInDarkMode = values[3] as Boolean,
            shouldFollowSystem = values[4] as Boolean,
            isSwitchActive = values[5] as Boolean,
            isListView = values[6] as Boolean,
            isAppInAmoledMode = values[7] as Boolean
        )
    }.flowOn(Dispatchers.IO).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsState()
    )

    fun <T> putPreferenceValue(key: String, value: T) {
        viewModelScope.launch(Dispatchers.IO) {
            when (value) {
                is Int -> dataStoreRepository.putInt(key, value)
                is Float -> dataStoreRepository.putFloat(key, value)
                is Boolean -> dataStoreRepository.putBoolean(key, value)
                is String -> dataStoreRepository.putString(key, value)
                is Set<*> -> dataStoreRepository.putStringSet(
                    key,
                    value.filterIsInstance<String>().toSet()
                )

                else -> throw IllegalArgumentException("Unsupported value type")
            }
        }
    }

    val historyStateFlow: StateFlow<Set<String>> =
        dataStoreRepository.stringSetFlow(Constants.Preferences.SEARCH_HISTORY)
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
                            useCases.updateNote(
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
                            useCases.deleteNote(it)
                        }
                    }
                }
            }

            is ListEvent.RestoreNotes -> {
                viewModelScope.launch(Dispatchers.IO) {
                    event.noteEntities.forEach {
                        useCases.updateNote(
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

            is ListEvent.ChangeViewMode -> {
                viewModelScope.launch(Dispatchers.IO) {
                    dataStoreRepository.putBoolean(
                        Constants.Preferences.IS_LIST_VIEW,
                        settingsStateFlow.value.isListView.not()
                    )
                }
            }

            is ListEvent.Search -> searchNotes(event.key)
            is ListEvent.MoveNotes -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val folderId = event.folderId
                    event.noteEntities.forEach {
                        useCases.updateNote(
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
                _oNote = event.noteEntity
            }

            ListEvent.ToggleOrderSection -> {
                dataStateFlow.update {
                    it.copy(
                        isOrderSectionVisible = it.isOrderSectionVisible.not()
                    )
                }
            }

            is ListEvent.AddNoteToFolder -> {
                _oNote = NoteEntity(
                    folderId = event.folderId,
                    timestamp = System.currentTimeMillis()
                )
            }
        }
    }

    fun onFolderEvent(event: FolderEvent) {
        when (event) {
            is FolderEvent.AddFolder -> {
                viewModelScope.launch(Dispatchers.IO) {
                    useCases.addFolder(event.folder)
                }
            }

            is FolderEvent.DeleteFolder -> {
                viewModelScope.launch(Dispatchers.IO) {
                    useCases.deleteNotesByFolderId(event.folder.id)
                    useCases.deleteFolder(event.folder)
                }
            }

            is FolderEvent.UpdateFolder -> {
                viewModelScope.launch(Dispatchers.IO) {
                    useCases.updateFolder(event.folder)
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
        queryNotesJob = useCases.getNotes(noteOrder, trash, filterFolder, folderId)
            .flowOn(Dispatchers.IO)
            .onEach { notes ->
                dataStateFlow.update {
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


    private fun searchNotes(keyWord: String) {
        queryNotesJob?.cancel()
        queryNotesJob = useCases.searchNotes(keyWord)
            .flowOn(Dispatchers.IO)
            .onEach { notes ->
                dataStateFlow.update {
                    it.copy(
                        notes = notes
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun addTable(row: Int, column: Int) {
        contentState.edit { addTable(row, column) }
    }

    fun addTasks(taskList: List<TaskItem>) {
        taskList.forEach {
            contentState.edit { addTask(it.task, it.checked) }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    fun onNoteEvent(event: NoteEvent) {
        when (event) {

            is NoteEvent.FolderChanged -> {
                noteStateFlow.update {
                    it.copy(
                        folderId = event.value
                    )
                }
            }

            is NoteEvent.Save -> {
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
                    useCases.addNote(note)
                    uiEventFlow.emit(UiEvent.NavigateBack)
                }
            }

            NoteEvent.Delete -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val note = noteStateFlow.value
                    note.id?.let {
                        useCases.updateNote(
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
                    uiEventFlow.emit(UiEvent.NavigateBack)
                }
            }

            NoteEvent.SwitchType -> {
                noteStateFlow.update {
                    it.copy(
                        isMarkdown = it.isMarkdown.not()
                    )
                }
            }

            is NoteEvent.Edit -> {
                when (event.key) {
                    Constants.Editor.UNDO -> contentState.undoState.undo()
                    Constants.Editor.REDO -> contentState.undoState.redo()
                    Constants.Editor.H1 -> contentState.edit { addHeader(1) }
                    Constants.Editor.H2 -> contentState.edit { addHeader(2) }
                    Constants.Editor.H3 -> contentState.edit { addHeader(3) }
                    Constants.Editor.H4 -> contentState.edit { addHeader(4) }
                    Constants.Editor.H5 -> contentState.edit { addHeader(5) }
                    Constants.Editor.H6 -> contentState.edit { addHeader(6) }
                    Constants.Editor.BOLD -> contentState.edit { bold() }
                    Constants.Editor.ITALIC -> contentState.edit { italic() }
                    Constants.Editor.UNDERLINE -> contentState.edit { underline() }
                    Constants.Editor.STRIKETHROUGH -> contentState.edit { strikeThrough() }
                    Constants.Editor.MARK -> contentState.edit { mark() }
                    Constants.Editor.INLINE_CODE -> contentState.edit { inlineCode() }
                    Constants.Editor.INLINE_BRACKETS -> contentState.edit { inlineBrackets() }
                    Constants.Editor.INLINE_BRACES -> contentState.edit { inlineBraces() }
                    Constants.Editor.INLINE_MATH -> contentState.edit { inlineMath() }
                    Constants.Editor.QUOTE -> contentState.edit { quote() }
                    Constants.Editor.RULE -> contentState.edit { addRule() }
                    Constants.Editor.DIAGRAM -> contentState.edit { addMermaid() }
                    Constants.Editor.TEXT -> contentState.edit { add(event.value) }
                    Constants.Editor.TITLE -> titleState.edit { add(event.value) }
                }
            }

            is NoteEvent.Load -> {
                // 判断id是否与oNote的id相同，不同则从数据库获取笔记，并更新oNote。
                viewModelScope.launch(Dispatchers.IO) {
                    if (event.id != (_oNote.id ?: -1L))
                        _oNote = useCases.getNoteById(event.id)
                            ?: NoteEntity(timestamp = System.currentTimeMillis())
                    noteStateFlow.update { noteState ->
                        noteState.copy(
                            id = _oNote.id,
                            folderId = _oNote.folderId,
                            isMarkdown = _oNote.isMarkdown,
                            timestamp = _oNote.timestamp
                        )
                    }
                    titleState.setTextAndPlaceCursorAtEnd(_oNote.title)
                    contentState.setTextAndPlaceCursorAtEnd(_oNote.content)
                }
            }

            NoteEvent.Update -> {
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
                    if (note.id != null)
                        if (note.title != _oNote.title || note.content != _oNote.content || note.isMarkdown != _oNote.isMarkdown || note.folderId != _oNote.folderId)
                            useCases.updateNote(note)
                }
            }
        }
    }

    private val _dataActionState = MutableStateFlow(DataActionState())
    val dataActionStateFlow = _dataActionState.asStateFlow()
    private var dataActionJob: Job? = null
    fun cancelDataAction() {
        dataActionJob?.cancel()
        _dataActionState.value = DataActionState()
    }

    // 获取文件名的函数
    @SuppressLint("Range")
    fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
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

    fun onDatabaseEvent(event: DatabaseEvent) {
        when (event) {

            is DatabaseEvent.Import -> {

                val contentResolver = event.contentResolver
                val folderId = event.folderId
                val uriList = event.uriList

                dataActionJob?.cancel()
                _dataActionState.update { it.copy(loading = true) }
                dataActionJob = viewModelScope.launch(Dispatchers.IO) {
                    uriList.forEachIndexed { index, uri ->
                        _dataActionState.update {
                            it.copy(progress = index.toFloat() / uriList.size)
                        }

                        val fileName = getFileName(contentResolver, uri)

                        contentResolver.openInputStream(uri).use {
                            it?.bufferedReader().use { reader ->
                                val content = reader?.readText()
                                val note = NoteEntity(
                                    title = fileName?.substringBeforeLast(".") ?: "",
                                    content = content ?: "",
                                    folderId = folderId,
                                    isMarkdown = (fileName?.endsWith(".md") == true)
                                            || (fileName?.endsWith(".markdown") == true
                                            || (fileName?.endsWith(".html") == true)),
                                    timestamp = System.currentTimeMillis()
                                )
                                useCases.addNote(note)
                            }
                        }
                    }
                    _dataActionState.update {
                        it.copy(progress = 1f)
                    }
                }
            }

            is DatabaseEvent.Export -> {

                val contentResolver = event.contentResolver
                val notes = event.notes
                val type = event.type

                dataActionJob?.cancel()
                _dataActionState.update { it.copy(loading = true) }

                val extension = when (type) {
                    ExportType.TXT -> ".txt"
                    ExportType.MARKDOWN -> ".md"
                    else -> ".html"
                }

                dataActionJob = viewModelScope.launch(Dispatchers.IO) {
                    notes.forEachIndexed { index, noteEntity ->
                        _dataActionState.update {
                            it.copy(progress = index.toFloat() / notes.size)
                        }

                        val fileName = noteEntity.title
                        val content =
                            if (".html" != extension) noteEntity.content
                            else renderer.render(parser.parse(noteEntity.content))

                        val values = ContentValues().apply {
                            put(MediaStore.Downloads.DISPLAY_NAME, "$fileName$extension")
                            put(MediaStore.Downloads.MIME_TYPE, "text/*")
                            put(
                                MediaStore.Downloads.RELATIVE_PATH,
                                "${Environment.DIRECTORY_DOWNLOADS}/${Constants.File.OPENNOTE}"
                            )
                        }

                        val uri = contentResolver.insert(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                            values
                        )

                        uri?.let { uri1 ->
                            runCatching {
                                contentResolver.openOutputStream(uri1)?.use { outputStream ->
                                    OutputStreamWriter(outputStream).use { writer ->
                                        writer.write(content)
                                    }
                                }
                            }.onFailure { throwable ->
                                _dataActionState.update {
                                    it.copy(
                                        error = "Failed to export note: ${throwable.localizedMessage ?: "error"}"
                                    )
                                }
                            }
                        }
                    }
                    _dataActionState.update {
                        it.copy(progress = 1f)
                    }
                }
            }

            is DatabaseEvent.Backup -> {

                val contentResolver = event.contentResolver

                dataActionJob?.cancel()
                _dataActionState.update { it.copy(loading = true) }

                _dataActionState.update {
                    it.copy(progress = 0.2f)
                }
                dataActionJob = viewModelScope.launch(Dispatchers.IO) {
                    val notes = useCases.getNotes().first()
                    val folders = useCases.getFolders().first()
                    val backupData = BackupData(notes, folders)
                    val json = Json.encodeToString(backupData)
                    _dataActionState.update {
                        it.copy(progress = 0.5f)
                    }
                    // 创建 ContentValues 对象
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, "${System.currentTimeMillis()}.json")
                        put(MediaStore.Downloads.MIME_TYPE, "application/json")
                        put(
                            MediaStore.Downloads.RELATIVE_PATH,
                            "${Environment.DIRECTORY_DOWNLOADS}/${Constants.File.OPENNOTE_BACKUP}"
                        )
                    }

                    // 获取 Uri
                    val uri =
                        contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)

                    // 将 JSON 字符串写入到文件中
                    uri?.let {
                        contentResolver.openOutputStream(it)?.use { outputStream ->
                            OutputStreamWriter(outputStream).use { writer ->
                                writer.write(json)
                            }
                        }
                    }

                    _dataActionState.update {
                        it.copy(progress = 1f)
                    }
                }
            }

            is DatabaseEvent.Recovery -> {
                val contentResolver = event.contentResolver
                val uri = event.uri

                dataActionJob?.cancel()
                _dataActionState.update { it.copy(loading = true) }

                _dataActionState.update {
                    it.copy(progress = 0.2f)
                }

                dataActionJob = viewModelScope.launch(Dispatchers.IO) {

                    val json = contentResolver.openInputStream(uri)?.bufferedReader()
                        .use { it?.readText() }

                    _dataActionState.update {
                        it.copy(progress = 0.4f)
                    }

                    runCatching {
                        val backupData = Json.decodeFromString<BackupData>(json ?: "")
                        _dataActionState.update {
                            it.copy(progress = 0.6f)
                        }
                        backupData.folders.forEach { folderEntity ->
                            useCases.addFolder(folderEntity)
                        }
                        backupData.notes.forEach { noteEntity ->
                            useCases.addNote(noteEntity)
                        }
                    }.onFailure {
                        // 兼容旧版本
                        runCatching {
                            val notes = Json.decodeFromString<List<NoteEntity>>(json ?: "")
                            notes.forEachIndexed { _, noteEntity ->
                                useCases.addNote(noteEntity)
                            }
                        }.onFailure { throwable ->
                            _dataActionState.update {
                                it.copy(
                                    error = "Failed to decode backup data: ${throwable.localizedMessage ?: "error"}"
                                )
                            }
                        }.onSuccess {
                            _dataActionState.update {
                                it.copy(progress = 1f)
                            }
                        }
                    }.onSuccess {
                        _dataActionState.update {
                            it.copy(progress = 1f)
                        }
                    }
                }
            }

            DatabaseEvent.Reset -> {
                dataActionJob?.cancel()
                _dataActionState.update { it.copy(loading = true, infinite = true) }
                dataActionJob = viewModelScope.launch(Dispatchers.IO) {
                    runCatching {
                        database.clearAllTables()
                    }.onSuccess {
                        _dataActionState.update {
                            it.copy(progress = 1f)
                        }
                    }.onFailure { throwable ->
                        _dataActionState.update {
                            it.copy(
                                error = "Failed to reset database: ${throwable.localizedMessage ?: "error"}"
                            )
                        }
                    }
                }
            }
        }
    }
}
