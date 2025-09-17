package com.yangdai.opennote.presentation.viewmodel

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.util.fastJoinToString
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yangdai.opennote.data.local.Database
import com.yangdai.opennote.data.local.entity.BackupData
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.domain.repository.AppDataStoreRepository
import com.yangdai.opennote.domain.usecase.NoteOrder
import com.yangdai.opennote.domain.usecase.OrderType
import com.yangdai.opennote.domain.usecase.UseCases
import com.yangdai.opennote.presentation.component.dialog.ExportType
import com.yangdai.opennote.presentation.component.dialog.TaskItem
import com.yangdai.opennote.presentation.component.note.HeaderNode
import com.yangdai.opennote.presentation.component.note.add
import com.yangdai.opennote.presentation.component.note.addHeader
import com.yangdai.opennote.presentation.component.note.addInNewLine
import com.yangdai.opennote.presentation.component.note.addMermaid
import com.yangdai.opennote.presentation.component.note.addRule
import com.yangdai.opennote.presentation.component.note.addTable
import com.yangdai.opennote.presentation.component.note.addTask
import com.yangdai.opennote.presentation.component.note.alert
import com.yangdai.opennote.presentation.component.note.bold
import com.yangdai.opennote.presentation.component.note.highlight
import com.yangdai.opennote.presentation.component.note.inlineBraces
import com.yangdai.opennote.presentation.component.note.inlineBrackets
import com.yangdai.opennote.presentation.component.note.inlineCode
import com.yangdai.opennote.presentation.component.note.inlineMath
import com.yangdai.opennote.presentation.component.note.italic
import com.yangdai.opennote.presentation.component.note.quote
import com.yangdai.opennote.presentation.component.note.strikeThrough
import com.yangdai.opennote.presentation.component.note.tab
import com.yangdai.opennote.presentation.component.note.unTab
import com.yangdai.opennote.presentation.component.note.underline
import com.yangdai.opennote.presentation.event.DatabaseEvent
import com.yangdai.opennote.presentation.event.FolderEvent
import com.yangdai.opennote.presentation.event.ListEvent
import com.yangdai.opennote.presentation.event.NoteEvent
import com.yangdai.opennote.presentation.event.UiEvent
import com.yangdai.opennote.presentation.state.AppColor
import com.yangdai.opennote.presentation.state.AppTheme
import com.yangdai.opennote.presentation.state.DataActionState
import com.yangdai.opennote.presentation.state.DataState
import com.yangdai.opennote.presentation.state.ListNoteContentDisplayMode
import com.yangdai.opennote.presentation.state.ListNoteContentOverflowStyle
import com.yangdai.opennote.presentation.state.ListNoteContentSize
import com.yangdai.opennote.presentation.state.NoteState
import com.yangdai.opennote.presentation.state.SettingsState
import com.yangdai.opennote.presentation.state.TextState
import com.yangdai.opennote.presentation.util.BackupManager
import com.yangdai.opennote.presentation.util.Constants
import com.yangdai.opennote.presentation.util.PARSER
import com.yangdai.opennote.presentation.util.decryptBackupDataWithCompatibility
import com.yangdai.opennote.presentation.util.encryptBackupData
import com.yangdai.opennote.presentation.util.extension.highlight.HighlightExtension
import com.yangdai.opennote.presentation.util.getFileName
import com.yangdai.opennote.presentation.util.getOrCreateDirectory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.commonmark.Extension
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.footnotes.FootnotesExtension
import org.commonmark.ext.front.matter.YamlFrontMatterExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension
import org.commonmark.ext.image.attributes.ImageAttributesExtension
import org.commonmark.ext.ins.InsExtension
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import org.commonmark.node.AbstractVisitor
import org.commonmark.node.Heading
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.io.OutputStreamWriter
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val database: Database,
    private val appDataStoreRepository: AppDataStoreRepository,
    private val useCases: UseCases
) : ViewModel() {

    val authenticated = MutableStateFlow(false)
    val isCreatingPassword = MutableStateFlow(false)
    val intent = MutableStateFlow<Intent?>(null)

    // 起始页加载状态，初始值为 true
    val isLoading: StateFlow<Boolean>
        field = MutableStateFlow(true)

    // 主屏幕展示的笔记列表状态, 包含笔记列表、排序方式
    val mainScreenDataStateFlow: StateFlow<DataState>
        field = MutableStateFlow(DataState())

    // 文件夹和文件夹内笔记数量为一个Pair的列表
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val folderWithNoteCountsFlow = useCases.getFolders().debounce(300).flatMapLatest { folders ->
        if (folders.isEmpty()) return@flatMapLatest flowOf(emptyList())
        else combine(
            folders.map { folder ->
                useCases.getNotesCountByFolderId(folder.id).map { count -> folder to count }
            }
        ) { countPairs -> countPairs.toList() }
    }.flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    // 编辑时的笔记状态, 包含笔记的 id、所属文件夹 id、格式、时间戳
    val noteStateFlow: StateFlow<NoteState>
        field = MutableStateFlow(NoteState())

    // UI 事件，用于导航
    val uiEventFlow: SharedFlow<UiEvent>
        field = MutableSharedFlow<UiEvent>()

    // 查询笔记的任务
    private var queryNotesJob: Job? = null

    // Markdown 解析器和渲染器
    private lateinit var extensions: List<Extension>
    private lateinit var parser: Parser
    private lateinit var renderer: HtmlRenderer

    // 被打开的笔记的标题和内容的状态，唯一的数据源
    val titleState = TextFieldState()
    val contentState = TextFieldState()
    val contentSnapshotFlow = snapshotFlow { contentState.text }

    // Markdown 渲染后的 HTML 内容
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val html = contentSnapshotFlow.debounce(100)
        .mapLatest {
            renderer.render(parser.parse(it.toString()))
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ""
        )

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val textState = contentSnapshotFlow.debounce(1000)
        .mapLatest { TextState.fromText(it) }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = TextState()
        )

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val outline = contentSnapshotFlow.debounce(1000)
        .mapLatest {
            val document = PARSER.parse(it.toString())
            val root = HeaderNode("", 0, IntRange.EMPTY)
            val headerStack = mutableListOf(root)
            document.accept(object : AbstractVisitor() {
                override fun visit(heading: Heading) {
                    val span = heading.sourceSpans.first()
                    val range = span.inputIndex until (span.inputIndex + span.length)

                    val title = it.substring(range).replace("#", "").trim()
                    val node = HeaderNode(title, heading.level, range)

                    while (headerStack.last().level >= heading.level) {
                        headerStack.removeAt(headerStack.lastIndex)
                    }
                    headerStack.last().children.add(node)
                    headerStack.add(node)

                    visitChildren(heading)
                }
            })
            root
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HeaderNode("", 0, IntRange.EMPTY)
        )

    // 当前笔记的初始化状态，用于比较是否有修改
    private var _oNote: NoteEntity = NoteEntity(
        timestamp = System.currentTimeMillis(),
        isMarkdown = appDataStoreRepository.getBooleanValue(
            Constants.Preferences.IS_DEFAULT_LITE_MODE, false
        ).not()
    )

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
                TaskListItemsExtension.create(),
                HighlightExtension.create(),
                YamlFrontMatterExtension.create()
            )
            parser = Parser.builder().extensions(extensions).build()
            renderer = HtmlRenderer.builder().extensions(extensions).build()
            delay(200L)
            //任务完成后将 isLoading 设置为 false 以隐藏启动屏幕
            isLoading.value = false
        }
        getNotes()
    }

    val settingsStateFlow: StateFlow<SettingsState> = combine<Any, SettingsState>(
        appDataStoreRepository.intFlow(Constants.Preferences.APP_THEME),
        appDataStoreRepository.intFlow(Constants.Preferences.APP_COLOR),
        appDataStoreRepository.booleanFlow(Constants.Preferences.BIOMETRIC_AUTH_ENABLED),
        appDataStoreRepository.booleanFlow(Constants.Preferences.IS_APP_IN_DARK_MODE),
        appDataStoreRepository.booleanFlow(Constants.Preferences.SHOULD_FOLLOW_SYSTEM),
        appDataStoreRepository.booleanFlow(Constants.Preferences.IS_SWITCH_ACTIVE),
        appDataStoreRepository.booleanFlow(Constants.Preferences.IS_LIST_VIEW),
        appDataStoreRepository.booleanFlow(Constants.Preferences.IS_APP_IN_AMOLED_MODE),
        appDataStoreRepository.booleanFlow(Constants.Preferences.IS_DEFAULT_VIEW_FOR_READING),
        appDataStoreRepository.booleanFlow(Constants.Preferences.IS_DEFAULT_LITE_MODE),
        appDataStoreRepository.booleanFlow(Constants.Preferences.IS_LINT_ACTIVE),
        appDataStoreRepository.stringFlow(Constants.Preferences.STORAGE_PATH),
        appDataStoreRepository.stringFlow(Constants.Preferences.DATE_FORMATTER),
        appDataStoreRepository.stringFlow(Constants.Preferences.TIME_FORMATTER),
        appDataStoreRepository.booleanFlow(Constants.Preferences.IS_SCREEN_PROTECTED),
        appDataStoreRepository.floatFlow(Constants.Preferences.FONT_SCALE),
        appDataStoreRepository.intFlow(BackupManager.BACKUP_FREQUENCY_KEY),
        appDataStoreRepository.stringFlow(Constants.Preferences.PASSWORD),
        appDataStoreRepository.intFlow(Constants.Preferences.ENUM_OVERFLOW_STYLE),
        appDataStoreRepository.intFlow(Constants.Preferences.ENUM_CONTENT_SIZE),
        appDataStoreRepository.intFlow(Constants.Preferences.ENUM_DISPLAY_MODE),
        appDataStoreRepository.booleanFlow(Constants.Preferences.IS_AUTO_SAVE_ENABLED),
        appDataStoreRepository.intFlow(Constants.Preferences.TITLE_ALIGN),
        appDataStoreRepository.booleanFlow(Constants.Preferences.SHOW_LINE_NUMBERS)
    ) { values ->
        SettingsState(
            theme = AppTheme.fromInt(values[0] as Int),
            color = AppColor.fromInt(values[1] as Int),
            biometricAuthEnabled = values[2] as Boolean,
            isAppInDarkMode = values[3] as Boolean,
            shouldFollowSystem = values[4] as Boolean,
            isSwitchActive = values[5] as Boolean,
            isListView = values[6] as Boolean,
            isAppInAmoledMode = values[7] as Boolean,
            isDefaultViewForReading = values[8] as Boolean,
            isDefaultLiteMode = values[9] as Boolean,
            isLintActive = values[10] as Boolean,
            storagePath = values[11] as String,
            dateFormatter = values[12] as String,
            timeFormatter = values[13] as String,
            isScreenProtected = values[14] as Boolean,
            fontScale = values[15] as Float,
            backupFrequency = values[16] as Int,
            password = values[17] as String,
            enumOverflowStyle = ListNoteContentOverflowStyle.fromInt(values[18] as Int),
            enumContentSize = ListNoteContentSize.fromInt(values[19] as Int),
            enumDisplayMode = ListNoteContentDisplayMode.fromInt(values[20] as Int),
            isAutoSaveEnabled = values[21] as Boolean,
            titleAlignment = values[22] as Int,
            showLineNumbers = values[23] as Boolean
        )
    }.flowOn(Dispatchers.IO).stateIn(
        scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = SettingsState()
    )

    fun <T> putPreferenceValue(key: String, value: T) {
        viewModelScope.launch(Dispatchers.IO) {
            when (value) {
                is Int -> appDataStoreRepository.putInt(key, value)
                is Float -> appDataStoreRepository.putFloat(key, value)
                is Boolean -> appDataStoreRepository.putBoolean(key, value)
                is String -> appDataStoreRepository.putString(key, value)
                is Set<*> -> appDataStoreRepository.putStringSet(
                    key, value.filterIsInstance<String>().toSet()
                )

                else -> throw IllegalArgumentException("Unsupported value type")
            }
        }
    }

    val historyStateFlow: StateFlow<Set<String>> =
        appDataStoreRepository.stringSetFlow(Constants.Preferences.SEARCH_HISTORY).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = setOf()
        )

    fun onListEvent(event: ListEvent) {
        when (event) {

            is ListEvent.Sort -> getNotes(
                event.noteOrder, event.trash, event.filterFolder, event.folderId
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
                    appDataStoreRepository.putBoolean(
                        Constants.Preferences.IS_LIST_VIEW, settingsStateFlow.value.isListView.not()
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

            ListEvent.ToggleOrderSection -> {
                mainScreenDataStateFlow.update {
                    it.copy(
                        isOrderSectionVisible = it.isOrderSectionVisible.not()
                    )
                }
            }

            is ListEvent.OpenOrCreateNote -> {
                titleState.clearText()
                contentState.clearText()
                // 直接获取点击的note，避免在NoteEvent.Load中再次从数据库加载
                _oNote = event.noteEntity ?: NoteEntity(
                    folderId = event.folderId,
                    timestamp = System.currentTimeMillis(),
                    isMarkdown = appDataStoreRepository.getBooleanValue(
                        Constants.Preferences.IS_DEFAULT_LITE_MODE, false
                    ).not()
                )
            }
        }
    }

    fun onFolderEvent(event: FolderEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            when (event) {
                is FolderEvent.AddFolder -> {
                    useCases.addFolder(event.folder)
                }

                is FolderEvent.DeleteFolder -> {
                    useCases.deleteNotesByFolderId(event.folder.id)
                    useCases.deleteFolder(event.folder)
                }

                is FolderEvent.UpdateFolder -> {
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
        queryNotesJob = viewModelScope.launch {
            useCases.getNotes(noteOrder, trash, filterFolder, folderId)
                .distinctUntilChanged()
                .collect { notes ->
                    mainScreenDataStateFlow.update {
                        it.copy(
                            notes = notes,
                            noteOrder = noteOrder,
                            filterTrash = trash,
                            filterFolder = filterFolder,
                            folderId = folderId
                        )
                    }
                }
        }
    }

    private fun searchNotes(keyWord: String) {
        queryNotesJob?.cancel()
        queryNotesJob = useCases.searchNotes(keyWord).flowOn(Dispatchers.IO).onEach { notes ->
            mainScreenDataStateFlow.update {
                it.copy(
                    notes = notes
                )
            }
        }.launchIn(viewModelScope)
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
                                isMarkdown = note.isStandard,
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
                        isStandard = it.isStandard.not()
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
                    Constants.Editor.MARK -> contentState.edit { highlight() }
                    Constants.Editor.INLINE_CODE -> contentState.edit { inlineCode() }
                    Constants.Editor.INLINE_BRACKETS -> contentState.edit { inlineBrackets() }
                    Constants.Editor.INLINE_BRACES -> contentState.edit { inlineBraces() }
                    Constants.Editor.INLINE_MATH -> contentState.edit { inlineMath() }
                    Constants.Editor.QUOTE -> contentState.edit { quote() }
                    Constants.Editor.NOTE -> contentState.edit { alert(event.key.uppercase()) }
                    Constants.Editor.TIP -> contentState.edit { alert(event.key.uppercase()) }
                    Constants.Editor.IMPORTANT -> contentState.edit { alert(event.key.uppercase()) }
                    Constants.Editor.WARNING -> contentState.edit { alert(event.key.uppercase()) }
                    Constants.Editor.CAUTION -> contentState.edit { alert(event.key.uppercase()) }
                    Constants.Editor.TAB -> contentState.edit { tab() }
                    Constants.Editor.UN_TAB -> contentState.edit { unTab() }
                    Constants.Editor.RULE -> contentState.edit { addRule() }
                    Constants.Editor.DIAGRAM -> contentState.edit { addMermaid() }
                    Constants.Editor.TABLE -> contentState.edit {
                        addTable(
                            event.value.substringBefore(
                                ",", "1"
                            ).toInt(), event.value.substringAfter(",", "1").toInt()
                        )
                    }

                    Constants.Editor.TASK -> {
                        val taskList = Json.decodeFromString<List<TaskItem>>(event.value)
                        taskList.forEach {
                            contentState.edit { addTask(it.task, it.checked) }
                        }
                    }

                    Constants.Editor.LIST -> contentState.edit { addInNewLine(event.value) }
                    Constants.Editor.TEXT -> contentState.edit { add(event.value) }
                }
            }

            is NoteEvent.Load -> {
                viewModelScope.launch(Dispatchers.IO) {
                    // 判断id是否与oNote的id相同，不同则从数据库获取笔记，并更新oNote。用于从小组件打开时的情况。
                    if (event.id != _oNote.id && event.id != -1L) {
                        _oNote = useCases.getNoteById(event.id) ?: NoteEntity(
                            timestamp = System.currentTimeMillis(),
                            isMarkdown = appDataStoreRepository.booleanFlow(Constants.Preferences.IS_DEFAULT_LITE_MODE)
                                .first().not()
                        )
                    } else if (event.id == -1L && event.sharedContent != null) {
                        _oNote = NoteEntity(
                            title = event.sharedContent.fileName,
                            content = event.sharedContent.content,
                            timestamp = System.currentTimeMillis(),
                            isMarkdown = appDataStoreRepository.booleanFlow(Constants.Preferences.IS_DEFAULT_LITE_MODE)
                                .first().not()
                        )
                    }
                    noteStateFlow.update { noteState ->
                        noteState.copy(
                            id = _oNote.id,
                            folderId = _oNote.folderId,
                            isStandard = _oNote.isMarkdown,
                            timestamp = _oNote.timestamp
                        )
                    }
                    titleState.setTextAndPlaceCursorAtEnd(_oNote.title)
                    contentState.setTextAndPlaceCursorAtEnd(_oNote.content)
                    titleState.undoState.clearHistory()
                    contentState.undoState.clearHistory()
                }
            }

            NoteEvent.SaveOrUpdate -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val noteState = noteStateFlow.value
                    val note = NoteEntity(
                        id = noteState.id,
                        title = titleState.text.toString(),
                        content = contentState.text.toString(),
                        folderId = noteState.folderId,
                        isMarkdown = noteState.isStandard,
                        timestamp = System.currentTimeMillis()
                    )
                    if (note.id != null) {
                        if (note.title != _oNote.title || note.content != _oNote.content || note.isMarkdown != _oNote.isMarkdown || note.folderId != _oNote.folderId)
                            useCases.updateNote(note)
                    } else {
                        if (note.title.isNotBlank() || note.content.isNotBlank()) {
                            val newId = useCases.addNote(note)
                            noteStateFlow.update { it.copy(id = newId) }
                        }
                    }
                }
            }
        }
    }

    fun shouldShowSnackbar(): Boolean {
        val isNoteChanged = contentState.text.toString() != _oNote.content
                || titleState.text.toString() != _oNote.title
                || noteStateFlow.value.isStandard != _oNote.isMarkdown
                || noteStateFlow.value.folderId != _oNote.folderId
        val isAutoSaveEnabled =
            appDataStoreRepository.getBooleanValue(
                Constants.Preferences.IS_AUTO_SAVE_ENABLED,
                false
            )
        val isNoteEmpty = contentState.text.isBlank() && titleState.text.isBlank()
        val isNewNote = noteStateFlow.value.id == null
        if (isAutoSaveEnabled) return false
        return if (isNewNote) !isNoteEmpty else isNoteChanged
    }

    private val _dataActionState = MutableStateFlow(DataActionState())
    val dataActionStateFlow = _dataActionState.asStateFlow()
    private var dataActionJob: Job? = null

    fun cancelDataAction() {
        dataActionJob?.cancel()
        _dataActionState.value = DataActionState()
    }

    fun startDataAction(infinite: Boolean = false) {
        cancelDataAction()
        _dataActionState.update { it.copy(loading = true, infinite = infinite) }
    }

    fun onDatabaseEvent(event: DatabaseEvent) {
        when (event) {

            is DatabaseEvent.ImportVideo -> {
                val context = event.context
                val uri = event.uri

                startDataAction()
                dataActionJob = viewModelScope.launch(Dispatchers.IO) {
                    val rootUri =
                        appDataStoreRepository.getStringValue(
                            Constants.Preferences.STORAGE_PATH,
                            ""
                        )
                            .toUri()
                    // 获取Open Note目录
                    val openNoteDir =
                        getOrCreateDirectory(context, rootUri, Constants.File.OPENNOTE)
                    // 获取Backup目录
                    val videosDir = openNoteDir?.let { dir ->
                        getOrCreateDirectory(context, dir.uri, Constants.File.OPENNOTE_VIDEOS)
                    }
                    videosDir?.let { dir ->
                        val name = getFileName(context, uri)
                        val fileName =
                            "${name?.substringBeforeLast(".")}_${System.currentTimeMillis()}.${
                                name?.substringAfterLast(".")
                            }"
                        val newFile = dir.createFile("video/*", fileName)
                        _dataActionState.update { it.copy(progress = 0.5f) }
                        newFile?.let { file ->
                            context.contentResolver.openInputStream(uri)?.use { input ->
                                context.contentResolver.openOutputStream(file.uri)?.use { output ->
                                    input.copyTo(output)
                                    withContext(Dispatchers.Main) {
                                        contentState.edit { add("<video src=\"$fileName\" controls></video>") }
                                    }
                                }
                            }
                        }
                    }
                    _dataActionState.update { it.copy(progress = 1f) }
                }
            }

            is DatabaseEvent.ImportImages -> {
                val context = event.context
                val contentResolver = context.contentResolver
                val uriList = event.uriList

                startDataAction()
                dataActionJob = viewModelScope.launch(Dispatchers.IO) {
                    val rootUri =
                        appDataStoreRepository.getStringValue(
                            Constants.Preferences.STORAGE_PATH,
                            ""
                        )
                            .toUri()
                    // 获取Open Note目录
                    val openNoteDir =
                        getOrCreateDirectory(context, rootUri, Constants.File.OPENNOTE)
                    // 获取Backup目录
                    val imagesDir = openNoteDir?.let { dir ->
                        getOrCreateDirectory(context, dir.uri, Constants.File.OPENNOTE_IMAGES)
                    }
                    val savedUriList = mutableListOf<String>()
                    imagesDir?.let { dir ->
                        uriList.forEachIndexed { index, uri ->
                            _dataActionState.update {
                                it.copy(progress = index.toFloat() / uriList.size)
                            }

                            val timestamp = System.currentTimeMillis()
                            val name = getFileName(context, uri)
                            val fileName = "${name?.substringBeforeLast(".")}_${timestamp}.${
                                name?.substringAfterLast(".")
                            }"

                            try {
                                // 复制文件
                                contentResolver.openInputStream(uri)?.use { input ->
                                    val mimeType = contentResolver.getType(uri) ?: "image/*"
                                    val newFile = dir.createFile(mimeType, fileName)

                                    newFile?.let { file ->
                                        contentResolver.openOutputStream(file.uri)?.use { output ->
                                            input.copyTo(output)
                                        }
                                        savedUriList.add("![](${fileName})")
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        contentState.edit { add(savedUriList.fastJoinToString(separator = "\n")) }
                    }
                    _dataActionState.update { it.copy(progress = 1f) }
                }
            }

            is DatabaseEvent.ImportFiles -> {

                val context = event.context
                val folderId = event.folderId
                val uriList = event.uriList

                startDataAction()
                dataActionJob = viewModelScope.launch(Dispatchers.IO) {
                    uriList.forEachIndexed { index, uri ->
                        _dataActionState.update {
                            it.copy(progress = index.toFloat() / uriList.size)
                        }

                        val fileName = getFileName(context, uri)

                        context.contentResolver.openInputStream(uri).use {
                            it?.bufferedReader().use { reader ->
                                val content = reader?.readText()
                                val note = NoteEntity(
                                    title = fileName?.substringBeforeLast(".").orEmpty(),
                                    content = content.orEmpty(),
                                    folderId = folderId,
                                    isMarkdown = (fileName?.endsWith(".md") == true) || (fileName?.endsWith(
                                        ".markdown"
                                    ) == true || (fileName?.endsWith(".html") == true)),
                                    timestamp = System.currentTimeMillis()
                                )
                                useCases.addNote(note)
                            }
                        }
                    }
                    _dataActionState.update { it.copy(progress = 1f) }
                }
            }

            is DatabaseEvent.ExportFiles -> {

                val context = event.context
                val notes = event.notes
                val type = event.type

                startDataAction()

                val extension = when (type) {
                    ExportType.TXT -> ".txt"
                    ExportType.MARKDOWN -> ".md"
                    else -> ".html"
                }

                dataActionJob = viewModelScope.launch(Dispatchers.IO) {
                    val rootUri =
                        appDataStoreRepository.getStringValue(
                            Constants.Preferences.STORAGE_PATH,
                            ""
                        )
                            .toUri()
                    // 获取Open Note目录
                    val openNoteDir =
                        getOrCreateDirectory(context, rootUri, Constants.File.OPENNOTE)

                    openNoteDir?.let { dir ->
                        notes.forEachIndexed { index, noteEntity ->
                            _dataActionState.update {
                                it.copy(progress = index.toFloat() / notes.size)
                            }

                            val fileName = "${noteEntity.title}$extension"
                            val content = if (".html" != extension) noteEntity.content
                            else renderer.render(parser.parse(noteEntity.content))

                            try {
                                // 创建文件
                                val file = dir.createFile("text/*", fileName)
                                file?.let { docFile ->
                                    context.contentResolver.openOutputStream(docFile.uri)
                                        ?.use { outputStream ->
                                            OutputStreamWriter(outputStream).use { writer ->
                                                writer.write(content)
                                            }
                                        }
                                }
                            } catch (e: Exception) {
                                _dataActionState.update {
                                    it.copy(message = "Failed to export note: ${e.localizedMessage}")
                                }
                            }
                        }
                    }

                    _dataActionState.update { it.copy(progress = 1f) }
                }
            }

            is DatabaseEvent.Backup -> {

                val context = event.context

                startDataAction()

                _dataActionState.update { it.copy(progress = 0.2f) }
                dataActionJob = viewModelScope.launch(Dispatchers.IO) {
                    val rootUri =
                        appDataStoreRepository.getStringValue(
                            Constants.Preferences.STORAGE_PATH,
                            ""
                        ).toUri()
                    // 获取Open Note目录
                    val openNoteDir =
                        getOrCreateDirectory(context, rootUri, Constants.File.OPENNOTE)
                    // 获取Backup目录
                    val backupDir = openNoteDir?.let { dir ->
                        getOrCreateDirectory(context, dir.uri, Constants.File.OPENNOTE_BACKUP)
                    }
                    backupDir?.let { dir ->
                        val notes = useCases.getNotes().first()
                        val folders = useCases.getFolders().first()
                        val backupData = BackupData(notes, folders)
                        val json = Json.encodeToString(backupData)
                        _dataActionState.update { it.copy(progress = 0.4f) }
                        val encryptedJson = encryptBackupData(json)
                        _dataActionState.update { it.copy(progress = 0.6f) }
                        try {
                            val fileName = "${System.currentTimeMillis()}.json"
                            val file = dir.createFile("application/json", fileName)

                            file?.let { docFile ->
                                context.contentResolver.openOutputStream(docFile.uri)
                                    ?.use { outputStream ->
                                        OutputStreamWriter(outputStream).use { writer ->
                                            writer.write(encryptedJson)
                                        }
                                    }
                            }
                        } catch (e: Exception) {
                            _dataActionState.update {
                                it.copy(message = "Failed to backup data: ${e.localizedMessage}")
                            }
                        }
                    }

                    _dataActionState.update { it.copy(progress = 1f) }
                }
            }

            is DatabaseEvent.Recovery -> {
                val contentResolver = event.contentResolver
                val uri = event.uri

                startDataAction()
                _dataActionState.update { it.copy(progress = 0.2f) }
                dataActionJob = viewModelScope.launch(Dispatchers.IO) {

                    val encryptedOrPlainJson =
                        contentResolver.openInputStream(uri)?.bufferedReader()?.readText().orEmpty()
                    _dataActionState.update { it.copy(progress = 0.4f) }
                    runCatching {
                        val json = decryptBackupDataWithCompatibility(encryptedOrPlainJson)
                        val backupData = Json.decodeFromString<BackupData>(json)
                        _dataActionState.update { it.copy(progress = 0.6f) }
                        backupData.folders.forEach { folderEntity ->
                            useCases.addFolder(folderEntity)
                        }
                        backupData.notes.forEach { noteEntity ->
                            useCases.addNote(noteEntity)
                        }
                    }.onFailure { throwable ->
                        _dataActionState.update {
                            it.copy(message = "Recovery failed: ${throwable.localizedMessage ?: "Unknown error"}")
                        }
                    }.onSuccess {
                        _dataActionState.update { it.copy(progress = 1f) }
                    }
                }
            }

            is DatabaseEvent.RemoveUselessFiles -> {
                val context = event.context
                startDataAction()
                try {
                    _dataActionState.update { it.copy(progress = 0.1f) }

                    dataActionJob = viewModelScope.launch(Dispatchers.IO) {

                        val rootUri = appDataStoreRepository.getStringValue(
                            Constants.Preferences.STORAGE_PATH,
                            ""
                        ).toUri()

                        val allNoteContents = useCases.getNotes()
                            .map { notes -> notes.joinToString(" ") { it.content } }
                            .first()

                        _dataActionState.update { it.copy(progress = 0.3f) }

                        val directories = listOf(
                            Constants.File.OPENNOTE_VIDEOS,
                            Constants.File.OPENNOTE_IMAGES,
                            Constants.File.OPENNOTE_AUDIO
                        )

                        val openNoteDir = getOrCreateDirectory(
                            context,
                            rootUri,
                            Constants.File.OPENNOTE
                        )

                        val mutex = Mutex()
                        var progress = 0.4f
                        val progressStep = 0.6f / directories.size

                        openNoteDir?.let { dir ->
                            coroutineScope {
                                // 并行删除各目录下的无用文件
                                directories.map { dirName ->
                                    async {
                                        getOrCreateDirectory(
                                            context,
                                            dir.uri,
                                            dirName
                                        )?.let { dir ->
                                            deleteUselessFiles(dir, allNoteContents)
                                            // 使用互斥锁保护进度更新
                                            mutex.withLock {
                                                progress += progressStep
                                                _dataActionState.update {
                                                    it.copy(progress = progress)
                                                }
                                            }
                                        }
                                    }
                                }.awaitAll()
                            }
                        }

                        _dataActionState.update { it.copy(progress = 1f) }
                    }
                } catch (e: Exception) {
                    Log.e("DatabaseEvent", "Error in RemoveUselessFiles", e)
                    _dataActionState.update {
                        it.copy(
                            message = "Operation failed: ${e.localizedMessage}"
                        )
                    }
                }
            }

            DatabaseEvent.Reset -> {
                startDataAction(true)
                dataActionJob = viewModelScope.launch(Dispatchers.IO) {
                    runCatching {
                        database.clearAllTables()
                    }.onSuccess {
                        _dataActionState.update { it.copy(progress = 1f) }
                    }.onFailure { throwable ->
                        _dataActionState.update {
                            it.copy(
                                message = "Failed to reset database: ${throwable.localizedMessage ?: "error"}"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun deleteUselessFiles(dir: DocumentFile, allNoteContents: String) {
        try {
            val files = dir.listFiles()
            files.forEach { file ->
                file.name?.let { fileName ->
                    if (!allNoteContents.contains(fileName)) {
                        try {
                            val deleted = file.delete()
                            if (!deleted) {
                                _dataActionState.update {
                                    it.copy(
                                        message = "Failed to delete file: $fileName"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("DatabaseEvent", "Error deleting file: $fileName", e)
                            _dataActionState.update {
                                it.copy(
                                    message = "Failed to delete file: ${e.localizedMessage}"
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DatabaseEvent", "Error accessing directory", e)
            _dataActionState.update {
                it.copy(
                    message = "Failed to access directory: ${e.localizedMessage}"
                )
            }
        }
    }
}
