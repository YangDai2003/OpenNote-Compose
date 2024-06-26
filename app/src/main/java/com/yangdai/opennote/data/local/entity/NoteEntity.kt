package com.yangdai.opennote.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    indices = [
        // 用于getAllNotes和getAllDeletedNotes
        Index(value = ["isDeleted", "timestamp"], name = "idx_deleted_timestamp"),
        // 用于getNotesByFolderId
        Index(value = ["folderId", "isDeleted", "timestamp"], name = "idx_folder_deleted_timestamp")
    ]
)
data class NoteEntity(
    @PrimaryKey val id: Long? = null,
    val title: String = "",
    val content: String = "",
    val folderId: Long? = null,
    val isMarkdown: Boolean = true,
    val isDeleted: Boolean = false,
    val timestamp: Long
)
