package com.yangdai.opennote.domain.usecase

import com.yangdai.opennote.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetNotesCountByFolderId(
    private val repository: NoteRepository
) {

    operator fun invoke(folderId: Long? = null): Flow<Int> {

        return repository.getNotesByFolderId(folderId).map { notes ->
            notes.size
        }
    }
}
