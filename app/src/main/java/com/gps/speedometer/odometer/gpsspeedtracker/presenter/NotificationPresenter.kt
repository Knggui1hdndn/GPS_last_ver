package com.gps.speedometer.odometer.gpsspeedtracker.presenter

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context.MODE_PRIVATE
import android.graphics.Color
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.gps.speedometer.odometer.gpsspeedtracker.constants.MyLocationConstants
import com.gps.speedometer.odometer.gpsspeedtracker.constants.SettingConstants
import com.gps.speedometer.odometer.gpsspeedtracker.dao.MyDataBase
import com.gps.speedometer.odometer.gpsspeedtracker.interfaces.MapInterface
import com.gps.speedometer.odometer.gpsspeedtracker.`object`.CheckPermission
import com.gps.speedometer.odometer.gpsspeedtracker.`object`.SharedData
import com.gps.speedometer.odometer.gpsspeedtracker.service.MyService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions

class NotificationPresenter(val view: MapInterface.View, val smf: SupportMapFragment) :
    MapInterface.Presenter {
    private val context = smf.requireContext()
    private val shared = context.getSharedPreferences(MyLocationConstants.STATE, MODE_PRIVATE)
    private var map: GoogleMap? = null
    private var polylineOptions = PolylineOptions()
    private val myDataBase = MyDataBase.getInstance(context)

    override fun checkShowPolyLine() {
        Log.d("okokokom,m",isServiceRunning(MyService::class.java).toString())
        if (isServiceRunning(MyService::class.java)) {
             map?.addPolyline(
                 PolylineOptions().addAll(conVertToLatLng()).color(Color.GREEN).width(15f)
            )
        }else{
            polylineOptions = PolylineOptions()
        }
    }

    override fun updatePolyLine() {
        val shared = context.getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE)
        SharedData.locationLiveData.observe(smf) {
            if (it != null && shared.getBoolean(SettingConstants.TRACK_ON_MAP, true)) {
                Log.d("splaps",it.latitude.toString()+"   "+ it.longitude)

                map?.addPolyline(
                    polylineOptions.add(LatLng(it.latitude, it.longitude)).color(Color.GREEN)
                        .width(15f)
                )
            }
        }
    }

    override fun getCurrentSpeed() {
        SharedData.currentSpeedLiveData.observe(smf) {
            val speed = it.keys.first()
            view.onShowCurrentSpeed(
                if (speed <= 0) "0" + SharedData.toUnit else String.format(
                    "%.0f", SharedData.convertSpeed(speed)
                ) + SharedData.toUnit
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun getCurrentPosition() {
        if (CheckPermission.hasLocationPermission(context)) map?.isMyLocationEnabled = true;
        val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(smf.requireActivity())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    map?.isTrafficEnabled = true
                }
            }
    }

    override fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager =  context.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    override fun conVertToLatLng(): List<LatLng> {
        val listMovement = myDataBase.locationDao().getLocationData(
            myDataBase.movementDao().getLastMovementDataId()
        )
        return listMovement.map { LatLng(it.latitude, it.longitude) }.toList()
    }

    @SuppressLint("MissingPermission")
    override fun setUpMap() {
        val callback = OnMapReadyCallback { googleMap ->
            map = googleMap
            view.setMap(map!!)
            checkShowPolyLine()
            map?.apply {
                moveCamera(CameraUpdateFactory.newLatLng(LatLng(18.683500, 105.485750)))
                uiSettings.isRotateGesturesEnabled = true
                if (CheckPermission.hasLocationPermission(context)) isMyLocationEnabled = true;
                getUiSettings().setMyLocationButtonEnabled(false);
                mapType = GoogleMap.MAP_TYPE_HYBRID
                setOnMapLoadedCallback {
                    getCurrentPosition()
                     updatePolyLine()
                }
                setOnCameraMoveListener {
                    view.onMoveCamera()
                }
                setOnCameraIdleListener {
                    view.onCameraIdle()
                }
            }

        }
        smf.getMapAsync(callback)
    }
}
