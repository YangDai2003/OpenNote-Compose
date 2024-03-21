package com.yangdai.opennote.domain.operations

import com.yangdai.opennote.data.local.entity.FolderEntity
import com.yangdai.opennote.domain.repository.FolderRepository

class UpdateFolder(
    private val repository: FolderRepository
) {

    suspend operator fun invoke(folderEntity: FolderEntity) {
        repository.updateFolder(folderEntity)
    }
}