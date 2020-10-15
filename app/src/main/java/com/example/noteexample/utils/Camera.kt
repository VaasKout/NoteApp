package com.example.noteexample.utils


import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.FileProvider
import com.example.noteexample.R
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

const val REQUEST_TAKE_PHOTO = 1

class Camera(private val activity: Activity) {
    lateinit var currentPhotoPath: String

    fun dispatchTakePictureIntent(barView: View) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
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
                    startActivityForResult(activity, takePictureIntent, REQUEST_TAKE_PHOTO, null)
                }
            }
        }
        val file = File(currentPhotoPath)
        MediaScannerConnection.scanFile(
            activity, arrayOf(file.toString()),
            arrayOf(file.name), null
        )
    }

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

     fun loadImagesFromStorage(): List<Bitmap> {

        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor?
        val columnIndexId: Int
        val listOfAllImages = mutableListOf<Bitmap>()
        val projection = arrayOf(MediaStore.Images.Media._ID)
        cursor = activity.contentResolver
            .query( uri, projection, null, null, null)

        if ( cursor != null ){
            columnIndexId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()){
                val contentUri = ContentUris.withAppendedId(uri, cursor.getLong(columnIndexId))
                var image: Bitmap
                //TODO figure out why it works only on emulator
                activity.contentResolver.openFileDescriptor(contentUri, "r").use { pfd ->
                    if( pfd != null ){
                        image = BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor)
                        listOfAllImages.add(image)
                    }
                }
            }
            cursor.close()
        }
        return listOfAllImages
    }
}