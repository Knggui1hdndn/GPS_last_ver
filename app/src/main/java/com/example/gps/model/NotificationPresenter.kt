package com.example.gps.model

import android.annotation.SuppressLint
import android.location.Location
import com.example.gps.interfaces.MapInterface
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline

class NotificationPresenter(val view: MapInterface.View, val smf: SupportMapFragment) :
    MapInterface.Presenter {

    private var map: GoogleMap? = null

    override fun updateLocationUI(location: Location?) {

    }

    override fun getPolyLine(polyline: Polyline) {

    }

    @SuppressLint("MissingPermission")
    override fun getCurrentPosition()  {
        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(smf.requireActivity())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
            }
    }

    @SuppressLint("MissingPermission")
    override fun setUpMap() {
        val callback = OnMapReadyCallback { googleMap ->
            map = googleMap
            map?.apply {
                moveCamera(CameraUpdateFactory.newLatLng(LatLng(18.683500, 105.485750)))
                uiSettings.isZoomControlsEnabled = true
                uiSettings.isRotateGesturesEnabled = true
                isMyLocationEnabled = true
                mapType = GoogleMap.MAP_TYPE_HYBRID
            }
            view.setMap(map!!)
        }
        smf.getMapAsync(callback)
    }
}
