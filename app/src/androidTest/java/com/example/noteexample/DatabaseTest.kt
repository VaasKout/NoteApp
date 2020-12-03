package com.example.noteexample

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.noteexample.database.Header
import com.example.noteexample.database.Image
import com.example.noteexample.database.NoteDao
import com.example.noteexample.database.NoteRoomDatabase
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var noteDao: NoteDao
    private lateinit var db: NoteRoomDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, NoteRoomDatabase::class.java).build()
        noteDao = db.noteDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    /**
     * Test for header
     */
    @Test
    @Throws(Exception::class)
    fun writeAndReadLastHeader() {
        runBlocking {
            val header = Header(title = "title")
            noteDao.insertHeader(header)
            val lastHeader = noteDao.getLastNote()
            assertThat(lastHeader.header.title, equalTo(header.title))
        }
    }

    /**
     * Test for one-to-many relations
     */

    @Test
    @Throws(Exception::class)
    fun writeAndReadLastNote(){
        runBlocking {
            val header = Header(noteID = 1)
            noteDao.insertHeader(header)

            val image = Image(parentNoteID = header.noteID, photoPath = "")
            noteDao.insertImage(image)

            var noteWithImage = noteDao.getLastNote()
            assertThat(noteWithImage.header.noteID, equalTo(noteWithImage.images[0].parentNoteID))

            noteWithImage = noteDao.getNote(header.noteID)
            assertThat(noteWithImage.header.noteID, equalTo(noteWithImage.images[0].parentNoteID))
        }
    }
}
