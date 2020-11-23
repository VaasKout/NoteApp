package com.example.noteexample.gallery


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.noteexample.database.NoteContent
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.repository.NoteRepository
import com.example.noteexample.utils.Camera
import com.example.noteexample.utils.GalleryData

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    //Variables
    var galleryList = listOf<GalleryData>()
    var currentNoteContentList = listOf<NoteContent>()

    //Flags
    var actionModeStarted = false
    var galleryListInit = false
    var expandedState = false

    //LiveData
    val allNoteContent: LiveData<List<NoteContent>>
    private val _actionMode = MutableLiveData<Boolean>()
    val actionMode: LiveData<Boolean> = _actionMode

    private val repository: NoteRepository

    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        allNoteContent = repository.allNoteContent
    }

    fun getData(camera: Camera) {
        galleryList = camera.loadImagesFromStorage()
    }

    fun clearSelected() {
        galleryList.forEach {
            it.isChecked = false
        }
    }

    suspend fun insertImages(noteId: Int) {
        val photoList = mutableListOf<GalleryData>()
        val newNoteContentList = mutableListOf<NoteContent>()
        photoList.addAll(galleryList.filter { list -> list.isChecked })
        if (photoList.isNotEmpty()) {
            currentNoteContentList.forEach {
                if (it.hidden || it.photoPath.isEmpty()) {
                    it.photoPath = photoList[0].imgSrcUrl
                    it.hidden = false
                    photoList.removeAt(0)
                } else {
                    return@forEach
                }
            }
            photoList.forEach { photo ->
                val noteContent = NoteContent(
                    noteId = noteId,
                    photoPath = photo.imgSrcUrl
                )
                newNoteContentList.add(noteContent)
            }
            repository.insertNoteContentList(newNoteContentList)
            repository.updateNoteContentList(currentNoteContentList)
        }
    }

    fun onStartActionMode() {
        _actionMode.value = true
    }

    fun onDoneActionMode() {
        _actionMode.value = false
    }


//    fun loadImg(camera: Camera, adapter: GalleryAdapter){
//        viewModelScope.launch(Dispatchers.IO) {
//            val list = camera.loadImagesFromStorage()
//        }
//    }
}