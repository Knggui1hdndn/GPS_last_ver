package com.example.gps.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.gps.MyLocationConstants
import com.example.gps.R
import com.example.gps.SharedData
import com.example.gps.dao.MyDataBase
import com.example.gps.model.MovementData
import com.example.gps.ui.MainActivity2
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority


class MyService : Service(), SensorEventListener {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private var checkStop: Boolean = false
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var myDataBase: MyDataBase? = null
    private var millis = 0L
    private var distance = 0f
    private var checkStart = false
    private var checkPause = false
    private var lastMovementDataId = 0
    private var mili = 0L
    private var previousLocation: Location? = null
    private var listSpeed = mutableListOf<Float>()
    private lateinit var sharedPreferences: SharedPreferences
    var acceleration: Double = 0.0

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        myDataBase = MyDataBase.getInstance(applicationContext)
        sharedPreferences = applicationContext.getSharedPreferences("state", MODE_PRIVATE)
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        handle(intent?.action)
        return START_NOT_STICKY
    }

    private val handler = Handler(Looper.getMainLooper())

    private val countRunnable: Runnable = object : Runnable {
        override fun run() {
            millis += 1000
            SharedData.time.value = millis
            // Schedule the next iteration after 1 second
            handler.postDelayed(this, 1000)

        }
    }
    private val locationCallback1 = object : LocationCallback() {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        @SuppressLint("SuspiciousIndentation")
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation = locationResult.lastLocation
            if (lastLocation != null && previousLocation != null && mili != 0L && myDataBase != null) {
                if (!checkStart) {
                    myDataBase!!.movementDao().insertMovementData(
                        MovementData(
                            0,
                            System.currentTimeMillis(),
                            lastLocation.latitude,
                            lastLocation.longitude,
                            0.0,
                            0.0,
                            0F,
                            0F,
                            0F,
                            0F
                        )
                    )
                    lastMovementDataId = myDataBase!!.movementDao().getLastMovementDataId()
                    checkStart = true
                }
                val distance = getDistance(lastLocation)
                val currentSpeed = getCurrentSpeed(lastLocation)
                listSpeed.add(currentSpeed.toFloat())
                val maxSpeed = getMaxSpeed()
                val averageSpeed = getAverageSpeed()

Log.d("oksssss",averageSpeed.toString())

                updateNotification(
                    currentSpeed.toInt().toString(),
                    distance.toInt().toString(),
                    maxSpeed.toInt().toString()
                )
                with(SharedData) {
                    locationLiveData.value = lastLocation
                    distanceLiveData.value = distance
                    currentSpeedLiveData.value = currentSpeed.toFloat()
                    maxSpeedLiveData.value = maxSpeed
                    averageSpeedLiveData.value = averageSpeed
                }
                myDataBase!!.locationDao().insertLocationData(
                    lastLocation.latitude,
                    lastLocation.longitude,
                    lastMovementDataId
                )
                val movementData = myDataBase!!.movementDao().getLastMovementData()
                movementData.time = millis.toFloat()
                movementData.averageSpeed = averageSpeed
                movementData.maxSpeed = maxSpeed
                movementData.distance = distance
                myDataBase!!.movementDao().updateMovementData(movementData)
                sharedPreferences.edit().putInt(
                    MyLocationConstants.DISTANCE,
                    (sharedPreferences.getInt(MyLocationConstants.DISTANCE, 0) + distance).toInt()
                ).apply()

                if (checkPause || checkStop) {

                    val movementData = myDataBase!!.movementDao().getLastMovementData()
                    movementData.endLatitude = lastLocation.latitude
                    movementData.endLongitude = lastLocation.longitude
                    myDataBase!!.movementDao().updateMovementData(movementData)
                    if (checkStop) {
                        stopSelf()
                        removeCallBack()
                        handler.removeCallbacks(countRunnable)
                        with(SharedData) {
                            locationLiveData.value = null
                            distanceLiveData.value = 0F
                            currentSpeedLiveData.value = 0F
                            maxSpeedLiveData.value = 0F
                            averageSpeedLiveData.value = 0F
                            time.value = 0
                        }
                    }
                }
            }
            //s=vt
            previousLocation = lastLocation
            mili = System.currentTimeMillis()
        }
    }

    private fun getCurrentSpeed(lastLocation: Location): Float {
        if (acceleration < 2) {
            return 0F
        }
        val time = (System.currentTimeMillis() - mili) / 1000.0
        val s = nearestDistance(lastLocation)
        return ((s / time) * 3.6).toFloat()
    }

    private fun getMaxSpeed(): Float {
        return listSpeed.max()
    }

    private fun nearestDistance(lastLocation: Location): Float {
        return previousLocation!!.distanceTo(lastLocation)
    }

    private fun getDistance(lastLocation: Location): Float {
        distance += previousLocation!!.distanceTo(lastLocation)
        return distance / 1000
    }

    private fun getAverageSpeed(): Float {
        return (3.6 * (distance / (millis / 1000.0))).toFloat()
    }



    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
        .setWaitForAccurateLocation(true)
        .setMinUpdateIntervalMillis(100)
        .setMaxUpdateDelayMillis(100)
        .build()

    private fun handle(action: String?) {
        when (action) {
            MyLocationConstants.START -> {
                checkStart = false
                startCallBack()
                startForeground(1, getNotifications("0", "0", "0"))
                handler.postDelayed(countRunnable, 1000)
            }

            MyLocationConstants.PAUSE -> {
                checkPause = true
                fusedLocationClient?.removeLocationUpdates(locationCallback1)
                handler.removeCallbacks(countRunnable)
            }

            MyLocationConstants.RESUME -> {
                checkPause = false
                handler.postDelayed(countRunnable, 1000)
                startCallBack()

            }

            MyLocationConstants.STOP -> {
                checkStop = true

            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun startCallBack() {
        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback1,
            Looper.getMainLooper()
        )
    }

    private fun removeCallBack() {
        fusedLocationClient?.removeLocationUpdates(
            locationCallback1
        )
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
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_IMMUTABLE
        )
        notificationLayout.setTextViewText(R.id.txtKm, km)
        notificationLayout.setTextViewText(R.id.txtDistance, distance)
        notificationLayout.setTextViewText(R.id.txtMaxSpeed, maxSpeed)
        return NotificationCompat.Builder(this, "1")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setCustomContentView(notificationLayout)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
             .setSound(null)
            .setSilent(true)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS or NotificationCompat.DEFAULT_VIBRATE) // Tắt âm thanh, chỉ nhấp nháy đèn và rung
            .build()
    }

    private val accelerometerData = FloatArray(3)
    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            accelerometerData[0] = event.values[0]
            accelerometerData[1] = event.values[1]
            accelerometerData[2] = event.values[2]

            val x = accelerometerData[0]
            val y = accelerometerData[1]
            val z = accelerometerData[2]
            acceleration =
                ((x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH)).toDouble()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}


