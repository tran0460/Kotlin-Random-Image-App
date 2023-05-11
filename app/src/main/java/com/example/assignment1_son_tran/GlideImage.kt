package com.example.assignment1_son_tran
/*
* MADE BY SON TRAN
* Oct 18, 2022
* */
import CoroutinesAsyncTask
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import java.io.File

// 720 x 405 = 16:9 ratio, common for smartphones.
class GlideImage {


    //region listOfImageUrls Property
    private val listOfImageUrls = mutableListOf(
        "https://source.unsplash.com/random/720x405?sig=${(100..900).random()}",
        "https://source.unsplash.com/random/720x405?sig=${(100..900).random()}",
        "https://source.unsplash.com/random/720x405?sig=${(100..900).random()}",
        "https://source.unsplash.com/random/720x405?sig=${(100..900).random()}",
        "https://source.unsplash.com/random/720x405?sig=${(100..900).random()}",
        "https://source.unsplash.com/random/720x405?sig=${(100..900).random()}",
        "https://source.unsplash.com/random/720x405?sig=${(100..900).random()}",
        "https://source.unsplash.com/random/720x405?sig=${(100..900).random()}",
        "https://source.unsplash.com/random/720x405?sig=${(100..900).random()}",
        "https://source.unsplash.com/random/720x405?sig=${(100..900).random()}",
        "https://www.fillmurray.com/720/405",
        "https://picsum.photos/720/405",
    )
    //endregion

    //region listCounter Property
    private var listCounter = 0
    //endregion

    //region lastURL Property
    @Suppress("PRIVATE")
    var lastURL = ""
        private set // read only property
    //endregion

    //region diskCacheStrategy Property
    val diskCacheStrategy = DiskCacheStrategy.ALL
    //endregion

    //region localStorage Property
    val localStorage = LocalStorage()
    //endregion

    //region loadGlideImage Method
    fun loadGlideImage(
        imageView: ImageView,
        context: Context,
        progressBar: ProgressBar,
        url: String = getRandomImageURL()
    ){
        progressBar.visibility = View.VISIBLE

        Glide.with(context)
            .load(url)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.visibility = View.GONE
                    toast("Load Failed: $url")
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.visibility = View.GONE
//                  toast("Image Loaded")
                    lastURL = url
                    localStorage.save(TheApp.context.getString(R.string.last_url_key),lastURL)
                    return false
                }
            })
            .diskCacheStrategy(diskCacheStrategy)
            .into(imageView)
    }
    //endregion

    //region getRandomImageURL Method
    private fun getRandomImageURL(): String {

        lastURL = listOfImageUrls[listCounter]
        listCounter++

        if (listCounter == listOfImageUrls.size) {
            listOfImageUrls.shuffle()
            listCounter = 0

        }
        return lastURL
    }
    //endregion

    //region toast Method
    fun toast(message: String){
        Toast.makeText(TheApp.context, message, Toast.LENGTH_SHORT).show()
    }
    //endregion

    //region emtptyCache Method
    fun emptyCache(context: Context){
        val asyncGlide = AsyncGlide(context)
        asyncGlide.execute()
    }
    //endregion

    //region Nested class
    private inner class AsyncGlide(val context: Context) : CoroutinesAsyncTask<Any, Any, Any>(){
        override fun doInBackground(vararg params: Any?) {
            Glide.get(context).clearDiskCache()
        }

        override fun onPostExecute(result: Any?) {
            toast("Image cache deleted")
            super.onPostExecute(result)

        }

    }
    //endregion

    //region load image from internal storage
    fun loadImageFromInternalStorage(
        imageView: ImageView,
        context: Context
    ) {
        val filePath = "${context.filesDir}${File.separator}${context.getString(R.string.last_image_file_name)}"
        Glide.with(context)
            .load(File(filePath))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(imageView)
    }
    //endregion

    //region init Method
    init {
        listOfImageUrls.shuffle()
    }
    //endregion
}