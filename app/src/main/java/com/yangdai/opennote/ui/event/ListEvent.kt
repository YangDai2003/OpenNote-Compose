package com.yangdai.opennote.ui.event

import com.yangdai.opennote.domain.operations.NoteOrder
import com.yangdai.opennote.domain.operations.OrderType

sealed interface ListEvent {
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
}