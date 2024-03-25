package com.yangdai.opennote.domain.usecase

import com.yangdai.opennote.data.local.entity.FolderEntity
import com.yangdai.opennote.domain.repository.FolderRepository

class AddFolder(
    private val repository: FolderRepository
) {

    suspend operator fun invoke(folderEntity: FolderEntity) {
        repository.insertFolder(folderEntity)
    }
}