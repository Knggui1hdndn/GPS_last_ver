package com.example.gps

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import com.example.gps.interfaces.MotionCalculatorInterface
import com.example.gps.presenter.MotionCalculatorPresenter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority


interface LocationChangeListener {
    fun onLocationChanged(km: String, distance: String, maxSpeed: String)
}

class Map(
    context: Context,
    private val motion: MotionCalculatorPresenter,
    locationChangeListener: LocationChangeListener
) : MotionCalculatorInterface.MapInterFace {
    private var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private var checkStart: Boolean = false
    private var checkStop: Boolean = false

    private val locationCallback = object : LocationCallback() {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        @SuppressLint("SuspiciousIndentation", "MissingPermission")
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation = locationResult.lastLocation
            motion.updateLocation(lastLocation!!) { averageSpeed, currentSpeed, distance, maxSpeed, time ->
                if (checkStart) {
                    motion.updateMovementDataWhenStart()
                    checkStart = false
                }
                locationChangeListener.onLocationChanged(
                    currentSpeed.toInt().toString(),
                    distance.toInt().toString(),
                    maxSpeed.toInt().toString()
                )
                with(SharedData) {
                    locationLiveData.value = lastLocation
                    distanceLiveData.value = distance
                    currentSpeedLiveData.value = hashMapOf(currentSpeed to time * 1000)
                    maxSpeedLiveData.value = maxSpeed
                    averageSpeedLiveData.value = averageSpeed
                }
                motion.insertLocationData(lastLocation)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun startCallBack() {
        motion.startTimer()
        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
    }

    @SuppressLint("MissingPermission")
    override fun removeCallBack() {
        motion.stopTimer()
        if (checkStop) {
            motion.updateMovementDataWhenStop()
            checkStop=false
            checkStart=false
        }
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun setStop() {
        checkStop=true
    }

    override fun setStart() {
        checkStart=true
    }


    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10)
        .setWaitForAccurateLocation(true).setMinUpdateIntervalMillis(10)
        .setGranularity(Granularity.GRANULARITY_FINE).setMaxUpdateDelayMillis(10).build()

}