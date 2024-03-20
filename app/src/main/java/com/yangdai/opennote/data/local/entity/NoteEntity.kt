package com.yangdai.opennote.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NoteEntity(
    @PrimaryKey val id: Long? = null,
    @ColumnInfo(name = "title") val title: String = "",
    @ColumnInfo(name = "content") val content: String = "",
    @ColumnInfo(name = "folder") val folderId: Long? = null,
    val timestamp: Long
)
