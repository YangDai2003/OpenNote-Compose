package com.yangdai.opennote.domain.usecase

import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.domain.repository.NoteRepository

class AddNote(
    private val repository: NoteRepository
) {

    suspend operator fun invoke(note: NoteEntity): Long {
        return repository.insertNote(note)
    }
}