package com.example.gps.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception

class ScreenshotUtil {
    companion object{
        @SuppressLint("StaticFieldLeak")
        private var context:Context?=null

        fun getInstance(context:Context): Companion {
            this.context=context
            return this
        }
        fun loadBitmapFromView(context: Context, v: View): Bitmap? {
            val dm = context.resources.displayMetrics
            v.measure(
                View.MeasureSpec.makeMeasureSpec(dm.widthPixels, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(dm.heightPixels, View.MeasureSpec.EXACTLY)
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
            val contentUri: Uri = FileProvider.getUriForFile(context!!, "com.example.gps", imageFile)
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "image/*"
            sharingIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            context?.startActivity(Intent.createChooser(sharingIntent, "Share image using"))
        }

    }
}