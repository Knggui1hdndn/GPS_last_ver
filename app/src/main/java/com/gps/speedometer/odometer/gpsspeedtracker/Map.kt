package com.gps.speedometer.odometer.gpsspeedtracker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.common.api.ResolvableApiException
import com.gps.speedometer.odometer.gpsspeedtracker.interfaces.MotionCalculatorInterface
import com.gps.speedometer.odometer.gpsspeedtracker.`object`.SharedData
import com.gps.speedometer.odometer.gpsspeedtracker.presenter.MotionCalculatorPresenter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.gps.speedometer.odometer.gpsspeedtracker.constants.MyLocationConstants
import com.gps.speedometer.odometer.gpsspeedtracker.service.MyService
import com.gps.speedometer.odometer.gpsspeedtracker.ui.MainActivity2
import com.gps.speedometer.odometer.gpsspeedtracker.ui.adpater.HistoryAdapter


interface LocationChangeListener {
    fun onLocationChanged(km: String, distance: String, maxSpeed: String)
}

class Map(
    val context: Context,
    private val motion: MotionCalculatorPresenter,
    locationChangeListener: LocationChangeListener
) : MotionCalculatorInterface.MapInterFace {
     private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private var checkStart: Boolean = false
    private var checkStop: Boolean = false

    private val locationCallback = object : LocationCallback() {

        @RequiresApi(Build.VERSION_CODES.O)
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
        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
            .build()

        val settingsClient = LocationServices.getSettingsClient(context)
        val task = settingsClient.checkLocationSettings(locationSettingsRequest)

        task.addOnCompleteListener { result ->
            if (result.isSuccessful) {
                motion.startTimer()
                fusedLocationClient.requestLocationUpdates(
                    locationRequest, locationCallback, Looper.getMainLooper()
                )
            } else {
                try {
                    val resolvable = result.exception as ResolvableApiException
                    resolvable.startResolutionForResult(SharedData.activity as MainActivity2, MainActivity2.REQUEST_CHECK_SETTING)
                } catch (e: Exception) {
                    Log.d("ssssssss", "Error resolving location settings: ${e.message}")
                }
            }
        }


    }

    @SuppressLint("MissingPermission")
    override fun removeCallBack() {
        motion.stopTimer()
        if (checkStop) {
            motion.updateMovementDataWhenStop()
            checkStop = false
            checkStart = false
        }
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun setStop() {
        checkStop = true
    }

    override fun setStart() {
        checkStart = true
    }


    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .setGranularity(Granularity.GRANULARITY_FINE)
        .setMinUpdateDistanceMeters(0f)
        .setWaitForAccurateLocation(true)
        .setMinUpdateIntervalMillis(100)
        .setMaxUpdateDelayMillis(100).build()

}