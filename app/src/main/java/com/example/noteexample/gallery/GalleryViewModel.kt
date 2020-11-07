package com.example.noteexample.gallery


import android.app.Application
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.*
import com.example.noteexample.database.GalleryData
import com.example.noteexample.database.NoteContent
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.repository.NoteRepository
import com.example.noteexample.utils.Camera
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    //Variables
    var galleryList = listOf<GalleryData>()

    //Flags
    var actionModeStarted = false
    var galleryListInit = false

    private val _actionMode = MutableLiveData<Boolean>()
    val actionMode: LiveData<Boolean> = _actionMode

    private val repository: NoteRepository

    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
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
            val photos = galleryList.filter { list -> list.isChecked }
            photos.forEach { photo ->
                val noteContentList = NoteContent(
                    noteId = noteId,
                    photoPath = photo.imgSrcUrl
                )
                Log.e("noteIDGal", "$noteId")
                repository.insertNoteContent(noteContentList)
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