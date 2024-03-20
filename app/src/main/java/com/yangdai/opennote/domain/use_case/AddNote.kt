package com.yangdai.opennote.domain.use_case

import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.domain.repository.NoteRepository

class AddNote(
    private val repository: NoteRepository
) {

    suspend operator fun invoke(note: NoteEntity) {
        repository.insertNote(note)
    }
}