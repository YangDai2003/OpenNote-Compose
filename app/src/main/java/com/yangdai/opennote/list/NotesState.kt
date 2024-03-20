package com.yangdai.opennote.list

import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.domain.use_case.NoteOrder
import com.yangdai.opennote.domain.use_case.OrderType

data class NotesState(
    val notes: List<NoteEntity> = emptyList(),
    val noteOrder: NoteOrder = NoteOrder.Date(OrderType.Descending),
    val isOrderSectionVisible: Boolean = false
)