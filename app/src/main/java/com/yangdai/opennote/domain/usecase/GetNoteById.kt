package com.yangdai.opennote.domain.usecase

import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.domain.repository.NoteRepository

class GetNoteById(
    private val repository: NoteRepository
) {

    suspend operator fun invoke(id: Long): NoteEntity? {
        return repository.getNoteById(id)
    }
}