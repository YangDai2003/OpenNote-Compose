package com.yangdai.opennote.presentation.event

import com.yangdai.opennote.presentation.util.SharedContent

sealed interface NoteEvent {
    data class Load(val id: Long, val sharedContent: SharedContent? = null) : NoteEvent
    data class FolderChanged(val value: Long?) : NoteEvent
    data class Edit(val key: String, val value: String = "") : NoteEvent
    data object SwitchType : NoteEvent
    data object Delete : NoteEvent
    data object Save : NoteEvent
    data object Update : NoteEvent
}