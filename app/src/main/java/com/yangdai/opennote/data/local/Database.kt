package com.yangdai.opennote.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yangdai.opennote.data.local.dao.FolderDao
import com.yangdai.opennote.data.local.dao.NoteDao
import com.yangdai.opennote.data.local.entity.FolderEntity
import com.yangdai.opennote.data.local.entity.NoteEntity

@Database(
    version = 1,
    entities = [NoteEntity::class, FolderEntity::class]
)
abstract class Database : RoomDatabase() {
    abstract val noteDao: NoteDao
    abstract val folderDao: FolderDao

    companion object {
        const val NAME = "NOTE_DB"
    }
}