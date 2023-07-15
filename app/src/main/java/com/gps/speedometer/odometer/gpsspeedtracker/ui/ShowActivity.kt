package com.gps.speedometer.odometer.gpsspeedtracker.ui

import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.app.Dialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.gps.speedometer.odometer.gpsspeedtracker.constants.SettingConstants
import com.gps.speedometer.odometer.gpsspeedtracker.`object`.SharedData
import com.gps.speedometer.odometer.gpsspeedtracker.dao.MyDataBase
import com.gps.speedometer.odometer.gpsspeedtracker.model.MovementData
import com.gps.speedometer.odometer.gpsspeedtracker.utils.ColorUtils
import com.gps.speedometer.odometer.gpsspeedtracker.utils.FontUtils
import com.gps.speedometer.odometer.gpsspeedtracker.utils.StringUtils
import com.gps.speedometer.odometer.gpsspeedtracker.utils.TimeUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.gps.speedometer.odometer.gpsspeedtracker.R
import com.gps.speedometer.odometer.gpsspeedtracker.biiling.BaseActivity
import com.gps.speedometer.odometer.gpsspeedtracker.databinding.ActivityShowBinding
import com.gps.speedometer.odometer.gpsspeedtracker.ui.adpater.HistoryAdapter
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale


class ShowActivity : BaseActivity() {
    private lateinit var bottomSheet: RelativeLayout
    private lateinit var binding: ActivityShowBinding
    private lateinit var mData2: MovementData
    private val polylineOptions = PolylineOptions()
    private lateinit var myDataBase: MyDataBase

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { p0 ->
        map = p0
        val startLatLng = LatLng(mData2.startLatitude, mData2.startLongitude)
        val endLatLng = LatLng(mData2.endLatitude, mData2.endLongitude)
        val builder = LatLngBounds.Builder()
        builder.include(startLatLng)
        builder.include(endLatLng)
        val bounds = builder.build()
        val padding = 200
        // Khoảng cách đệm giữa phạm vi hiển thị và biên của bản đồ (tùy chỉnh theo nhu cầu của bạn)
        //  p0.moveCamera(CameraUpdateFactory.newLatLng(startLatLng))
        p0.setOnMapLoadedCallback {
            val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
            p0.animateCamera(cu)
        }
        p0.addMarker(
            MarkerOptions()
                .position(startLatLng)
                .title("Bắt đầu")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )

        if (mData2.endLatitude != 0.0) {
            p0.addMarker(
                MarkerOptions()
                    .position(endLatLng)
                    .title("Kết thúc")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
        }

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


    @SuppressLint("MissingInflatedId", "CutPasteId", "WrongViewCast", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val shareShow = getSharedPreferences("show", MODE_PRIVATE)
        mData2 = MyDataBase.getInstance(this).movementDao()
            .getMovementDataById(shareShow.getInt("id", 0))
        setupMyActivity()
        setBackgroundColor()
        setFont()
        showBannerAds(binding.bannerContainer!!)

        if (ColorUtils.isThemeDark())
            binding.mCoordinatorLayout.setBackgroundColor(Color.BLACK)
        else
            binding.mCoordinatorLayout.setBackgroundColor(Color.WHITE)
    }

    private fun formatTripInformation(): String {
        with(mData2) {
            val startLaLong = "${this.startLatitude},${startLongitude}"
            val endLaLong = "${this.startLatitude},${startLongitude}"
            val startPoint = "Start point: $startLaLong"
            val endPoint = "End point: $endLaLong"
            val time = "Time: ${TimeUtils.formatTime(time)}"
            val date = "Date: ${SimpleDateFormat("dd/MM/YYYY").format(mData2.date)}"
            return "$startPoint\n$endPoint\n$time,$date"
        }
        return ""
    }


    @SuppressLint("NewApi")
    private fun setupMyActivity() {
        binding = ActivityShowBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myDataBase = MyDataBase.getInstance(this)


        setSupportActionBar(binding.mToolBar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (!this::mData2.isInitialized) finish()
        setData(mData2)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(callback)
    }

    private fun setFont() {
        FontUtils.setFont(
            this,
            binding.txtSpeed,
            binding.txtTime,
            binding.txtDistance,
            binding.txtAverageSpeed
        )
    }


    private fun getDialogCapScreen(): Dialog {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        val view = LayoutInflater.from(this).inflate(R.layout.layout_screen, null)
        dialog.setContentView(view)
        with(dialog) {
            val img = findViewById<ImageView>(R.id.img)
            var bitmapScreen: Bitmap? = null
            val returnedBitmap = loadBitmapFromView(
                this@ShowActivity,
                binding.mCoordinatorLayout
            )
            snapShortMap {
                bitmapScreen = drawImage(returnedBitmap!!, it)
                img.setImageBitmap(bitmapScreen)
                saveBitmapToAppDirectory(bitmapScreen!!, "share.png")
                val imgFile = getFileFromAppDirectory("share.png")
                openScreenshot(imgFile)
            }

        }

        return dialog


    }


    private fun openScreenshot(imageFile: File) {
        val contentUri: Uri = FileProvider.getUriForFile(
            this,
            "com.gps.speedometer.odometer.gpsspeedtracker",
            imageFile
        )
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.setType("image/png");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, formatTripInformation())
        startActivity(Intent.createChooser(sharingIntent, "Share image using"))
    }

    private fun saveBitmapToAppDirectory(bitmap: Bitmap, fileName: String) {
        val file = File(filesDir, fileName)
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
    }

    private fun getFileFromAppDirectory(fileName: String): File {
        return File(filesDir, fileName)
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

    private fun drawImage(bitmapAllScreen: Bitmap, mapBitmap: Bitmap): Bitmap {
        val saveBitmap = Bitmap.createBitmap(bitmapAllScreen)
        val canvas = Canvas(saveBitmap)
        canvas.drawBitmap(
            mapBitmap,
            0f,
            (bitmapAllScreen.height - mapBitmap.height).toFloat(),
            Paint()
        )
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


    private fun setBackgroundColor() {
        val intColor = getSharedPreferences(
            SettingConstants.SETTING,
            Service.MODE_PRIVATE
        ).getInt(SettingConstants.COLOR_DISPLAY, 2)
        with(binding) {
            FontUtils.setTextColor(intColor, txtSpeed, txtAverageSpeed, txtTime, txtDistance)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun setData(mData: MovementData?) {
        val df: DateFormat = SimpleDateFormat("dd/MM/yyyy_HH:mm:ss")
        with(binding)
        {
            if (mData != null) {

                val dateTime = df.format(mData.date).split("_")
                txtTimeStart.text = dateTime[0]
                txtDateStart.text = dateTime[1]
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
        if (endLatitude == 0.0 && endLongitude == 0.0) return null
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_show, menu)
        val color = if (ColorUtils.isThemeDark()) Color.WHITE else Color.BLACK
        for (i in 0 until menu!!.size()) {
            menu.getItem(i).iconTintList = ColorStateList.valueOf(color)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.mnShare -> {
                onClickMenuShare()
            }

            R.id.mnDelete -> {
                onClickMenuDelete()
            }
        }
        return true
    }

    private fun onClickMenuDelete() {
        mData2.let { it1 ->
            myDataBase.movementDao().delete(it1)
            finish()
        }
    }

    private fun onClickMenuShare() {
        getDialogCapScreen().show()
    }

    override fun onStop() {
        super.onStop()
    }

}