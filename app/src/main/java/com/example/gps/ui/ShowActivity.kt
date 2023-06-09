package com.example.gps.ui

import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.app.AlertDialog
import android.app.Dialog
import android.app.Service
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import com.example.gps.R
import com.example.gps.SettingConstants
import com.example.gps.SharedData
import com.example.gps.dao.MyDataBase
import com.example.gps.databinding.ActivityShowBinding
import com.example.gps.databinding.BottomSheetBinding
import com.example.gps.model.MovementData
import com.example.gps.utils.FontUtils
import com.example.gps.utils.MapUtils
import com.example.gps.utils.StringUtils
import com.example.gps.utils.TimeUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale


class ShowActivity : AppCompatActivity() {
    private lateinit var bottomSheet: RelativeLayout
    private lateinit var binding: ActivityShowBinding
    private lateinit var bottom: BottomSheetBinding
    private var mData2: MovementData? = null
    private val polylineOptions = PolylineOptions()
    private lateinit var myDataBase: MyDataBase

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { p0 ->
        map = p0
        val startLatLng = LatLng(mData2!!.startLatitude, mData2!!.startLongitude)
        val endLatLng = LatLng(mData2!!.endLatitude, mData2!!.endLongitude)

        p0.moveCamera(CameraUpdateFactory.newLatLng(startLatLng))

        p0.addMarker(
            MarkerOptions()
                .position(startLatLng)
                .title("Bắt đầu")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )

        p0.addMarker(
            MarkerOptions()
                .position(endLatLng)
                .title("Kết thúc")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        p0.addPolyline(
            polylineOptions.addAll(convertToListLatLng())
                .color(Color.GREEN)
                .width(15f)
        )

        p0.apply {
            setMinZoomPreference(15.0f);
            setMaxZoomPreference(35.0f);

            uiSettings.isRotateGesturesEnabled = true
            setOnCameraMoveListener {
                resetMinMaxZoomPreference()
            }
        }
    }
    private lateinit var map: GoogleMap
    private lateinit var bottom2: BottomSheetBehavior<*>

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingInflatedId", "CutPasteId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupMyActivity()
        setBackgroundColor()
        setFont()

        bottom.imgCap.setOnClickListener {
            binding.imgBack.visibility = View.GONE
            binding.imgChange.visibility = View.GONE
            getDialogCapScreen().show()

        }

        bottom.imgDelete.setOnClickListener {
            getDialog().show()

        }

        binding.imgBack.setOnClickListener {
            finish()
        }

        binding.imgChange.setOnClickListener {
            map.let { it1 -> MapUtils.setStStyleMap(it1) }
        }

        bottom.imgShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "plain/text";
            val text = formatTripInformation()
            shareIntent.putExtra(Intent.EXTRA_TEXT, text)
            startActivity(shareIntent)
        }

    }


    private fun formatTripInformation(): String {
        with(mData2) {
            val startLaLong = "${this!!.startLatitude},${startLongitude}"
            val endLaLong = "${this.startLatitude},${startLongitude}"
            val startPoint = "Start point: ${bottom.txtAddressStart.text} $startLaLong"
            val endPoint = "End point: ${bottom.txtAddressEnd.text} $endLaLong"
            val time = "Time: ${TimeUtils.formatTime(time)}"
            val date = "Date: ${bottom.timeStart.text.toString().replace("Trip_", "")}"
            return "$startPoint\n$endPoint\n$time,$date"
        }
        return ""
    }

     @SuppressLint("NewApi")
     private fun setupMyActivity() {
        binding = ActivityShowBinding.inflate(layoutInflater)
        bottom = binding.bottom
        setContentView(binding.root)
        myDataBase = MyDataBase.getInstance(this)
        bottomSheet = bottom.bottomSheet
        bottom2 = BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            peekHeight = 220
        }
        setData(intent.extras?.getSerializable("movementData") as MovementData?)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(callback)
    }

    private fun setFont() {
        FontUtils.setFont(
            this,
            bottom.txtSpeed,
            bottom.txtTime,
            bottom.txtDistance,
            bottom.txtAverageSpeed
        )
    }

    private fun getDialogCapScreen(): Dialog {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        val view = LayoutInflater.from(this).inflate(R.layout.layout_screen, null)
        dialog.setContentView(view)
        with(dialog) {
            val imgClose = findViewById<ImageView>(R.id.imgClose)
            val imgShare = findViewById<ImageView>(R.id.imgShare)
            val img = findViewById<ImageView>(R.id.img)
            var bitmapScreen: Bitmap? = null
            val returnedBitmap = loadBitmapFromView(
                this@ShowActivity,
                binding.mCoordinatorLayout
            )
             snapShortMap {
                bitmapScreen = drawImage(it, returnedBitmap!!)
                img.setImageBitmap(bitmapScreen)
            }
            imgClose.setOnClickListener {
                dialog.cancel()
            }
            imgShare.setOnClickListener {
                if (bitmapScreen != null) {
                    saveBitmapToAppDirectory(bitmapScreen!!, "share.png")
                    val imgFile = getFileFromAppDirectory("share.png")
                    openScreenshot(imgFile)
                }
            }
            setOnCancelListener {
                binding.imgBack.visibility = View.VISIBLE
                binding.imgChange.visibility = View.VISIBLE
            }
        }

        return dialog


    }

    private fun saveBitmapToAppDirectory(bitmap: Bitmap, fileName: String) {
        val file = File(filesDir, fileName)
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.close()
    }

    private fun getFileFromAppDirectory(fileName: String): File {
        return File(filesDir, fileName)
    }

    private fun openScreenshot(imageFile: File) {
        val contentUri: Uri = FileProvider.getUriForFile(this, "com.example.gps", imageFile)

        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "image/*"
        sharingIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
        startActivity(Intent.createChooser(sharingIntent, "Share image using"))
    }

    private fun loadBitmapFromView(context: Context, v: View): Bitmap? {
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

    private fun drawImage(mapBitmap: Bitmap, bottom: Bitmap): Bitmap {
        val saveBitmap = Bitmap.createBitmap(mapBitmap)
        val c = Canvas(saveBitmap)

        c.drawBitmap(bottom, 0f, 0f, Paint())
        return saveBitmap
    }

    private fun snapShortMap(callback: (Bitmap) -> Unit) {
        val snapshotReadyCallback = GoogleMap.SnapshotReadyCallback { bitmap ->
            if (bitmap != null) {
                callback(bitmap)
            }
        }
        map.snapshot(snapshotReadyCallback)
    }

    private fun getDialog(): AlertDialog {
        return AlertDialog.Builder(this@ShowActivity)
            .setPositiveButton("Xóa") { _: DialogInterface, _: Int ->
                mData2?.let { it1 ->
                    myDataBase.movementDao().delete(it1)
                    finish()
                }
            }
            .setNegativeButton("Hủy") { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
            .setTitle("Thông báo!")
            .setMessage("Xác nhận xóa item?").create()
    }

    private fun setBackgroundColor() {
        val intColor = getSharedPreferences(
            SettingConstants.SETTING,
            Service.MODE_PRIVATE
        ).getInt(SettingConstants.COLOR_DISPLAY, 2)
        with(bottom) {
            FontUtils.setTextColor(intColor, txtSpeed, txtAverageSpeed, txtTime, txtDistance)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun setData(mData: MovementData?) {
        val df: DateFormat = SimpleDateFormat("dd/MM/yy_HH:mm:ss")
        with(bottom)
        {
            if (mData != null) {
                mData2 = mData
                timeStart.text = "Trip${df.format(mData.date)}"
                txtAddressEnd.text = getAddressLine(mData.endLatitude, mData.endLongitude)
                    ?: "_ _"
                txtAddressStart.text = getAddressLine(mData.startLatitude, mData.startLongitude)
                    ?: "_ _"
                txtSpeed.text = StringUtils.convert(mData.maxSpeed)
                txtAverageSpeed.text = StringUtils.convert(mData.averageSpeed)
                txtTime.text = TimeUtils.formatTime(mData.time)
                txtDistance.text =
                    DecimalFormat("#.##").format(SharedData.convertDistance(mData.distance)) + SharedData.toUnit
            }
        }
    }


    private fun convertToListLatLng(): List<LatLng> {
        val listMovement = myDataBase.locationDao()
            .getLocationData(
                myDataBase.movementDao()
                    .getLastMovementDataId()
            )

        return listMovement.map { LatLng(it.latitude, it.longitude) }.toList()
    }

    private fun getAddressLine(endLatitude: Double, endLongitude: Double): String? {
        if (endLatitude==0.0 && endLongitude==0.0) return null
        return try {
            val geocoder = Geocoder(
                this@ShowActivity,
                Locale.getDefault()
            ).getFromLocation(endLatitude, endLongitude, 1)
            return geocoder?.get(0)?.getAddressLine(0)
        } catch (e: Exception) {
            null
        }
    }
}