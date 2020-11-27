package com.example.noteexample.database

import androidx.room.*

@Entity
data class Note(
    @PrimaryKey(autoGenerate = true) val noteID: Long = 0,
    @ColumnInfo(name = "position") var pos: Int = 0,
    @ColumnInfo(name = "title") var title: String = "",
    @ColumnInfo(name = "note") var firstNote: String = "",
    @ColumnInfo(name = "date") var date: String = "",
    @ColumnInfo(name = "hasNoteContent") var hasNoteContent: Boolean = false,
    var isChecked: Boolean = false,
)

@Entity
data class Images(
    @PrimaryKey(autoGenerate = true) var imgID: Long = 0,
    var signature: String = "",
    var photoPath: String,
    var hidden: Boolean = false,
)

@Entity
data class Flags(
    @PrimaryKey val id: Long = 0,
    var filter: Int = 0,
    var ascendingOrder: Boolean = false,
    var showDate: Boolean = true,
    var columns: Int = 2,
)

@Entity(primaryKeys = ["noteID, imgID"])
data class NoteWithImagesCrossRef(
    val noteID: Long,
    val imgID: Long,
)

data class NoteWithImages(
    @Embedded val note: Note,
    @Relation(
        parentColumn = "noteID",
        entityColumn = "imgID",
        associateBy = Junction(NoteWithImagesCrossRef::class)
    )
    val images: Images
)

data class OrderedNotes(
    @Embedded val flags: Flags,
)
