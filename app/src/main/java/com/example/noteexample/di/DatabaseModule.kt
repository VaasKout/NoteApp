package com.example.noteexample.di

import android.content.Context
import com.example.noteexample.database.NoteDao
import com.example.noteexample.database.NoteRoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): NoteRoomDatabase {
        return NoteRoomDatabase.getDatabase(context)
    }

    @Provides
    fun provideNoteDao(noteRoomDatabase: NoteRoomDatabase): NoteDao{
        return noteRoomDatabase.noteDao()
    }
}