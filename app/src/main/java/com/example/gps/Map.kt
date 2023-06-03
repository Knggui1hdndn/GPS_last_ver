package com.example.gps

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore.Video.Media
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import com.example.gps.dao.MyDataBase
import com.example.gps.model.MovementData
import com.example.gps.service.MyService
import com.example.gps.ui.ShowActivity
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

class Map() {
    private val handler = Handler(Looper.getMainLooper())
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var myDataBase: MyDataBase? = null
    private var millis = 0L
    private var distance = 0f
    private var lastMovementDataId = 0
    private var previousLocation: Location? = null
    private var listSpeed = mutableListOf<Float>()
    private lateinit var sharedPreferences: SharedPreferences
    private var notificationManager: NotificationManager? = null
    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null
    var checkStart: Boolean = false
    var checkPause: Boolean = false
    var checkStop: Boolean = false
    private lateinit var locationChangeListener: LocationChangeListener
    private lateinit var context: Context
    var mediaPlayer: MediaPlayer? = null

    constructor(context: Context, locationChangeListener: LocationChangeListener) : this() {
        this.locationChangeListener = locationChangeListener
        this.context = context
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        myDataBase = MyDataBase.getInstance(context)
        sharedPreferences = context.getSharedPreferences("state", Service.MODE_PRIVATE)
        notificationManager =
            context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    }

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

            if (lastLocation != null && previousLocation != null && myDataBase != null) {

                if (!checkStart) {
                    val movementData = myDataBase!!.movementDao().getLastMovementData()
                    movementData.apply {
                        startLongitude = previousLocation!!.longitude
                        startLatitude = previousLocation!!.latitude
                    }
                    myDataBase!!.movementDao().updateMovementData(movementData)
                    lastMovementDataId = myDataBase!!.movementDao().getLastMovementDataId()
                    checkStart = true
                }
                val distance = getDistance(lastLocation)
                val currentSpeed = getCurrentSpeed(lastLocation)
                listSpeed.add(0F)
                listSpeed.add(currentSpeed)
                val maxSpeed = getMaxSpeed()
                val averageSpeed = getAverageSpeed()
                locationChangeListener.onLocationChanged(
                    currentSpeed.toInt().toString(),
                    distance.toInt().toString(),
                    maxSpeed.toInt().toString()
                )
                with(SharedData) {
                    locationLiveData.value = lastLocation
                    distanceLiveData.value = hashMapOf(convertSpeed(distance).toFloat() to toUnit)
                    currentSpeedLiveData.value =
                        hashMapOf(convertSpeed(currentSpeed).toFloat() to lastLocation.time)
                    maxSpeedLiveData.value = hashMapOf(convertSpeed(maxSpeed).toFloat() to toUnit)
                    averageSpeedLiveData.value =
                        hashMapOf(convertSpeed(averageSpeed).toFloat() to toUnit)
                }
                myDataBase!!.locationDao().insertLocationData(
                    lastLocation.latitude, lastLocation.longitude, lastMovementDataId
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

            }
            //s=vt
            previousLocation = lastLocation
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentSpeed(lastLocation: Location): Float {
        if (!lastLocation.hasSpeed()) return 0F
        var speed =
            if (lastLocation.hasSpeedAccuracy() && lastLocation.hasSpeed()) (lastLocation.speed * 3.6).toFloat() else 0F

        val convertedSpeed = SharedData.convertSpeed(speed)
        val sharedPreferences =
            context.getSharedPreferences(SettingConstants.SETTING, Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean(SettingConstants.SPEED_ALARM, true)) {
            when {
                convertedSpeed > myDataBase!!.vehicleDao()
                    .getVehicleChecked(myDataBase!!.SpeedDao().getChecked().type).limitWarning -> {
                    if (mediaPlayer == null || (!mediaPlayer!!.isPlaying)) {
                        mediaPlayer = MediaPlayer.create(context, R.raw.wraning)
                        val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 100, 0)
                        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
                        // mediaPlayer?.start()
                    }
                }

                else -> {
                    if (mediaPlayer != null) mediaPlayer!!.stop()
                }
            }
        }
        return speed
    }

    private fun getMaxSpeed(): Float {
        return listSpeed.max()
    }


    private fun getDistance(lastLocation: Location): Float {
        distance += previousLocation!!.distanceTo(lastLocation)
        return distance / 1000
    }

    private fun getAverageSpeed(): Float {
        return (3.6 * (distance / (millis / 1000.0))).toFloat()
    }

    @SuppressLint("MissingPermission")
    fun startCallBack() {
        fusedLocationClient?.requestLocationUpdates(
            locationRequest, locationCallback1, Looper.getMainLooper()
        )
    }

    @SuppressLint("MissingPermission")
    fun removeCallBack() {
        if (checkStop) {
            val movementData = myDataBase!!.movementDao().getLastMovementData()
            if (checkStop) {

                if (previousLocation == null) {
                    movementData.endLatitude = movementData.startLatitude
                    movementData.endLongitude = movementData.startLongitude
                } else {
                    movementData.endLatitude = previousLocation!!.latitude
                    movementData.endLongitude = previousLocation!!.longitude
                }
                myDataBase!!.movementDao().updateMovementData(movementData)
                val i = Intent(context, ShowActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                i.putExtra("movementData", myDataBase!!.movementDao().getLastMovementData())
                context.startActivity(i)
                context.stopService(Intent(context, MyService::class.java))

                with(SharedData) {
                    locationLiveData.value = null
                    distanceLiveData.value = hashMapOf(0F to toUnit)
                    currentSpeedLiveData.value = hashMapOf(0F to 0)
                    maxSpeedLiveData.value = hashMapOf(0F to toUnit)
                    averageSpeedLiveData.value = hashMapOf(0F to toUnit)
                    time.value = 0
                }
            }
        }
        fusedLocationClient?.removeLocationUpdates(
            locationCallback1
        )
    }

    fun removeHandler() {
        handler.removeCallbacks(countRunnable)

    }

    fun postDelayed() {
        handler.postDelayed(countRunnable, 1000)
    }


    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10)
        .setWaitForAccurateLocation(true).setMinUpdateIntervalMillis(10)
        .setGranularity(Granularity.GRANULARITY_FINE).setMaxUpdateDelayMillis(10).build()

}