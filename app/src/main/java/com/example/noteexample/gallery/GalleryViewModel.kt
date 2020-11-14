package com.example.noteexample.gallery


import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.noteexample.utils.dataClasses.GalleryData
import com.example.noteexample.database.NoteContent
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.repository.NoteRepository
import com.example.noteexample.utils.Camera
import kotlinx.coroutines.launch

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    //Variables
    var galleryList = listOf<GalleryData>()
    var currentNoteContentList = listOf<NoteContent>()

    //Flags
    var actionModeStarted = false
    var galleryListInit = false

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

    fun insertImages(noteId: Int) {
        viewModelScope.launch {
            val photoList = mutableListOf<GalleryData>()
            val newNoteContentList = mutableListOf<NoteContent>()
            photoList.addAll(galleryList.filter { list -> list.isChecked })
            currentNoteContentList.forEach {
                if (photoList.isNotEmpty() && it.hidden) {
                    it.photoPath = photoList[0].imgSrcUrl
                    it.hidden = false
                    photoList.removeAt(0)
                } else {
                    return@forEach
                }
            }
            if (photoList.isNotEmpty()){
                photoList.forEach { photo ->
                    val noteContent = NoteContent(
                        noteId = noteId,
                        photoPath = photo.imgSrcUrl
                    )
                    newNoteContentList.add(noteContent)
                }
                repository.insertNoteContentList(newNoteContentList)
            }
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