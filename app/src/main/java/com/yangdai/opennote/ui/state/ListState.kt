package com.yangdai.opennote.ui.state

import androidx.compose.runtime.Stable
import com.yangdai.opennote.data.local.entity.FolderEntity
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.domain.operations.NoteOrder
import com.yangdai.opennote.domain.operations.OrderType

@Stable
data class ListState(
    val notes: List<NoteEntity> = emptyList(),
    val folders: List<FolderEntity> = emptyList(),
    val trash: Boolean = false,
    val noteOrder: NoteOrder = NoteOrder.Date(OrderType.Descending),
    val isOrderSectionVisible: Boolean = false,
    val filterFolder: Boolean = false,
    val folderId: Long? = null,
)