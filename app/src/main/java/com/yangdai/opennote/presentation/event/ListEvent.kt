package com.yangdai.opennote.presentation.event

import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.domain.usecase.NoteOrder
import com.yangdai.opennote.domain.usecase.OrderType

sealed interface ListEvent {

    data class Search(val key: String) : ListEvent
    data class OpenOrCreateNote(val noteEntity: NoteEntity?, val folderId: Long?) : ListEvent

    data class Sort(
        val noteOrder: NoteOrder = NoteOrder.Date(OrderType.Descending),
        val filterFolder: Boolean = false,
        val folderId: Long? = null,
        val trash: Boolean = false
    ) : ListEvent

    data class DeleteNotes(val noteEntities: Collection<NoteEntity>, val recycle: Boolean) :
        ListEvent

    data class MoveNotes(val noteEntities: Collection<NoteEntity>, val folderId: Long?) : ListEvent
    data class RestoreNotes(val noteEntities: Collection<NoteEntity>) : ListEvent

    data object ToggleOrderSection : ListEvent
    data object ChangeViewMode : ListEvent
}