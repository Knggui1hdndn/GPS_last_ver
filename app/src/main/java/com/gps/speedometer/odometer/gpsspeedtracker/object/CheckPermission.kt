package com.gps.speedometer.odometer.gpsspeedtracker.`object`

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object CheckPermission {

    fun hasLocationPermission(context:Context): Boolean {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        val permission1 = Manifest.permission.ACCESS_COARSE_LOCATION
         val result = ContextCompat.checkSelfPermission(context, permission)
        val result1 = ContextCompat.checkSelfPermission(context, permission1)
         return (result == PackageManager.PERMISSION_GRANTED || result1==PackageManager.PERMISSION_GRANTED)
    }
}