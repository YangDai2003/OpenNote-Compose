package com.yangdai.opennote.data.repository

import com.yangdai.opennote.data.local.dao.NoteDao
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class NoteRepositoryImpl(
    private val dao: NoteDao
) : NoteRepository {

    override fun getAllNotes(): Flow<List<NoteEntity>>  {
        return dao.getAllNotes()
    }

    override fun getAllDeletedNotes(): Flow<List<NoteEntity>> {
        return dao.getAllDeletedNotes()
    }

    override fun getNotesByFolderId(folderId: Long?): Flow<List<NoteEntity>> {
        return dao.getNotesByFolderId(folderId)
    }

    override suspend fun getNoteById(id: Long): NoteEntity? {
        return dao.getNoteById(id)
    }

    override suspend fun insertNote(note: NoteEntity) {
        dao.insertNote(note)
    }

    override suspend fun deleteNote(noteEntity: NoteEntity) {
        dao.deleteNote(noteEntity)
    }

    override suspend fun deleteNotesByFolderId(folderId: Long?) {
        dao.deleteNotesByFolderId(folderId)
    }

    override suspend fun updateNote(note: NoteEntity) {
        dao.updateNote(note)
    }

    override fun getNotesByKeyWord(keyWord: String): Flow<List<NoteEntity>> {
        return dao.getNotesByKeyWord(keyWord)
    }
}