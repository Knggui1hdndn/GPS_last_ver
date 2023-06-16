package com.example.gps.interfaces

import android.location.Location
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import java.util.function.BiPredicate

interface MapInterface : MeasurementInterFace {
    interface View {
        fun setMap(googleMap: GoogleMap)
        fun onClearMap(boolean: Boolean)
        fun onShowCurrentSpeed(string: String)
        fun onMoveCamera()
        fun onCameraIdle()
    }

    interface Presenter {
        fun checkShowPolyLine()
        fun updatePolyLine()
        fun getCurrentSpeed()
        fun getCurrentPosition()
        fun isServiceRunning(serviceClass: Class<*>): Boolean
        fun conVertToLatLng(): List<LatLng>
        fun setUpMap()
    }
}