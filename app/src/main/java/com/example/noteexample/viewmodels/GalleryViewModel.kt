package com.example.noteexample.viewmodels

import androidx.lifecycle.*
import com.example.noteexample.database.Image
import com.example.noteexample.database.NoteWithImages
import com.example.noteexample.database.GalleryData
import com.example.noteexample.repository.NoteRepository
import com.example.noteexample.utils.Camera
import kotlinx.coroutines.*

/**
 * ViewModel for [com.example.noteexample.ui.GalleryFragment]
 */
class GalleryViewModel(
    val noteID: Long,
    val repository: NoteRepository
) : ViewModel() {

    //Variables
    var galleryList = listOf<GalleryData>()
    private var currentNote: NoteWithImages? = null

    //Flags
    var actionModeStarted = false
    var expandedState = false

    //LiveData
    private val _actionMode = MutableLiveData<Boolean>()
    val actionMode: LiveData<Boolean> = _actionMode

    init {
        viewModelScope.launch {
            currentNote = repository.getNote(noteID)
        }
    }

    /**
     * Insert photos in current note and replace all hidden by new
     * @see com.example.noteexample.ui.EditNoteFragment
     */
    suspend fun insertImages() {
        val photoList = mutableListOf<GalleryData>()
        val newNoteContentList = mutableListOf<Image>()
        photoList.addAll(galleryList.filter { list -> list.isChecked })
        if (photoList.isNotEmpty()) {
            currentNote?.let { current ->
                current.images.forEach {
                    if (it.hidden || it.photoPath.isEmpty()) {
                        it.photoPath = photoList[0].imgSrcUrl
                        it.hidden = false
                        photoList.removeAt(0)
                    } else {
                        return@forEach
                    }
                }
                photoList.forEach { photo ->
                    val image = Image(
                        parentNoteID = noteID,
                        photoPath = photo.imgSrcUrl
                    )
                    newNoteContentList.add(image)
                }
                repository.updateNoteWithImages(current)
                repository.insertImages(newNoteContentList)
            }
        }
    }

    /**
     * Function loads list of photos from gallery
     * @see Camera.loadImagesFromStorage
     */

    fun getData(){
        if (galleryList.isEmpty()){
            galleryList = repository.getGalleryData()
        }
    }

    /**
     * Clear [galleryList] if action mode is done
     */
    fun clearSelected() {
        galleryList.forEach {
            it.isChecked = false
        }
    }


    /**
     * [actionMode] functions
     */
    fun onStartActionMode() {
        _actionMode.value = true
    }

    fun onDoneActionMode() {
        _actionMode.value = false
    }
}