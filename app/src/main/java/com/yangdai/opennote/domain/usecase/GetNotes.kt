package com.yangdai.opennote.domain.usecase

import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.domain.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class GetNotes(
    private val repository: NoteRepository
) {
    // 缓存比较器以避免重复创建
    private val titleAscendingComparator = compareBy<NoteEntity> { it.title.lowercase() }
    private val titleDescendingComparator = compareByDescending<NoteEntity> { it.title.lowercase() }

    operator fun invoke(
        noteOrder: NoteOrder = NoteOrder.Date(OrderType.Descending),
        trash: Boolean = false,
        filterFolder: Boolean = false,
        folderId: Long? = null,
    ): Flow<List<NoteEntity>> = flow {
        when {
            trash -> repository.getAllDeletedNotes()
            filterFolder -> repository.getNotesByFolderId(folderId)
            else -> repository.getAllNotes()
        }
            .flowOn(Dispatchers.IO)
            .map { notes ->
                withContext(Dispatchers.Default) {
                    sortNotes(notes, noteOrder)
                }
            }
            .collect { sortedNotes ->
                emit(sortedNotes)
            }
    }

    private fun sortNotes(notes: List<NoteEntity>, noteOrder: NoteOrder): List<NoteEntity> =
        when (noteOrder) {
            is NoteOrder.Title -> notes.sortedWith(
                when (noteOrder.orderType) {
                    OrderType.Ascending -> titleAscendingComparator
                    OrderType.Descending -> titleDescendingComparator
                }
            )

            is NoteOrder.Date -> when (noteOrder.orderType) {
                OrderType.Ascending -> notes.asReversed() // 使用 asReversed 而不是 reversed
                OrderType.Descending -> notes
            }
        }
}
