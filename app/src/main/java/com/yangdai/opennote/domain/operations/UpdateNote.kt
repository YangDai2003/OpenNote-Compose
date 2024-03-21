package com.yangdai.opennote.domain.operations

import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.domain.repository.NoteRepository

class UpdateNote(
    private val repository: NoteRepository
) {

    suspend operator fun invoke(note: NoteEntity) {
        repository.updateNote(note)
    }
}