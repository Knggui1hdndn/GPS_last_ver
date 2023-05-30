package com.example.gps

import android.app.Service
import android.location.Location
import androidx.lifecycle.MutableLiveData

object SharedData {
    val averageSpeedLiveData = MutableLiveData<Float>()
    val maxSpeedLiveData = MutableLiveData<Float>()
    val currentSpeedLiveData = MutableLiveData<Float>()
    val distanceLiveData = MutableLiveData<Float>()
    val locationLiveData = MutableLiveData<Location>()

    val time = MutableLiveData<Long>(0)
    var unitSpeed = MutableLiveData<String>()

    var conversionRates = mutableMapOf(
        "km/h" to 1.0, // Đơn vị mặc định: km/h
        "mph" to 0.62137119,
        "knot" to 0.5399568
    )

    // Hàm chuyển đổi vận tốc
    fun convertSpeed(speed: Double, fromUnit: String, toUnit: String): Double {
        val conversionRate = conversionRates[fromUnit] ?: return speed
        val conversionFactor = conversionRates[toUnit] ?: return speed
        return speed * conversionFactor / conversionRate
    }
}
