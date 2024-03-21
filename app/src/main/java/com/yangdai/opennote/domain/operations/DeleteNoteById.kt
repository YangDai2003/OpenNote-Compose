package com.yangdai.opennote.domain.operations

import com.yangdai.opennote.domain.repository.NoteRepository

class DeleteNoteById(
    private val repository: NoteRepository
) {

    suspend operator fun invoke(id: Long) {
        repository.deleteNoteById(id)
    }
}