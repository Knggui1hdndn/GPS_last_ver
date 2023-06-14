package com.example.gps.interfaces

import android.location.Location
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline

interface MapInterface : DisplayInterface, MeasurementInterFace {
    interface View {
        fun onVisibilityPolyLine(boolean: Boolean)
        fun drawPolyLine(latLng: LatLng)
        fun setMap(googleMap: GoogleMap)
     }

    interface Presenter {
        fun updateLocationUI(location: Location?)
        fun getPolyLine(polyline: Polyline)
        fun getCurrentPosition()
        fun setUpMap()
    }
}