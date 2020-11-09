package com.example.noteexample.utils

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.core.content.FileProvider
import com.example.noteexample.R
import com.example.noteexample.utils.dataClasses.GalleryData
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class Camera(private val activity: Activity) {
    lateinit var currentPhotoPath: String
    val dialogList = arrayOf("Фото с камеры", "Фото из галереи")

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? =
            activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    fun dispatchTakePictureIntent(barView: View): Intent {
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(activity.packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Snackbar.make(
                        barView,
                        activity.getString(R.string.camera_error),
                        Snackbar.LENGTH_LONG
                    ).show()
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        activity,
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                }
            }
            val file = File(currentPhotoPath)
            MediaScannerConnection.scanFile(
                activity, arrayOf(file.toString()),
                arrayOf(file.name), null
            )
        }
    }


    fun loadImagesFromStorage(): List<GalleryData> {

        val uriExternal: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor?
        val columnIndexID: Int
        val listOfAllImages: MutableList<GalleryData> = mutableListOf()
        val projection = arrayOf(MediaStore.Images.Media._ID)
        var imageId: Long
        cursor = activity.contentResolver.query(uriExternal, projection, null, null, null)
        if (cursor != null) {
            columnIndexID = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                imageId = cursor.getLong(columnIndexID)
                val uriImage = Uri.withAppendedPath(uriExternal, imageId.toString()).toString()
                val imgUrl = GalleryData(uriImage)
                Log.e("url", uriImage)
                listOfAllImages.add(imgUrl)
            }
            cursor.close()
        }
        return listOfAllImages.reversed()
    }
}

//if (ContextCompat.checkSelfPermission(thisActivity,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE)
//            != PackageManager.PERMISSION_GRANTED) {
//
//        // Permission is not granted
//        // Should we show an explanation?
//        if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//            // Show an explanation to the user *asynchronously* -- don't block
//            // this thread waiting for the user's response! After the user
//            // sees the explanation, try again to request the permission.
//        } else {
//            // No explanation needed; request the permission
//            ActivityCompat.requestPermissions(thisActivity,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
//
//            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//            // app-defined int constant. The callback method gets the
//            // result of the request.
//        }
//    } else {
//        // Permission has already been granted
//    }