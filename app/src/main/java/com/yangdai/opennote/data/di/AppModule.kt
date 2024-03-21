package com.yangdai.opennote.data.di

import android.content.Context
import androidx.room.Room
import com.yangdai.opennote.data.local.Database
import com.yangdai.opennote.data.repository.FolderRepositoryImpl
import com.yangdai.opennote.data.repository.NoteRepositoryImpl
import com.yangdai.opennote.domain.operations.AddFolder
import com.yangdai.opennote.domain.repository.NoteRepository
import com.yangdai.opennote.domain.operations.AddNote
import com.yangdai.opennote.domain.operations.DeleteFolder
import com.yangdai.opennote.domain.operations.DeleteNoteById
import com.yangdai.opennote.domain.operations.DeleteNotesByFolderId
import com.yangdai.opennote.domain.operations.FindNote
import com.yangdai.opennote.domain.operations.GetFolders
import com.yangdai.opennote.domain.operations.GetNotes
import com.yangdai.opennote.domain.operations.Operations
import com.yangdai.opennote.domain.operations.SearchNotes
import com.yangdai.opennote.domain.operations.UpdateFolder
import com.yangdai.opennote.domain.operations.UpdateNote
import com.yangdai.opennote.domain.repository.FolderRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNoteDatabase(@ApplicationContext context: Context): Database =
        Room.databaseBuilder(
            context,
            Database::class.java,
            Database.NAME
        ).build()

    @Provides
    @Singleton
    fun provideNoteRepository(database: Database): NoteRepository =
        NoteRepositoryImpl(dao = database.noteDao)

    @Provides
    @Singleton
    fun provideFolderRepository(database: Database): FolderRepository =
        FolderRepositoryImpl(dao = database.folderDao)

    @Provides
    @Singleton
    fun provideNoteUseCases(
        noteRepository: NoteRepository,
        folderRepository: FolderRepository
    ): Operations {
        return Operations(
            getNotes = GetNotes(noteRepository),
            deleteNoteById = DeleteNoteById(noteRepository),
            addNote = AddNote(noteRepository),
            findNote = FindNote(noteRepository),
            searchNotes = SearchNotes(noteRepository),
            updateNote = UpdateNote(noteRepository),
            deleteNotesByFolderId = DeleteNotesByFolderId(noteRepository),
            addFolder = AddFolder(folderRepository),
            updateFolder = UpdateFolder(folderRepository),
            deleteFolder = DeleteFolder(folderRepository),
            getFolders = GetFolders(folderRepository)
        )
    }
}