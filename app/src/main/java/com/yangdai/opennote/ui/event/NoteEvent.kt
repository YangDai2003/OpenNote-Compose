package com.yangdai.opennote.ui.event

sealed interface NoteEvent {
    data class TitleChanged(val value: String) : NoteEvent
    data class FolderChanged(val value: Long?) : NoteEvent
    data object Delete : NoteEvent
    data object NavigateBack : NoteEvent
}