package com.yangdai.opennote.note

sealed interface NoteEvent {
    data class TitleChanged(val value: String) : NoteEvent
    data class ContentChanged(val value: String) : NoteEvent
    data object Delete : NoteEvent
    data object NavigateBack : NoteEvent
}