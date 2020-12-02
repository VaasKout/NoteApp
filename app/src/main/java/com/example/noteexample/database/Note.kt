package com.example.noteexample.database

import androidx.room.*

///**
// * Caution: Querying data with nested relationships requires Room to manipulate a large volume
// * of data and can affect performance.
// * Use as few nested relationships as possible in your queries.
// * [https://developer.android.com/training/data-storage/room/relationships#nested-relationships]
// */




@Entity(tableName = "flags_table")
data class Flags(
    @PrimaryKey val id: Long = 0,
    var filter: Int = 0,
    var ascendingOrder: Boolean = false,
    var showDate: Boolean = true,
    var columns: Int = 2,
)

@Entity(tableName = "header_table")
data class Header(
    @PrimaryKey(autoGenerate = true) var noteID: Long = 0,
    @ColumnInfo(name = "position") var pos: Int = 0,
    var title: String = "",
    var text: String = "",
    var date: String = "",
    var hasNoteContent: Boolean = false,
    var isChecked: Boolean = false,
)

@Entity(tableName = "image_table")
data class Image(
    @PrimaryKey(autoGenerate = true) var imgID: Long = 0,
    val parentNoteID: Long,
    var signature: String = "",
    var photoPath: String,
    var hidden: Boolean = false,
)


data class NoteWithImages(
    @Embedded val header: Header,
    @Relation(
        parentColumn = "noteID",
        entityColumn = "parentNoteID",
    )
    val images: List<Image>
)
