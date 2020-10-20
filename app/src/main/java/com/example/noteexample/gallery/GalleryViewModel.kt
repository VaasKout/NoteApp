package com.example.noteexample.gallery

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.noteexample.database.GalleryData
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteContent
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.repository.NoteRepository
import com.example.noteexample.utils.Camera
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GalleryViewModel(application: Application): AndroidViewModel(application){

    private val _galleryData = MutableLiveData<List<GalleryData>>()
    val galleryData: LiveData<List<GalleryData>> = _galleryData

    private val repository : NoteRepository

    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
    }
    
    fun getData(camera: Camera){
        viewModelScope.launch {
            _galleryData.value = camera.loadImagesFromStorage()
        }
    }

    fun clearSelected(){
       _galleryData.value?.forEach {
           it.isChecked = false
       }
    }

    fun insertImages(noteId: Int){
        viewModelScope.launch (Dispatchers.IO){
            _galleryData.value?.let {
                val photos = it.filter { list -> list.isChecked }
                photos.forEach{ photo ->
                    val noteContentList = NoteContent(
                        noteId = noteId,
                        photoPath = photo.imgSrcUrl
                    )
                    Log.e("noteIDGal", "$noteId")
                    repository.insertNoteContent(noteContentList)
                }
            }
        }
    }



//    fun loadImg(camera: Camera, adapter: GalleryAdapter){
//        viewModelScope.launch(Dispatchers.IO) {
//            val list = camera.loadImagesFromStorage()
//        }
//    }
}