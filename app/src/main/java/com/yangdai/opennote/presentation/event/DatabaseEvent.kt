package com.yangdai.opennote.presentation.event

import android.content.ContentResolver
import android.net.Uri
import com.yangdai.opennote.data.local.entity.NoteEntity

sealed interface DatabaseEvent {

    data class Import(
        val contentResolver: ContentResolver,
        val folderId: Long?,
        val uriList: List<Uri>
    ) : DatabaseEvent

    data class Export(
        val contentResolver: ContentResolver,
        val notes: List<NoteEntity>,
        val type: String
    ) : DatabaseEvent

    data class Backup(val contentResolver: ContentResolver) : DatabaseEvent
    data class Recovery(val contentResolver: ContentResolver, val uri: Uri) : DatabaseEvent
    data object Reset : DatabaseEvent
}