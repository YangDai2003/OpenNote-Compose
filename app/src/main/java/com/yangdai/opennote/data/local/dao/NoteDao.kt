package com.yangdai.opennote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yangdai.opennote.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM NOTEENTITY")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM NOTEENTITY WHERE folder = :folderId")
    fun getNotesByFolderId(folderId: Long): Flow<List<NoteEntity>>

    @Query(
        """
        SELECT * FROM NOTEENTITY
        WHERE id = :id
    """
    )
    suspend fun getNoteById(id: Long): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(noteEntity: NoteEntity)

    @Query("DELETE FROM NOTEENTITY WHERE id = :id")
    suspend fun deleteNote(id: Long)

    @Update
    suspend fun updateNote(noteEntity: NoteEntity)

    @Query("SELECT * FROM NOTEENTITY WHERE title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%'")
    fun getNotesByKeyWord(keyword: String): Flow<List<NoteEntity>>
}