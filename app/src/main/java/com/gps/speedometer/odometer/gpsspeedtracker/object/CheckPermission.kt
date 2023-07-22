package com.gps.speedometer.odometer.gpsspeedtracker.`object`

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.gps.speedometer.odometer.gpsspeedtracker.ui.MainActivity2
import java.security.Provider


object CheckPermission {

    fun hasLocationPermission(context: Context): Boolean {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        val permission1 = Manifest.permission.ACCESS_COARSE_LOCATION
        val result = ContextCompat.checkSelfPermission(context, permission)
        val result1 = ContextCompat.checkSelfPermission(context, permission1)
        return (result == PackageManager.PERMISSION_GRANTED || result1 == PackageManager.PERMISSION_GRANTED)
    }

    @SuppressLint("SuspiciousIndentation")
    fun hasLocationSetting(context: Context): Boolean {
        val locationSystem =
            context.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        return locationSystem.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
}