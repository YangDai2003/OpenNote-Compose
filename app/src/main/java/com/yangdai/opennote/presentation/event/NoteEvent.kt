package com.yangdai.opennote.presentation.event

sealed interface NoteEvent {
    data class Open(val id: Long) : NoteEvent
    data class FolderChanged(val value: Long?) : NoteEvent
    data class Edit(val key: String, val value: String = "") : NoteEvent
    data object SwitchType : NoteEvent
    data object Delete : NoteEvent
    data object Save : NoteEvent
}