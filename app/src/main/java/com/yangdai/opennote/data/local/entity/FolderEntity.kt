package com.yangdai.opennote.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yangdai.opennote.presentation.theme.*

@Entity
data class FolderEntity(
    @PrimaryKey val id: Long? = null,
    @ColumnInfo(name = "name") val name: String = "",
    @ColumnInfo(name = "color") val color: Int? = null
) {
    companion object {
        val folderColors = listOf(
            Red,
            Orange,
            Yellow,
            Green,
            Cyan,
            Blue,
            Purple
        )
    }
}