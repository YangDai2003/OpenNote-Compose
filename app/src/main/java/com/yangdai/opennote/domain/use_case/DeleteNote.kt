package com.yangdai.opennote.domain.use_case

import com.yangdai.opennote.domain.repository.NoteRepository

class DeleteNote(
    private val repository: NoteRepository
) {

    suspend operator fun invoke(id: Long) {
        repository.deleteNote(id)
    }
}