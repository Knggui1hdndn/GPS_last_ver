package com.example.gps

import android.annotation.SuppressLint
import android.app.NotificationManager
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
import android.os.Looper
import androidx.annotation.RequiresApi
import com.example.gps.dao.MyDataBase
import com.example.gps.model.MovementData
import com.example.gps.service.MyService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

interface LocationChangeListener {
    fun onLocationChanged(km: String, distance: String, maxSpeed: String)
}

class Map() : SensorEventListener {
    private val accelerometerData = FloatArray(3)
    private val handler = Handler(Looper.getMainLooper())
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var myDataBase: MyDataBase? = null
    private var millis = 0L
    private var distance = 0f
    private var lastMovementDataId = 0
    private var milli = 0L
    private var previousLocation: Location? = null
    private var listSpeed = mutableListOf<Float>()
    private lateinit var sharedPreferences: SharedPreferences
    private var acceleration: Double = 0.0
    private var notificationManager: NotificationManager? = null
    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null
    var checkStart: Boolean = false
    var checkPause: Boolean = false
    var checkStop: Boolean = false
    private lateinit var locationChangeListener: LocationChangeListener
    private lateinit var context: Context

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
            if (lastLocation != null && previousLocation != null && milli != 0L && myDataBase != null) {
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
                listSpeed.add(0F)
                listSpeed.add(currentSpeed.toFloat())
                val maxSpeed = getMaxSpeed()
                val averageSpeed = getAverageSpeed()
                locationChangeListener.onLocationChanged(
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
                if (checkPause || checkStop) {
                    val movementData = myDataBase!!.movementDao().getLastMovementData()
                    movementData.endLatitude = lastLocation.latitude
                    movementData.endLongitude = lastLocation.longitude
                    myDataBase!!.movementDao().updateMovementData(movementData)
                    if (checkStop) {
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
                        context.stopService(Intent(context, MyService::class.java))
                    }
                }
            }
            //s=vt
            previousLocation = lastLocation
            milli = System.currentTimeMillis()
        }
    }

    private fun getCurrentSpeed(lastLocation: Location): Float {
        if (!lastLocation.hasSpeed()) return 0F
        val time = (System.currentTimeMillis() - milli) / 1000.0
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

    @SuppressLint("MissingPermission")
    fun startCallBack() {
        sensorManager!!.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        fusedLocationClient?.requestLocationUpdates(
            locationRequest, locationCallback1, Looper.getMainLooper()
        )
    }

    fun removeCallBack() {
        sensorManager!!.unregisterListener(this)
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


    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
        .setWaitForAccurateLocation(true).setMinUpdateIntervalMillis(100)
        .setMaxUpdateDelayMillis(100).build()

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