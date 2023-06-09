package com.example.gps.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.gps.LocationChangeListener
import com.example.gps.Map
import com.example.gps.MyLocationConstants
import com.example.gps.R
import com.example.gps.SettingConstants
import com.example.gps.SharedData
import com.example.gps.ui.MainActivity2


class MyService : Service(), LocationChangeListener {
    private lateinit var map: Map
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        map = Map(applicationContext, this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handle(intent?.action)
        return START_NOT_STICKY
    }

    private fun handle(action: String?) {
        when (action) {
            MyLocationConstants.START -> {
                map.checkStart = false
                map.startCallBack()
                startForeground(1, getNotifications("0", "0", "0"))
                map.postDelayed()
            }

            MyLocationConstants.PAUSE -> {
                map.checkPause = true
                map.removeCallBack()
                map.removeHandler()
            }

            MyLocationConstants.RESUME -> {
                map.checkPause = true
                map.postDelayed()
                map.startCallBack()
            }

            MyLocationConstants.STOP -> {
                map.checkStop = true
                map.removeCallBack()
                map.removeHandler()
                SharedData.time.value = 0
            }
        }
    }


    private fun updateNotification(km: String, distance: String, maxSpeed: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, getNotifications(km, distance, maxSpeed))
    }

    @SuppressLint("WrongConstant")
    private fun getNotifications(km: String, distance: String, maxSpeed: String): Notification {
        val notificationLayout = RemoteViews(packageName, R.layout.notification_custom)
        val intent = Intent(applicationContext, MainActivity2::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
               PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val sharedPreferences =
            application.getSharedPreferences(SettingConstants.SETTING, Context.MODE_PRIVATE)
        val checkHide = sharedPreferences.getBoolean(SettingConstants.DISPLAY_SPEED, true)

        notificationLayout.setTextViewText(R.id.txtKm, if (checkHide) SharedData.convertSpeed(km.toDouble()).toInt().toString() else "")


        notificationLayout.setTextViewText(
            R.id.txtCurrentSp, if (checkHide)
                "Tốc độ hiện tại" else ""
        )

        notificationLayout.setTextViewText(
            R.id.txtDistance,
            SharedData.convertSpeed(distance.toDouble()).toInt().toString()
        )
        notificationLayout.setTextViewText(
            R.id.txtMaxSpeed,
            SharedData.convertSpeed(maxSpeed.toDouble()).toInt().toString()
        )
        notificationLayout.setTextViewText(R.id.unit1,if (checkHide) SharedData.toUnit else "")
        notificationLayout.setTextViewText(R.id.unit2, SharedData.toUnit)
        notificationLayout.setTextViewText(R.id.unit3, SharedData.toUnit)
        return NotificationCompat.Builder(this, "1")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setCustomContentView(notificationLayout)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(null)
            .setSilent(true)
            .setOngoing(true)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS or NotificationCompat.DEFAULT_VIBRATE) // Tắt âm thanh, chỉ nhấp nháy đèn và rung
            .setCustomBigContentView(notificationLayout)
            .build()
    }

    override fun onLocationChanged(km: String, distance: String, maxSpeed: String) {
        updateNotification(km, distance, maxSpeed)
    }


}


