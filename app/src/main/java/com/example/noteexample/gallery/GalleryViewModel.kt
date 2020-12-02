package com.example.noteexample.gallery


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.noteexample.database.Image
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.database.NoteWithImages
import com.example.noteexample.repository.NoteRepository
import com.example.noteexample.utils.Camera
import kotlinx.coroutines.*

class GalleryViewModel(val noteID: Long, application: Application) : AndroidViewModel(application) {

    //Variables
    var galleryList = listOf<GalleryData>()
    private var currentNote: NoteWithImages? = null

    //Flags
    var actionModeStarted = false
    var expandedState = false

    //LiveData
    private val _actionMode = MutableLiveData<Boolean>()
    val actionMode: LiveData<Boolean> = _actionMode

    private val repository: NoteRepository

    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        viewModelScope.launch {
            currentNote = repository.getNote(noteID)
        }
    }

    fun getData(camera: Camera) {
        runBlocking {  }
        galleryList = camera.loadImagesFromStorage()
    }


    fun clearSelected() {
        galleryList.forEach {
            it.isChecked = false
        }
    }

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

                repository.insertImages(newNoteContentList)
                repository.updateNoteWithImages(current)
            }
        }
    }

    fun onStartActionMode() {
        _actionMode.value = true
    }

    fun onDoneActionMode() {
        _actionMode.value = false
    }
}