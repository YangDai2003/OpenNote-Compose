package com.yangdai.opennote.domain.usecase

import com.yangdai.opennote.domain.repository.FolderRepository

class DeleteFolder(
    private val repository: FolderRepository
) {

    suspend operator fun invoke(folderId: Long) {
        repository.deleteFolder(folderId)
    }
}