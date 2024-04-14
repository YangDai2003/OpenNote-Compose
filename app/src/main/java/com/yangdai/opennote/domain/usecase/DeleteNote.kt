package com.yangdai.opennote.domain.usecase

import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.domain.repository.NoteRepository

class DeleteNote(
    private val repository: NoteRepository
) {

    suspend operator fun invoke(noteEntity: NoteEntity) {
        repository.deleteNote(noteEntity)
    }
}