package com.example.gps.presenter

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import com.example.gps.MyLocationConstants
import com.example.gps.R
import com.example.gps.SettingConstants
import com.example.gps.SharedData
import com.example.gps.dao.MyDataBase
import com.example.gps.interfaces.MotionCalculatorInterface
import com.example.gps.service.MyService
import com.example.gps.ui.ShowActivity

class MotionCalculatorPresenter(
    private val context: Context,
    private val listSpeed: MutableList<Double>,
    private val myDataBase: MyDataBase
) : MotionCalculatorInterface {

    private var distance = 0.0
    private var timer: Long = 0
    private lateinit var previousLocation: Location
    private val handler = Handler(Looper.getMainLooper())
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("state", Service.MODE_PRIVATE)
    val sharedPreferencesSetting =
        context.getSharedPreferences(SettingConstants.SETTING, Context.MODE_PRIVATE)
    private val mediaPlayer: MediaPlayer = MediaPlayer.create(context, R.raw.wraning)
    private var isWarningPlaying = false
    private var lastMovementDataId = myDataBase.movementDao().getLastMovementDataId()

    private val countRunnable: Runnable = object : Runnable {
        override fun run() {
            timer += 1000
            SharedData.time.value = timer
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
                ) + (distanceInMeters / 1000)).toInt()
            ).apply()
        }
        lastMovementDataId = myDataBase.movementDao().getLastMovementDataId()
        previousLocation = lastLocation
        return distance
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun calculateSpeed(lastLocation: Location): Double {
        if (!lastLocation.hasSpeed()) return 0.0
        val speed =  if (lastLocation.hasSpeedAccuracy() && lastLocation.hasSpeed()) (lastLocation.speed * 3.6) else 0.0
        if (sharedPreferencesSetting.getBoolean(SettingConstants.SPEED_ALARM, false)) {
            when {
                SharedData.convertSpeed(speed) > SharedData.convertSpeed(getWarningLimit().toDouble()) -> {
                    broadcastWarning()
                }
                else -> stopWarning()
            }
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
        return if (timer > 0) (distance / (timer / 1000.0)) * 3.6 else 0.0 // Đổi từ mét/giây sang kilômét/giờ
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
        if (this::previousLocation.isInitialized) {
            movementData.endLatitude = movementData.startLatitude
            movementData.endLongitude = movementData.startLongitude
        } else {
            movementData.endLatitude = previousLocation.latitude
            movementData.endLongitude = previousLocation.longitude
        }
        myDataBase.movementDao().updateMovementData(movementData)
        resetSharedData()
        val i = Intent(context, ShowActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        i.putExtra("movementData", myDataBase.movementDao().getLastMovementData())
        context.startActivity(i)
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
        if (!isWarningPlaying) {
            mediaPlayer.start()
            isWarningPlaying = true
        }
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
    }

    override fun stopWarning() {
        if (isWarningPlaying) {
            mediaPlayer.stop()
            isWarningPlaying = false
        }
    }
}

