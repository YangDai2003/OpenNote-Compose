package com.yangdai.opennote.data.di

import android.content.Context
import androidx.room.Room
import com.yangdai.opennote.data.local.Database
import com.yangdai.opennote.data.repository.NoteRepositoryImpl
import com.yangdai.opennote.domain.repository.NoteRepository
import com.yangdai.opennote.domain.use_case.AddNote
import com.yangdai.opennote.domain.use_case.DeleteNote
import com.yangdai.opennote.domain.use_case.GetNote
import com.yangdai.opennote.domain.use_case.GetAllNotes
import com.yangdai.opennote.domain.use_case.NoteUseCases
import com.yangdai.opennote.domain.use_case.SearchNotes
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
    fun provideNoteUseCases(repository: NoteRepository): NoteUseCases {
        return NoteUseCases(
            getAllNotes = GetAllNotes(repository),
            deleteNote = DeleteNote(repository),
            addNote = AddNote(repository),
            getNote = GetNote(repository),
            searchNotes = SearchNotes(repository)
        )
    }
}