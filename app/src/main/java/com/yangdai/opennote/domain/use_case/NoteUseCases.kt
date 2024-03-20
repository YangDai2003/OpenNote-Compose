package com.yangdai.opennote.domain.use_case

data class NoteUseCases(
    val getAllNotes: GetAllNotes,
    val deleteNote: DeleteNote,
    val addNote: AddNote,
    val getNote: GetNote,
    val searchNotes: SearchNotes
)