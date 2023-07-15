package com.gps.speedometer.odometer.gpsspeedtracker.presenter

import android.app.Service
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.gps.speedometer.odometer.gpsspeedtracker.R
import com.gps.speedometer.odometer.gpsspeedtracker.constants.MyLocationConstants
import com.gps.speedometer.odometer.gpsspeedtracker.constants.SettingConstants
import com.gps.speedometer.odometer.gpsspeedtracker.`object`.SharedData
import com.gps.speedometer.odometer.gpsspeedtracker.dao.MyDataBase
import com.gps.speedometer.odometer.gpsspeedtracker.interfaces.MotionCalculatorInterface
import com.gps.speedometer.odometer.gpsspeedtracker.service.MyService
import com.gps.speedometer.odometer.gpsspeedtracker.ui.ShowActivity

class MotionCalculatorPresenter(
    private val context: Context,
    private val listSpeed: MutableList<Double>,
    private val myDataBase: MyDataBase
) : MotionCalculatorInterface {
    private val shareShow: SharedPreferences=context.getSharedPreferences("show",  MODE_PRIVATE)
    private var distance = 0.0
    private var timer: Long = 0
    private lateinit var previousLocation: Location
    private val handler = Handler(Looper.getMainLooper())
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("state", Service.MODE_PRIVATE)
    val sharedPreferencesSetting =
        context.getSharedPreferences(SettingConstants.SETTING, Context.MODE_PRIVATE)
    private val mediaPlayer: MediaPlayer = MediaPlayer.create(context, R.raw.wraning)

    private var lastMovementDataId = myDataBase.movementDao().getLastMovementDataId()

    private val countRunnable: Runnable = object : Runnable {
        override fun run() {
            timer += 1000
            SharedData.time.value = timer
            SharedData.averageSpeedLiveData.value = getAverageSpeed()
            handler.postDelayed(this, 1000)
        }
    }

    // Phương thức tính toán

    override fun calculateDistance(lastLocation: Location): Double {
        if (this::previousLocation.isInitialized) {
            val distanceInMeters = lastLocation.distanceTo(previousLocation)
            distance += distanceInMeters / 1000.0
            sharedPreferences.edit().putInt(
                MyLocationConstants.DISTANCE,
                (sharedPreferences.getInt(
                    MyLocationConstants.DISTANCE,
                    0
                ) + (distanceInMeters / 1000.0)).toInt()
            ).apply()
        }
        lastMovementDataId = myDataBase.movementDao().getLastMovementDataId()
        previousLocation = lastLocation
        return distance
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun calculateSpeed(lastLocation: Location): Double {
        if (!lastLocation.hasSpeed()) return 0.0
        val speed = (lastLocation.speed * 3.6)
        if (sharedPreferencesSetting.getBoolean(SettingConstants.SPEED_ALARM, true)) {
            when {
                SharedData.convertSpeed(speed).toInt() >= getWarningLimit() -> {
                    broadcastWarning()
                }

                else -> {
                    stopWarning()
                }
            }
        } else {
            stopWarning()
        }
        return speed
    }

    override fun calculateTime(): Long {
        return (System.currentTimeMillis() - previousLocation.time) / 1000 // Đổi từ millisecond sang giây
    }

    override fun calculateMaxSpeed(): Double {
        return listSpeed.maxOrNull() ?: 0.0
    }

    override fun getAverageSpeed(): Double {
        return if (timer > 0) {
            ((distance * 1000) / (timer / 1000.0)) * 3.6
        } else {
            0.0
        }
    }

    // Phương thức truy cập dữ liệu

    override fun getWarningLimit(): Int {
        return myDataBase.vehicleDao().getVehicleChecked().limitWarning
    }

    // Phương thức quản lý thời gian

    override fun startTimer() {
        handler.postDelayed(countRunnable, 1000)
    }

    override fun pauseTimer() {
        handler.removeCallbacks(countRunnable)
    }

    override fun stopTimer() {
        handler.removeCallbacks(countRunnable)
    }

    // Phương thức cập nhật dữ liệu vị trí và chuyển động

    @RequiresApi(Build.VERSION_CODES.O)
    override fun updateLocation(
        lastLocation: Location,
        call: (Double, Double, Double, Double, Long) -> Unit
    ) {
        val distance = calculateDistance(lastLocation)
        val averageSpeed = getAverageSpeed()
        val currentSpeed = calculateSpeed(lastLocation)

        listSpeed.add(currentSpeed)
        val maxSpeed = calculateMaxSpeed()
        val time = calculateTime()
        call(averageSpeed, currentSpeed, distance, maxSpeed, time)
        updateMovementData(averageSpeed, distance, maxSpeed)
    }

    override fun updateMovementData(speed: Double, distance: Double, maxSpeed: Double) {
        val movementData = myDataBase.movementDao().getLastMovementData()
        movementData.apply {
            this.averageSpeed = speed
            this.maxSpeed = maxSpeed
            this.distance = distance
            this.time = timer
        }
        myDataBase.movementDao().updateMovementData(movementData)
    }

    override fun updateMovementDataWhenStart() {
        val movementData = myDataBase.movementDao().getLastMovementData()
        movementData.apply {
            startLongitude = previousLocation.longitude
            startLatitude = previousLocation.latitude
        }
        myDataBase.movementDao().updateMovementData(movementData)
    }

    override fun updateMovementDataWhenStop() {
        val movementData = myDataBase.movementDao().getLastMovementData()
        movementData.time = timer
        if (this::previousLocation.isInitialized) {
            movementData.endLatitude = previousLocation.latitude
            movementData.endLongitude = previousLocation.longitude
        } else {
            movementData.endLatitude = 0.0
            movementData.endLongitude = 0.0
        }
        myDataBase.movementDao().updateMovementData(movementData)
        shareShow.edit().putInt("id",movementData.id).apply()
        resetSharedData()
        context.stopService(Intent(context, MyService::class.java))
    }

    override fun resetSharedData() {
        with(SharedData) {
            locationLiveData.value = null
            distanceLiveData.value = 0.0
            currentSpeedLiveData.value = hashMapOf(0.0 to 0)
            maxSpeedLiveData.value = 0.0
            averageSpeedLiveData.value = 0.0
            time.value = 0
        }
    }

    override fun insertLocationData(lastLocation: Location) {
        myDataBase.locationDao().insertLocationData(
            lastLocation.latitude, lastLocation.longitude, lastMovementDataId
        )
    }

    // Phương thức phát cảnh báo
    override fun broadcastWarning() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
//            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
//            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
        }

    }

    override fun stopWarning() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.prepare()

        }
    }
}

