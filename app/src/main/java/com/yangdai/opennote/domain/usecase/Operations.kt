package com.yangdai.opennote.domain.usecase

data class Operations(
    val getNotes: GetNotes,
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