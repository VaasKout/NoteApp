package com.example.noteexample.gallery

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.noteexample.database.GalleryData
import com.example.noteexample.utils.Camera
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GalleryViewModel(application: Application): AndroidViewModel(application){

    private val _galleryData = MutableLiveData<List<GalleryData>>()
    val galleryData: LiveData<List<GalleryData>> = _galleryData

    fun getData(activity: Activity){
        viewModelScope.launch {
            val camera = Camera(activity)
            _galleryData.value = camera.loadImagesFromStorage()
        }
    }

//    fun loadImg(camera: Camera, adapter: GalleryAdapter){
//        viewModelScope.launch(Dispatchers.IO) {
//            val list = camera.loadImagesFromStorage()
//        }
//    }
}