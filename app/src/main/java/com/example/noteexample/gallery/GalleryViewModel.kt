package com.example.noteexample.gallery


import android.app.Application
import androidx.lifecycle.*
import com.example.noteexample.database.GalleryData
import com.example.noteexample.database.NoteContent
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.repository.NoteRepository
import com.example.noteexample.utils.Camera
import kotlinx.coroutines.launch

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    //Variables
    var galleryList = listOf<GalleryData>()

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
            val photos = galleryList.filter { list -> list.isChecked }
            photoList.addAll(photos)
                allNoteContent.value?.forEach {
                    if (it.hidden){
                        it.photoPath = photoList[0].imgSrcUrl
                        it.hidden = false
                        repository.updateNoteContent(it)
                        photoList.removeAt(0)
                    }
                }
            photoList.forEach { photo ->
                val noteContent = NoteContent(
                    noteId = noteId,
                    photoPath = photo.imgSrcUrl
                )
                repository.insertNoteContent(noteContent)
            }
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