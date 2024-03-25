package com.yangdai.opennote.domain.usecase

import com.yangdai.opennote.data.local.entity.FolderEntity
import com.yangdai.opennote.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow

class GetFolders(
    private val repository: FolderRepository
) {

    operator fun invoke(): Flow<List<FolderEntity>> {
        return repository.getAllFolders()
    }
}