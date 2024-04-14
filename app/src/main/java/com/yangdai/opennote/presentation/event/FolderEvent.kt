package com.yangdai.opennote.presentation.event

import com.yangdai.opennote.data.local.entity.FolderEntity

sealed interface FolderEvent {

    data class AddFolder(val folder: FolderEntity) : FolderEvent

    data class UpdateFolder(val folder: FolderEntity) : FolderEvent

    data class DeleteFolder(val folder: FolderEntity) : FolderEvent
}