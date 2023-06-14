package com.example.gps.presenter

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context.MODE_PRIVATE
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import com.example.gps.constants.MyLocationConstants
import com.example.gps.dao.MyDataBase
import com.example.gps.interfaces.MapInterface
import com.example.gps.`object`.SharedData
import com.example.gps.service.MyService
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
    private val myDataBase = MyDataBase.getInstance(context)

    override fun updatePolyLine() {
        if (isServiceRunning(MyService::class.java)) {
            map?.addPolyline(
                PolylineOptions().addAll(conVertToLatLng()).color(Color.GREEN).width(15f)
            )
        }
        SharedData.locationLiveData.observe(smf) {
            if(it!=null){
                map?.addPolyline(
                    PolylineOptions().add(LatLng(it.latitude, it.longitude)).color(Color.GREEN)
                        .width(15f)
                )
            }
        }
    }

    override fun getCurrentSpeed() {
        SharedData.currentSpeedLiveData.observe(smf) {
            val speed = it.keys.first()
            view.showCurrentSpeed(
                if (speed <= 0) "0" + SharedData.toUnit else String.format(
                    "%.0f", SharedData.convertSpeed(speed)
                ) + SharedData.toUnit
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun getCurrentPosition() {
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
        val manager =
            context.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
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
            updatePolyLine()
            view.setMap(map!!)
            map?.apply {
                moveCamera(CameraUpdateFactory.newLatLng(LatLng(18.683500, 105.485750)))
                uiSettings.isZoomControlsEnabled = true
                uiSettings.isRotateGesturesEnabled = true
                isMyLocationEnabled = true;
                 getUiSettings().setMyLocationButtonEnabled(false);

                mapType = GoogleMap.MAP_TYPE_HYBRID
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
