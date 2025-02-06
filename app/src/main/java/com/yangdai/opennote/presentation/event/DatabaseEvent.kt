package com.yangdai.opennote.presentation.event

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.presentation.component.dialog.ExportType

sealed interface DatabaseEvent {

    data class ImportFiles(
        val context: Context,
        val folderId: Long?,
        val uriList: List<Uri>
    ) : DatabaseEvent

    data class ExportFiles(
        val context: Context,
        val notes: List<NoteEntity>,
        val type: ExportType
    ) : DatabaseEvent

    data class Backup(val context: Context) : DatabaseEvent
    data class Recovery(val contentResolver: ContentResolver, val uri: Uri) : DatabaseEvent
    data object Reset : DatabaseEvent

    data class ImportImages(
        val context: Context,
        val uriList: List<Uri>
    ) : DatabaseEvent
}