package com.yangdai.opennote.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yangdai.opennote.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM NOTEENTITY WHERE isDeleted = 0 ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM NOTEENTITY WHERE isDeleted = 1 ORDER BY timestamp DESC")
    fun getAllDeletedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM NOTEENTITY WHERE folderId = :folderId AND isDeleted = 0 ORDER BY timestamp DESC")
    fun getNotesByFolderId(folderId: Long?): Flow<List<NoteEntity>>

    @Query("SELECT * FROM NOTEENTITY WHERE isDeleted = 0 AND (title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%') ORDER BY timestamp DESC")
    fun getNotesByKeyWord(keyword: String): Flow<List<NoteEntity>>

    @Query(
        """
        SELECT * FROM NOTEENTITY
        WHERE id = :id
    """
    )
    suspend fun getNoteById(id: Long): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(noteEntity: NoteEntity): Long

    @Delete
    suspend fun deleteNote(noteEntity: NoteEntity)

    @Query("DELETE FROM NOTEENTITY WHERE folderId = :folderId")
    suspend fun deleteNotesByFolderId(folderId: Long?)

    @Update
    suspend fun updateNote(noteEntity: NoteEntity)

}