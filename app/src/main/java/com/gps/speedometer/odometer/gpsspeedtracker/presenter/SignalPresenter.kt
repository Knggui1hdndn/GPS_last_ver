package com.gps.speedometer.odometer.gpsspeedtracker.presenter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.GnssStatus
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.gps.speedometer.odometer.gpsspeedtracker.interfaces.SignalInterface

class SignalPresenter(val view: SignalInterface.View, val context: Context) :
    SignalInterface.Presenter {
    private val call = object : GnssStatus.Callback() {
        @SuppressLint("MissingPermission")
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            val satelliteCount = status.satelliteCount
            var totalSignalStrength = 0.0
            for (i in 0 until satelliteCount) {
                val cn0DbHz = status.getCn0DbHz(i)
                totalSignalStrength += cn0DbHz
            }
            val averageSignalStrength = totalSignalStrength / satelliteCount
            view.onStrengthGPSDataReceived(
                averageSignalStrength.toInt(),
                status.satelliteCount
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun registerGnssStatusCallback() {
        Log.d("ooooooooooooo","registerGnssStatusCallback")
        (context.getSystemService(LOCATION_SERVICE) as LocationManager).registerGnssStatusCallback(call, Handler(Looper.getMainLooper()!!))
    }
    @SuppressLint("MissingPermission")
    override fun unRegisterGnssStatusCallback() {
        (context.getSystemService(LOCATION_SERVICE) as LocationManager).unregisterGnssStatusCallback(call)
    }
}