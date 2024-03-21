package com.yangdai.opennote.domain.repository

import com.yangdai.opennote.data.local.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

interface FolderRepository {

    fun getAllFolders(): Flow<List<FolderEntity>>

    suspend fun insertFolder(folder: FolderEntity)

    suspend fun deleteFolder(id: Long)

    suspend fun updateFolder(folder: FolderEntity)

}