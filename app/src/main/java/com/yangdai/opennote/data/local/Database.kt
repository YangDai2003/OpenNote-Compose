package com.yangdai.opennote.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yangdai.opennote.data.local.dao.FolderDao
import com.yangdai.opennote.data.local.dao.NoteDao
import com.yangdai.opennote.data.local.entity.FolderEntity
import com.yangdai.opennote.data.local.entity.NoteEntity

@Database(
    version = 2,
    entities = [NoteEntity::class, FolderEntity::class]
)
abstract class Database : RoomDatabase() {
    abstract val noteDao: NoteDao
    abstract val folderDao: FolderDao

    companion object {
        const val NAME = "NOTE_DB"
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 添加索引
        db.execSQL("CREATE INDEX IF NOT EXISTS `idx_deleted_timestamp` ON `NoteEntity` (`isDeleted`, `timestamp`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `idx_folder_deleted_timestamp` ON `NoteEntity` (`folderId`, `isDeleted`, `timestamp`)")
    }
}