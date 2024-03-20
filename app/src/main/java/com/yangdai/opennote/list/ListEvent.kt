package com.yangdai.opennote.list

import com.yangdai.opennote.domain.use_case.NoteOrder

sealed interface ListEvent {
    data class Search(val key: String) : ListEvent
    data class Order(val noteOrder: NoteOrder) : ListEvent
    data class DeleteNote(val id: Long) : ListEvent
    data object ToggleOrderSection : ListEvent
}