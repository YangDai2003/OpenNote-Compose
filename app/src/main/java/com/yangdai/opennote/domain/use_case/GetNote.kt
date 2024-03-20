package com.yangdai.opennote.domain.use_case

import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.domain.repository.NoteRepository

class GetNote(
    private val repository: NoteRepository
) {

    suspend operator fun invoke(id: Long): NoteEntity? {
        return repository.getNoteById(id)
    }
}