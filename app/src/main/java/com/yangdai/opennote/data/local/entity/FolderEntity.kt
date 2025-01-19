package com.yangdai.opennote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yangdai.opennote.presentation.theme.*
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class FolderEntity(
    @PrimaryKey val id: Long? = null,
    val name: String = "",
    val color: Int? = null
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