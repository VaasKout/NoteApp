package com.example.noteexample.database

import androidx.room.*

///**
// * Caution: Querying data with nested relationships requires Room to manipulate a large volume
// * of data and can affect performance.
// * Use as few nested relationships as possible in your queries.
// * [https://developer.android.com/training/data-storage/room/relationships#nested-relationships]
// */


data class GalleryData(
    val imgSrcUrl: String,
    var isChecked: Boolean = false,
)

//TODO flag to hide photos
/**
 * [Flags] Entity sorts notes in [com.example.noteexample.ui.AllNotesFragment]
 * and updated in [com.example.noteexample.ui.SettingsFragment]
 */
@Entity(tableName = "flags_table")
data class Flags(
    @PrimaryKey val id: Long = 0,
    var filter: Int = 0,
    var ascendingOrder: Boolean = false,
    var showDate: Boolean = true,
    var columns: Int = 2,
    var hidePhotos: Boolean = false
)


/**
 * [Header] is attached to title, text and date of each note
 *
 * [Header.isChecked] is used to select items to perform actions in
 * [com.example.noteexample.ui.AllNotesFragment.actionMode]
 *
 * ColumnInfo [pos] sorts items by index in ACS or DESC order in
 * [com.example.noteexample.ui.AllNotesFragment]
 */

//TODO check migration
@Entity(tableName = "header_table")
data class Header(
    @PrimaryKey(autoGenerate = true) val headerID: Long = 0,
    @ColumnInfo(name = "position") var pos: Int = 0,
    var title: String = "",
    var date: String = "",
    var isChecked: Boolean = false,
)

@Entity(tableName = "first_note_table")
data class FirstNote(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var notePos: Int,
    val parentNoteID: Long,
    var text: String = "",
    var isChecked: Boolean = false,
    var todoItem: Boolean = false,
)

/**
 * [Image] is attached to each image and signature, inserted in note,
 * [Image.hidden] flag define visibility of image in
 * [com.example.noteexample.ui.EditNoteFragment]
 */

@Entity(tableName = "image_table")
data class Image(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var imgPos: Int,
    val parentImgNoteID: Long,
    var signature: String = "",
    var photoPath: String,
    var hidden: Boolean = false,
)

/**
 * This class binds @Entity objects [Header], [FirstNote], [Image],  in relation to One-To-Many
 */
data class NoteWithImages(
    @Embedded val header: Header,
    @Relation(
        parentColumn = "headerID",
        entityColumn = "parentNoteID"
    )
    var notes: List<FirstNote>,
    @Relation(
        parentColumn = "headerID",
        entityColumn = "parentImgNoteID",
    )
    var images: List<Image>,
)
