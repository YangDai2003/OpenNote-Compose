package com.yangdai.opennote.domain.usecase

data class UseCases(
    val getNotes: GetNotes,
    val getNoteById: GetNoteById,
    val deleteNote: DeleteNote,
    val addNote: AddNote,
    val searchNotes: SearchNotes,
    val updateNote: UpdateNote,
    val deleteNotesByFolderId: DeleteNotesByFolderId,
    val addFolder: AddFolder,
    val updateFolder: UpdateFolder,
    val deleteFolder: DeleteFolder,
    val getFolders: GetFolders
)