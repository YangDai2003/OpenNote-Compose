package com.yangdai.opennote.domain.operations

import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class SearchNotes(
    private val repository: NoteRepository
) {

    operator fun invoke(keyWord: String): Flow<List<NoteEntity>> {
        return repository.searchNote(keyWord)
    }
}