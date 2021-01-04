package com.example.noteexample.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Room Database itself
 */

@Database(
    entities = [Header::class, FirstNote::class, Image::class, Flags::class],
    version = 2,
    exportSchema = false
)
abstract class NoteRoomDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: NoteRoomDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `first_note_table` " +
                            "(`noteID` INTEGER NOT NULL default 0, " +
                            "`parentNoteID` INTEGER NOT NULL, " +
                            "`text` TEXT NOT NULL, PRIMARY KEY(`noteID`))"
                )
                database.execSQL("DROP TABLE IF EXISTS header_table")
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `header_table` " +
                            "(`headerID` INTEGER NOT NULL default 0, " +
                            "`position` INTEGER NOT NULL default 0, `title` TEXT NOT NULL, " +
                            "`date` TEXT NOT NULL, `isChecked` INTEGER NOT NULL default 0, " +
                            "`todoList` INTEGER NOT NULL default 0, PRIMARY KEY(`headerID`))"
                )
            }
        }

        fun getDatabase(context: Context): NoteRoomDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        NoteRoomDatabase::class.java,
                        "note_database"
                    ).addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            /**
                             * [RoomDatabase.Callback] insert [Flags] object when database is created
                             */
                            CoroutineScope(Dispatchers.IO).launch {
                                getDatabase(context).noteDao().insertFlags(Flags())
                            }
                        }
                    })
                        .addMigrations(MIGRATION_1_2)
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}