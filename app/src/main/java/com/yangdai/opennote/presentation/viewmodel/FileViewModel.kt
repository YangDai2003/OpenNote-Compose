package com.yangdai.opennote.presentation.viewmodel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.util.fastJoinToString
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.domain.repository.AppDataStoreRepository
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
import com.yangdai.opennote.presentation.event.FileDataEvent
import com.yangdai.opennote.presentation.event.FileEvent
import com.yangdai.opennote.presentation.state.DataActionState
import com.yangdai.opennote.presentation.state.NoteState
import com.yangdai.opennote.presentation.state.SettingsState
import com.yangdai.opennote.presentation.state.TextState
import com.yangdai.opennote.presentation.util.Constants
import com.yangdai.opennote.presentation.util.PARSER
import com.yangdai.opennote.presentation.util.extension.highlight.HighlightExtension
import com.yangdai.opennote.presentation.util.extension.properties.Properties.getPropertiesRange
import com.yangdai.opennote.presentation.util.extension.properties.Properties.splitPropertiesAndContent
import com.yangdai.opennote.presentation.util.getFileName
import com.yangdai.opennote.presentation.util.getOrCreateDirectory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
import org.commonmark.node.AbstractVisitor
import org.commonmark.node.Heading
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.io.OutputStreamWriter
import javax.inject.Inject

@HiltViewModel
class FileViewModel @Inject constructor(
    private val appDataStoreRepository: AppDataStoreRepository,
    private val useCases: UseCases
) : ViewModel() {

    // 编辑时的笔记状态, 包含笔记的 id、所属文件夹 id、格式、时间戳
    val noteStateFlow: StateFlow<NoteState>
        field = MutableStateFlow(
            NoteState(
                isStandard = appDataStoreRepository.getBooleanValue(
                    Constants.Preferences.IS_DEFAULT_LITE_MODE,
                    false
                ).not()
            )
        )

    // Markdown 解析器和渲染器
    private val extensions: List<Extension> = listOf(
        TablesExtension.create(),
        AutolinkExtension.create(),
        FootnotesExtension.create(),
        HeadingAnchorExtension.create(),
        InsExtension.create(),
        ImageAttributesExtension.create(),
        StrikethroughExtension.create(),
        TaskListItemsExtension.create(),
        HighlightExtension.create()
    )
    private val parser = Parser.builder().extensions(extensions).build()
    private val renderer = HtmlRenderer.builder().extensions(extensions).build()

    // 被打开的笔记的标题和内容的状态，唯一的数据源
    val titleState = TextFieldState()
    val contentState = TextFieldState()
    val contentSnapshotFlow = snapshotFlow { contentState.text }

    // Markdown 渲染后的 HTML 内容
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val html = contentSnapshotFlow.debounce(100)
        .mapLatest {
            var content = it.toString()
            content = content.splitPropertiesAndContent().second
            renderer.render(parser.parse(content))
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
            val content = it.toString()
            val propertiesRange = content.getPropertiesRange()
            val document = PARSER.parse(content)
            val root = HeaderNode("", 0, IntRange.EMPTY)
            val headerStack = mutableListOf(root)
            document.accept(object : AbstractVisitor() {
                override fun visit(heading: Heading) {
                    val span = heading.sourceSpans.first()
                    val range = span.inputIndex until (span.inputIndex + span.length)

                    // Skip headings that are inside the properties section
                    if (propertiesRange == null || !propertiesRange.contains(range.first)) {
                        val title = it.substring(range).replace("#", "").trim()
                        val node = HeaderNode(title, heading.level, range)

                        while (headerStack.last().level >= heading.level) {
                            headerStack.removeAt(headerStack.lastIndex)
                        }
                        headerStack.last().children.add(node)
                        headerStack.add(node)
                    }
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

    val settingsStateFlow: StateFlow<SettingsState> = combine<Any, SettingsState>(
        appDataStoreRepository.booleanFlow(Constants.Preferences.IS_APP_IN_DARK_MODE),
        appDataStoreRepository.booleanFlow(Constants.Preferences.IS_LINT_ACTIVE),
        appDataStoreRepository.stringFlow(Constants.Preferences.STORAGE_PATH),
        appDataStoreRepository.stringFlow(Constants.Preferences.DATE_FORMATTER),
        appDataStoreRepository.stringFlow(Constants.Preferences.TIME_FORMATTER),
        appDataStoreRepository.booleanFlow(Constants.Preferences.SHOW_LINE_NUMBERS)
    ) { values ->
        SettingsState(
            isAppInDarkMode = values[0] as Boolean,
            isLintActive = values[1] as Boolean,
            storagePath = values[2] as String,
            dateFormatter = values[3] as String,
            timeFormatter = values[4] as String,
            showLineNumbers = values[5] as Boolean
        )
    }.flowOn(Dispatchers.IO).stateIn(
        scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = SettingsState()
    )

    @OptIn(ExperimentalFoundationApi::class)
    fun onFileEvent(event: FileEvent) {
        when (event) {
            is FileEvent.Edit -> {
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
                            event.value.substringBefore(",", "1").toInt(),
                            event.value.substringAfter(",", "1").toInt()
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

            FileEvent.SwitchType -> {
                noteStateFlow.update {
                    it.copy(
                        isStandard = it.isStandard.not()
                    )
                }
            }

            FileEvent.Save -> {
                // 暂时保留这段代码，也许后续功能扩展会用到
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
                    useCases.addNote(note)
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

    fun startDataAction(infinite: Boolean = false) {
        cancelDataAction()
        _dataActionState.update { it.copy(loading = true, infinite = infinite) }
    }

    fun onFileDataEvent(event: FileDataEvent) {
        when (event) {

            is FileDataEvent.ImportVideo -> {
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

            is FileDataEvent.ImportImages -> {
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

            is FileDataEvent.ExportFiles -> {

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
        }
    }
}
