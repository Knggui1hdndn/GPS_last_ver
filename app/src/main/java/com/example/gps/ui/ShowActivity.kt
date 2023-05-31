package com.example.gps.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.Service
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.gps.R
import com.example.gps.SettingConstants
import com.example.gps.SharedData
import com.example.gps.dao.MyDataBase
import com.example.gps.databinding.ActivityShowBinding
import com.example.gps.databinding.BottomSheetBinding
import com.example.gps.model.MovementData
import com.example.gps.utils.FontUtils
import com.example.gps.utils.MapUtils
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowBinding.inflate(layoutInflater)
        bottom = binding.bottom
        setContentView(binding.root)
        myDataBase = MyDataBase.getInstance(this)
        bottomSheet = bottom.bottomSheet
        setBackgroundColor()
        val bottom2 = BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
        }
        bottomSheet.post {
            bottom2.peekHeight = 220 //height is ready
        }
        val intent = intent
        val movementData = intent.extras?.getSerializable("movementData")
        setData(movementData as MovementData?)
        FontUtils.setFont(
            this,
            bottom.txtSpeed,
            bottom.txtTime,
            bottom.txtDistance,
            bottom.txtAverageSpeed
        )
        with(bottom) {
            txtSpeed
            txtAverageSpeed
        }
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(callback)

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

    private fun getDialog(): Dialog {
        return AlertDialog.Builder(this@ShowActivity)
            .setPositiveButton("Xóa") { dialogInterface: DialogInterface, i: Int ->
                movementData2?.let { it1 ->
                    myDataBase.movementDao().delete(it1)
                    finish()
                }
            }
            .setNegativeButton("Hủy") { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
            .setTitle("Thông báo !")
            .setMessage("Xác nhận xóa item?").create()
    }

    private fun setBackgroundColor() {
        val intColor = getSharedPreferences(
            SettingConstants.SETTING,
            Service.MODE_PRIVATE
        ).getInt(SettingConstants.COLOR_DISPLAY, 2)
        with(bottom) {
    FontUtils.setTextColor(intColor,txtSpeed,txtAverageSpeed,txtTime,txtDistance)
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
                            movementData.endLatitude,
                            movementData.endLongitude
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
                txtSpeed.text = "${SharedData.convertSpeed(movementData.maxSpeed).toInt()}"+SharedData.toUnit
                txtAverageSpeed.text ="${SharedData.convertSpeed(movementData.averageSpeed).toInt()}"+SharedData.toUnit
                txtTime.text = TimeUtils.formatTime(movementData.time.toLong())
                txtDistance.text = DecimalFormat("#.##").format(SharedData.convertSpeed(movementData.distance)) +SharedData.toUnit
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