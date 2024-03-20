package com.yangdai.opennote.domain.repository

import com.yangdai.opennote.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    fun getAllNotes(): Flow<List<NoteEntity>>

    fun getNotesByFolderId(folderId: Long): Flow<List<NoteEntity>>

    suspend fun getNoteById(id: Long): NoteEntity?

    suspend fun insertNote(note: NoteEntity)

    suspend fun deleteNote(id: Long)

    suspend fun updateNote(note: NoteEntity)

    fun searchNote(keyWord: String): Flow<List<NoteEntity>>

}