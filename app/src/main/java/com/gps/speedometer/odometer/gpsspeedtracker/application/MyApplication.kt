package com.gps.speedometer.odometer.gpsspeedtracker

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.gps.speedometer.odometer.gpsspeedtracker.constants.SettingConstants
import com.gps.speedometer.odometer.gpsspeedtracker.ui.MainActivity2
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.gps.speedometer.odometer.gpsspeedtracker.`object`.CheckPermission
import com.gps.speedometer.odometer.gpsspeedtracker.`object`.SharedData
import com.gps.speedometer.odometer.gpsspeedtracker.service.MyService
import java.util.Date


class MyApplication :
   Application() {
    companion object {
        var check = true
    }

     private fun setUnitSpeedAndDistance() {
        try {
            SharedData.toUnit =
                getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE).getString(
                    SettingConstants.UNIT,
                    ""
                ).toString()
            when (SharedData.toUnit) {
                "km/h" -> SharedData.toUnitDistance = "km"
                "mph" -> SharedData.toUnitDistance = "mi"
                "knot" -> SharedData.toUnitDistance = "nm"
            }
        } catch (_: Exception) {
        }
    }

    override fun onCreate() {
        super.onCreate()
        setUnitSpeedAndDistance()
        SharedData.color.value = getSharedPreferences(
            SettingConstants.SETTING,
            MODE_PRIVATE
        ).getInt(SettingConstants.COLOR_DISPLAY, 0)

        val sharedPreferences = getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE)
        if (!sharedPreferences.getBoolean(SettingConstants.THEME, true)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        createChannelId()

    }
    private fun createChannelId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel("1", "Noti", NotificationManager.IMPORTANCE_HIGH)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            //hoặcNotificationManager notificationManager=getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

}


