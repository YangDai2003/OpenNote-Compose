package com.yangdai.opennote.note

data class NoteState(
    val id: Long? = null,
    val title: String = "",
    val content: String = "",
    val timestamp: Long? = null
)