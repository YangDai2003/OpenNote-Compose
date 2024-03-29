package com.yangdai.opennote.presentation.event

sealed interface NoteEvent {
    data class TitleChanged(val value: String) : NoteEvent
    data class FolderChanged(val value: Long?) : NoteEvent
    data class Edit(val value: String) : NoteEvent
    data object SwitchType : NoteEvent
    data object Delete : NoteEvent
    data object NavigateBack : NoteEvent
}