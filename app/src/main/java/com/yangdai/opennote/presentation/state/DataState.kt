package com.yangdai.opennote.presentation.state

import androidx.compose.runtime.Stable
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.domain.usecase.NoteOrder
import com.yangdai.opennote.domain.usecase.OrderType

@Stable
data class DataState(
    val notes: List<NoteEntity> = emptyList(),
    val filterTrash: Boolean = false,
    val noteOrder: NoteOrder = NoteOrder.Date(OrderType.Descending),
    val isOrderSectionVisible: Boolean = false,
    val filterFolder: Boolean = false,
    val folderId: Long? = null,
)