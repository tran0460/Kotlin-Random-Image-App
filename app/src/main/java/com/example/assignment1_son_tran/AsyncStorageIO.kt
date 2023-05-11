package com.example.assignment1_son_tran

import CoroutinesAsyncTask
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception

/*
 * Created by Tony Davidson on July 28, 2022
*/

// region Required String Resources

/*
    <string name="gallery_folder_name">RWA</string>
    <string name="last_image_file_name">lastImage.jpg</string>
    <string name="bitmap_compress_error">Bitmap Compress Error</string>
    <string name="file_stream_error">File Stream Error</string>
 */

// endregion

class AsyncStorageIO(private var bitmap: Bitmap, private val savingLastImage: Boolean = false) :
    CoroutinesAsyncTask<Any, Any, Any>() {

    // region Properties
    @Suppress("PRIVATE")
    var completed = false
        private set

    private val fileName = TheApp.context.getString(R.string.last_image_file_name)

    private val folderName =
        TheApp.context.getString(R.string.gallery_folder_name) // new property part 4

    private val mimeType = "image/jpg"

    private val imageFileExtension = ".jpg"

    // endregion

    // region Background Method overrides
    override fun onPreExecute() {
        super.onPreExecute()
        completed = false
    }

    override fun doInBackground(vararg params: Any?) {
        if (savingLastImage) {
            saveLastImage()
        } else {
            saveImage(TheApp.context)
        }
    }

    override fun onPostExecute(result: Any?) {
        super.onPostExecute(result)
        completed = true
        TheApp.context.toast("Image saved")
    }
    // endregion

    // region Save Last Image to Internal Storage

    private fun saveLastImage() {

        val file = TheApp.context.getFileStreamPath(fileName)

        if (file.exists()) {
            file.delete()
        }

        saveImageToInternalStorage()
    }

    private fun saveImageToInternalStorage() {

        val file = File(TheApp.context.filesDir, fileName)
        saveImageToStream(file)
    }

    private fun saveImageToStream(file: File) {

        try {
            val outputStream = FileOutputStream(file)
            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                TheApp.context.toast(e.message ?: TheApp.context.getString(R.string.bitmap_compress_error))
                e.printStackTrace()
            }
        } catch (e: Exception) {
            TheApp.context.toast(e.message ?: TheApp.context.getString(R.string.file_stream_error))
            e.printStackTrace()
        }
    }

    // endregion

    // region Extension methods

    // Extension method to show toast message
    private fun Context.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // endregion

    // region Save Image to Photos/Gallery

    private fun saveImage(
        context: Context
    ) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) { // sdk 29 and up
            val timeStampFileName = System.currentTimeMillis().toString() + imageFileExtension
            //add a separator and folder name if using a subfolder
            val relativeLocation = Environment.DIRECTORY_PICTURES + File.separator + folderName

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, timeStampFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation)
            }

            val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                try {
                    saveImageToStream(context.contentResolver.openOutputStream(uri))
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, false)
                    context.contentResolver.update(uri, contentValues, null, null)
                } catch (e: Exception) {
                    // Remove the invalid entry from the MediaStore
                    context.contentResolver.delete( uri, null, null )
                }
            }
        } else { // less than Q sdk 29
            @Suppress("DEPRECATION")
            val directory = File(Environment.getExternalStorageDirectory().toString()
                    + File.separator + folderName )

            if (!directory.exists()) {
                directory.mkdirs()
            }

            val timeStampFileName = System.currentTimeMillis().toString() + imageFileExtension

            val file = File(directory, timeStampFileName)

            try {
                saveImageToStream(file)

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, timeStampFileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                    @Suppress("DEPRECATION")
                    put(MediaStore.Images.Media.DATA, file.absolutePath) // deprecated API 29
                }

                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues)
            } catch (e: Exception) {
                // Remove the invalid entry from the MediaStore
                context.contentResolver.delete( MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, null )
            }
        }
    }

    private fun saveImageToStream(outputStream: OutputStream?) {

        outputStream?.let {

            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                TheApp.context.toast(e.message ?: TheApp.context.getString(R.string.bitmap_compress_error))
                e.printStackTrace()
            }

        }
    }

// endregion

}