package com.yangdai.opennote.presentation.event

import android.content.Context
import android.net.Uri
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.presentation.component.dialog.ExportType

sealed interface FileDataEvent {

    data class ExportFiles(
        val context: Context,
        val notes: List<NoteEntity>,
        val type: ExportType
    ) : FileDataEvent

    data class ImportImages(
        val context: Context,
        val uriList: List<Uri>
    ) : FileDataEvent

    data class ImportVideo(
        val context: Context,
        val uri: Uri
    ) : FileDataEvent
}