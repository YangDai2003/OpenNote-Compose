package com.yangdai.opennote.data.repository

import com.yangdai.opennote.data.local.dao.FolderDao
import com.yangdai.opennote.data.local.entity.FolderEntity
import com.yangdai.opennote.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow

class FolderRepositoryImpl(
    private val dao: FolderDao
) : FolderRepository {

    override fun getAllFolders(): Flow<List<FolderEntity>> {
        return dao.getAllFolders()
    }

    override suspend fun getFolderById(id: Long): FolderEntity? {
        return dao.getFolderById(id)
    }

    override suspend fun insertFolder(folder: FolderEntity) {
        dao.insertFolder(folder)
    }

    override suspend fun deleteFolder(id: Long) {
        dao.deleteFolder(id)
    }

    override suspend fun updateFolder(folder: FolderEntity) {
        dao.updateFolder(folder)
    }

}