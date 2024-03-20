package com.yangdai.opennote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yangdai.opennote.data.local.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Query("SELECT * FROM FOLDERENTITY")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query(
        """
        SELECT * FROM FOLDERENTITY
        WHERE id = :id
    """
    )
    suspend fun getFolderById(id: Long): FolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folderEntity: FolderEntity)

    @Query("DELETE FROM FolderEntity WHERE id = :id")
    suspend fun deleteFolder(id: Long)

    @Update
    suspend fun updateFolder(folderEntity: FolderEntity)

}