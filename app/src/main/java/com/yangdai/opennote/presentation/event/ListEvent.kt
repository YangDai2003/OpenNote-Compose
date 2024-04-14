package com.yangdai.opennote.presentation.event

import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.domain.usecase.NoteOrder
import com.yangdai.opennote.domain.usecase.OrderType

sealed interface ListEvent {

    data class ClickNote(val noteEntity: NoteEntity) : ListEvent
    data class Search(val key: String) : ListEvent

    data class Sort(
        val noteOrder: NoteOrder = NoteOrder.Date(OrderType.Descending),
        val filterFolder: Boolean = false,
        val folderId: Long? = null,
        val trash: Boolean = false
    ) : ListEvent

    data class DeleteNotesByIds(val ids: List<Long>, val recycle: Boolean) : ListEvent

    data class DeleteNotesByFolderId(val folderId: Long?) : ListEvent

    data class MoveNotes(val ids: List<Long>, val folderId: Long?) : ListEvent

    data class RestoreNotes(val ids: List<Long>) : ListEvent

    data object ToggleOrderSection : ListEvent
    data object AddNote : ListEvent
}