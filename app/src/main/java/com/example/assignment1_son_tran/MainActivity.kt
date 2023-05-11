package com.example.assignment1_son_tran

/*
* MADE BY SON TRAN
* Oct 7, 2022
* */

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.GestureDetectorCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.assignment1_son_tran.databinding.ActivityMainBinding
import kotlin.math.abs


class MainActivity : AppCompatActivity() ,
    GestureDetector.OnGestureListener , GestureDetector.OnDoubleTapListener {

    //region Properties
    private val glideImage = GlideImage()

    private lateinit var gestureDetector: GestureDetectorCompat

    private lateinit var binding: ActivityMainBinding

    private var showingSystemUI = true

    private val requestCode = 42 // can be any number positive

    private var permissionWriteToExternalStorage = false

    //endregion

    //region Override onCreate Method
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // set a systemUIVisibility listener
        window.addSystemUIVisibilityListener {
            showingSystemUI = it
        }

        val interConnection = InternetConnection(this)

        gestureDetector = GestureDetectorCompat(this, this)
        gestureDetector.setOnDoubleTapListener(this)

        glideImage.emptyCache(this) // new code

        // Check internet connection.
        if(!interConnection.isConnected) {
            AlertDialog.Builder(this)
                .setTitle(R.string.message_title)
                .setMessage(R.string.message_text)
                .setIcon(R.drawable.ic_baseline_network_check_24)
                .setNegativeButton(R.string.quit){ _, _ ->
                    finish()
                }
                .setCancelable(false)
                .show()
        } else {

// Check permission.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                setUpPermissions()
            }

            val fileName = this.getString(R.string.last_image_file_name)

            val file = this.getFileStreamPath(fileName)

            if(file.exists()) {
                glideImage.loadImageFromInternalStorage(binding.imageView1, this)
            } else {
                val localStorage = LocalStorage()
                val lastUrl: String? = localStorage.getValueString(this.getString(R.string.last_url_key))

                lastUrl?.let{
                    glideImage.loadGlideImage(binding.imageView1, this, binding.progressBar, it)
                } ?: glideImage.loadGlideImage(binding.imageView1, this, binding.progressBar)
            }
        }
    }
    //endregion

    //region Override onStop Method
    override fun onStop(){
        super.onStop()
        binding.imageView1.drawable?.let {
            val bitmap = binding.imageView1.drawable.toBitmap()
            val asyncStorageIO = AsyncStorageIO(bitmap, true)
            asyncStorageIO.execute()
        }
    }
    //endregion

    //region Gesture methods
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    override fun onShowPress(e: MotionEvent) {
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return true
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return true
    }

    override fun onLongPress(e: MotionEvent) {
        glideImage.emptyCache(this)
    }

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {

        if(e1.x > e2.x){
            glideImage.loadGlideImage(binding.imageView1, this, binding.progressBar)
        }
        val swipeDistanceThreshold = 150 // distance
        val swipeVelocityThreshold = 200 // Speed
        val yDifference: Float = e2.y - e1.y
        val xDifference: Float = e2.x - e1.x

        if(abs(xDifference) > abs(yDifference)) {
            if(abs(xDifference) > swipeDistanceThreshold && abs(velocityX) > swipeVelocityThreshold) {
                if(xDifference > 0) {
//                    toast("Swipe Right")
                } else {
//                    toast("Swipe Left")
                    glideImage.loadGlideImage(binding.imageView1, this, binding.progressBar)
                }
            }

        } else if(abs(yDifference) > swipeVelocityThreshold && abs(velocityY) > swipeVelocityThreshold) {
            if(yDifference > 0) {
//             toast("Swipe Down")
                if(permissionWriteToExternalStorage || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    toast("Image Saved")
                    binding.imageView1.drawable?.let {
                        val bitmap = binding.imageView1.drawable.toBitmap()
                        val asyncStorageIO = AsyncStorageIO(bitmap )
                        asyncStorageIO.execute()
                    }
                } else {
                    toast(getString(R.string.save_permission_denied))
                }
            } else {
//             toast("Swipe Up")
            }
        }

        return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        return true
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        showingSystemUI = if (showingSystemUI) {
            hideSystemUI()
            false
        } else {
            showSystemUI()
            true
        }
        return true
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        return true
    }
    //endregion

    //region Hide/Show UI
    private fun Window.addSystemUIVisibilityListener(visibilityListener: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            decorView.setOnApplyWindowInsetsListener { view, insets ->
                val suppliedInsets = view.onApplyWindowInsets(insets)
                // only check for statusBars() and navigationBars(),
                // because captionBar() is not always
                // available and isVisible() could return false, although showSystemUI() had been called
                visibilityListener(suppliedInsets.isVisible(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()))
                suppliedInsets
            }
        } else {
            @Suppress("DEPRECATION")
            decorView.setOnSystemUiVisibilityChangeListener {
                visibilityListener((it and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0)
            }
        }
    }

    private fun hideSystemUI() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // R = 30
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(
                    WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
                            or WindowInsets.Type.systemBars())

                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                supportActionBar?.hide()
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }
    }

    private fun showSystemUI() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { //R = 30
            window.setDecorFitsSystemWindows(false)
            // show the system bars
            window.insetsController?.show(WindowInsets.Type.systemBars())
            supportActionBar?.show()
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }
    //endregion

    // region Permissions
    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            requestCode)
    }

    private fun setUpPermissions() {
        val permission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            toast("Permission to access the location is missing.")
            makeRequest()
        } else {
            toast("Permission already Granted.")
            permissionWriteToExternalStorage = true
        }
    }

    override fun onRequestPermissionsResult (
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode) {
            this.requestCode -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    toast("Permission has been denied by user")
                } else {
                    toast("Permission has been granted by user")
                    permissionWriteToExternalStorage = true
                }
            }
        }
    }




    // endregion

    //region Toast Method (Unused)
    @Suppress("UNUSED")
    fun Context.toast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    //endregion
}