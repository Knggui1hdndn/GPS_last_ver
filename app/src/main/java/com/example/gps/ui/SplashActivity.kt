package com.example.gps.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.View.MeasureSpec
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider.getUriForFile
import com.example.gps.MyApplication
import com.example.gps.R
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class SplashActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
//        val uri = Uri.parse("package:" + packageName)
//        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)Remote stack trace:
//        startActivity(intent)
//        requestPermissions(
//            arrayOf(
//                Manifest.permission.READ_MEDIA_IMAGES,
//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.ACTIVITY_RECOGNITION,
//                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ), 3
//        )


       createTimer(3L)
//        findViewById<ImageView>(R.id.a).setImageBitmap(
//            loadBitmapFromView(
//                this,
//                window.decorView.rootView
//            )
//        )
//        loadBitmapFromView(this, window.decorView.rootView)?.let { saveImage(it) }
    }

    fun loadBitmapFromView(context: Context, v: View): Bitmap? {
        val dm = context.resources.displayMetrics
        v.measure(
            MeasureSpec.makeMeasureSpec(dm.widthPixels, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(dm.heightPixels, MeasureSpec.EXACTLY)
        )
        v.layout(0, 0, v.measuredWidth, v.measuredHeight)
        val returnedBitmap = Bitmap.createBitmap(
            v.measuredWidth,
            v.measuredHeight, Bitmap.Config.ARGB_8888
        )
        val c = Canvas(returnedBitmap)
        v.draw(c)
        return returnedBitmap
    }

    @Throws(IOException::class)
    fun saveImage(bitmap: Bitmap) {
        try {
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 40, bytes)
            val f =
                File(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + "test.png"
                )
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            fo.close()
            openScreenshot(f)

        } catch (e: Exception) {
            Log.e("lỗi", e.message.toString())
        }
    }

    private fun openScreenshot(imageFile: File) {
        val contentUri: Uri = getUriForFile(this, "com.example.gps", imageFile)
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "image/*"
        sharingIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
        startActivity(Intent.createChooser(sharingIntent, "Share image using"))
    }

    private fun createTimer(seconds: Long) {
        val countDownTimer: CountDownTimer = object : CountDownTimer(seconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                startMainActivity()
                finish()
                val application = application as? MyApplication

                // If the application is not an instance of MyApplication, log an error message and
                // start the MainActivity without showing the app open ad.
                if (application == null) {
                    Log.e("LOG_TAG", "Failed to cast application to MyApplication.")
                    startMainActivity()
                    return
                }

                // Show the app open ad.
                application.showAdIfAvailable(
                    this@SplashActivity,
                    object : MyApplication.OnShowAdCompleteListener {
                        override fun onShowAdComplete() {

                        }
                    })
            }
        }
        countDownTimer.start()
    }

    /** Start the MainActivity. */
    fun startMainActivity() {
        val intent = Intent(this, MainActivity2::class.java)
        startActivity(intent)
    }
}