package com.example.gps.ui

import android.annotation.SuppressLint
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
import android.os.Environment
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
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
import com.example.gps.utils.ScreenshotUtil
import com.example.gps.utils.TimeUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.StyleSpan
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.textview.MaterialTextView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

class ShowActivity : AppCompatActivity() {
    private lateinit var bottomSheet: RelativeLayout
    private lateinit var binding: ActivityShowBinding
    private lateinit var bottom: BottomSheetBinding
    private var movementData2: MovementData? = null
    private val polylineOptions = PolylineOptions()
    private lateinit var myDataBase: MyDataBase

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { p0 ->
        map = p0
        p0.moveCamera(
            CameraUpdateFactory.newLatLng(
                LatLng(
                    movementData2!!.startLatitude,
                    movementData2!!.startLongitude
                )
            )
        )
        p0.addMarker(
            MarkerOptions().position(
                LatLng(
                    movementData2!!.startLatitude,
                    movementData2!!.startLongitude
                )
            ).title("Bắt đầu")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )
        p0.addMarker(
            MarkerOptions().position(
                LatLng(
                    movementData2!!.endLatitude,
                    movementData2!!.endLongitude
                )
            ).title("Kết thúc")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
        p0.addPolyline(
            polylineOptions.addAll(convertToListLatLng()).color(Color.GREEN).width(15f)
        )
        p0.apply {
            setMinZoomPreference(15.0f);
            setMaxZoomPreference(35.0f);
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isRotateGesturesEnabled = true
            setOnCameraMoveListener {
                resetMinMaxZoomPreference()
            }
        }
    }
    private lateinit var map: GoogleMap
    private lateinit var   bottom2:BottomSheetBehavior<*>
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingInflatedId", "CutPasteId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowBinding.inflate(layoutInflater)
        bottom = binding.bottom
        setContentView(binding.root)
        myDataBase = MyDataBase.getInstance(this)
        bottomSheet = bottom.bottomSheet
        setBackgroundColor()
        bottom2 = BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
        }
        bottomSheet.post {
            bottom2.peekHeight = 220 //height is ready
        }

        val intent = intent
        val movementData = intent.extras?.getSerializable("movementData")
        setData(movementData as MovementData?)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(callback)

        FontUtils.setFont(
            this,
            bottom.txtSpeed,
            bottom.txtTime,
            bottom.txtDistance,
            bottom.txtAverageSpeed
        )

        bottom.imgCap.setOnClickListener {
            val x = getDialogCapScreen()

            x.show()
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
            val text =
                "Start point:${bottom.txtAddressStart.text}(${movementData2?.startLatitude},${movementData2?.startLongitude})\n" +
                        "End point:${bottom.txtAddressEnd.text}(${movementData2?.endLatitude},${movementData2?.endLongitude})\nTime:${
                            TimeUtils.formatTime(
                                movementData2?.time!!.toLong()
                            )
                        },Date:${bottom.timeStart.text.toString().replace("Trip_", "")}"
            shareIntent.putExtra(Intent.EXTRA_TEXT, text)
            startActivity(shareIntent)
        }

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

filesDir.
        v.draw(c)
        return returnedBitmap
    }

    fun drawImage(mapBitmap: Bitmap,bottom: Bitmap){
        var saveBitmap = Bitmap.createBitmap(mapBitmap)
        val c = Canvas(saveBitmap)
        c.drawBitmap(bottom,0f,0f, Paint())
        c.drawText()
    }


    @SuppressLint("InflateParams", "WrongViewCast")
    private fun getDialogCapScreen(): Dialog {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val view = LayoutInflater.from(this).inflate(R.layout.layout_screen, null)
        dialog.setContentView(view)


        with(dialog) {
            val close = findViewById<ImageView>( R.id.imgClose)
            val screen = findViewById<ImageView>( R.id.imgScreen)
            val share = findViewById<ImageView>( R.id.imgShare)
            val img = findViewById<ImageView>( R.id.img)
            val snapshotReadyCallback = GoogleMap.SnapshotReadyCallback { bitmap ->
                // Lưu bitmap vào một tệp ảnh
                val file = File(this@ShowActivity.externalCacheDir, "map_snapshot.png")
                val outputStream = FileOutputStream(file)
                bitmap?.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                outputStream.close()
                screen?.setImageBitmap(
                    bitmap
                )
                Log.d("\\\\\\\\\\\\",bitmap!!.width.toString().plus("/").plus(bitmap.height))
                Log.d("\\\\\\\\\\\\","bottom height "+binding.bottom.bottomSheet.height)

                var returnedBitmap =    loadBitmapFromView(
                    this@ShowActivity,
//                        this@ShowActivity.findViewById( bottom.bottomSheet.id)
                    binding.bottom.root
                )
                Log.d("\\\\\\\\\\\\",returnedBitmap!!.width.toString().plus("/").plus(returnedBitmap.height))
                img?.setImageBitmap(
                    returnedBitmap
                )
            }

            map.snapshot(snapshotReadyCallback)



        }

        return dialog


    }

    private fun getDialog(): AlertDialog {
        return AlertDialog.Builder(this@ShowActivity)
            .setPositiveButton("Xóa") { _: DialogInterface, _: Int ->
                movementData2?.let { it1 ->
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
    private fun setData(movementData: MovementData?) {
        val df: DateFormat = SimpleDateFormat("dd/MM/yy_HH:mm:ss")
        with(bottom)
        {
            if (movementData != null) {
                movementData2 = movementData
                timeStart.text = "Trip${df.format(movementData.date)}"
                txtAddressEnd.text =
                    if (getAddressLine(
                            movementData.endLatitude, movementData.endLongitude
                        ) != null
                    ) getAddressLine(movementData.endLatitude, movementData.endLongitude) else "__"
                txtAddressStart.text =
                    if (getAddressLine(
                            movementData.startLatitude,
                            movementData.startLongitude
                        )
                        != null
                    ) getAddressLine(
                        movementData.startLatitude,
                        movementData.startLongitude
                    ) else "__"
                txtSpeed.text =
                    "${SharedData.convertSpeed(movementData.maxSpeed).toInt()}" + SharedData.toUnit
                txtAverageSpeed.text = "${
                    SharedData.convertSpeed(movementData.averageSpeed).toInt()
                }" + SharedData.toUnit
                txtTime.text = TimeUtils.formatTime(movementData.time)
                txtDistance.text =
                    DecimalFormat("#.##").format(SharedData.convertDistance(movementData.distance)) + SharedData.toUnit
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