package com.yangdai.opennote.presentation.event

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.presentation.component.dialog.ExportType

sealed interface DatabaseEvent {

    data class ImportFile(
        val contentResolver: ContentResolver,
        val folderId: Long?,
        val uriList: List<Uri>
    ) : DatabaseEvent

    data class ExportFile(
        val contentResolver: ContentResolver,
        val notes: List<NoteEntity>,
        val type: ExportType
    ) : DatabaseEvent

    data class Backup(val contentResolver: ContentResolver) : DatabaseEvent
    data class Recovery(val contentResolver: ContentResolver, val uri: Uri) : DatabaseEvent
    data object Reset : DatabaseEvent

    data class ImportImages(
        val context: Context,
        val contentResolver: ContentResolver,
        val uriList: List<Uri>
    ) : DatabaseEvent
}