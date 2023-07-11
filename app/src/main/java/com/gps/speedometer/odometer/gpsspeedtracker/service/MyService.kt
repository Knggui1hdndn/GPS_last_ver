package com.gps.speedometer.odometer.gpsspeedtracker.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.gps.speedometer.odometer.gpsspeedtracker.constants.MyLocationConstants
import com.gps.speedometer.odometer.gpsspeedtracker.constants.SettingConstants
import com.gps.speedometer.odometer.gpsspeedtracker.`object`.SharedData
import com.gps.speedometer.odometer.gpsspeedtracker.dao.MyDataBase
import com.gps.speedometer.odometer.gpsspeedtracker.presenter.MotionCalculatorPresenter
import com.gps.speedometer.odometer.gpsspeedtracker.ui.MainActivity2
import com.gps.speedometer.odometer.gpsspeedtracker.LocationChangeListener
import com.gps.speedometer.odometer.gpsspeedtracker.Map
import com.gps.speedometer.odometer.gpsspeedtracker.R
import com.gps.speedometer.odometer.gpsspeedtracker.`object`.CheckPermission


class MyService : Service(), LocationChangeListener {
    private lateinit var map: Map
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        map = Map(
            applicationContext,
            MotionCalculatorPresenter(
                this,
                mutableListOf(),
                MyDataBase.getInstance(applicationContext)
            ),
            this
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        handle(intent?.action)
        return START_NOT_STICKY
    }

    private fun handle(action: String?) {
        when (action) {
            MyLocationConstants.START -> {

                updateNotification("0", "0", "0")
                map.setStart()
                map.startCallBack()
                startForeground(1, getNotifications("0", "0", "0"))
            }

            MyLocationConstants.PAUSE -> {
                map.removeCallBack()
            }

            MyLocationConstants.RESUME -> {
                map.startCallBack()
            }

            MyLocationConstants.STOP -> {
                if (CheckPermission.hasLocationSetting(applicationContext)) {
                    map.setStop()
                    map.removeCallBack()
                    SharedData.time.value = 0
                    map = Map(
                        applicationContext,
                        MotionCalculatorPresenter(
                            this,
                            mutableListOf(),
                            MyDataBase.getInstance(applicationContext)
                        ),
                        this
                    )
                }
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

        notificationLayout.setTextViewText(
            R.id.txtKm,
            if (checkHide) SharedData.convertSpeed(km.toDouble()).toInt().toString() else ""
        )


        notificationLayout.setTextViewText(
            R.id.txtCurrentSp, if (checkHide)
                "Current Speeds" else ""
        )

        notificationLayout.setTextViewText(
            R.id.txtDistance,
            SharedData.convertSpeed(distance.toDouble()).toInt().toString()
        )
        notificationLayout.setTextViewText(
            R.id.txtMaxSpeed,
            SharedData.convertSpeed(maxSpeed.toDouble()).toInt().toString()
        )
        notificationLayout.setTextViewText(R.id.unit1, if (checkHide) SharedData.toUnit else "")
        notificationLayout.setTextViewText(R.id.unit2, SharedData.toUnitDistance)
        notificationLayout.setTextViewText(R.id.unit3, SharedData.toUnit)
        return NotificationCompat.Builder(this, "1")
            .setSmallIcon(R.drawable.logo1)
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


