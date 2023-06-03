package com.example.gps

import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData

object SharedData {
    val averageSpeedLiveData = MutableLiveData<HashMap<Float,String>>()
    val maxSpeedLiveData = MutableLiveData<HashMap<Float,String>>()
    val currentSpeedLiveData = MutableLiveData<HashMap<Float,Long>>()
    val distanceLiveData = MutableLiveData<HashMap<Float,String>>()
    val locationLiveData = MutableLiveData<Location>()
    val speedAnalog = MutableLiveData<Int>()
    val time = MutableLiveData<Long>(0)
    var fromUnit = ""
    var toUnit = ""
    var checkService = false

    val conversionRates = mapOf(
        "knot" to mapOf(
            "km/h" to 1.852, "mph" to 1.15078, "knot" to 1.0

        ),
        "km/h" to mapOf(
            "knot" to 0.539957, "mph" to 0.621371, "km/h" to 1.0
        ),
        "mph" to mapOf(
            "knot" to 0.868976, "km/h" to 1.60934, "mph" to 1.0
        ),

        )


    // Hàm chuyển đổi vận tốc
    fun convertSpeed(speed: Float): Double {
        try {
            val sp =speed * conversionRates[fromUnit]!![toUnit]!!
             return sp
        } catch (e: Exception) {
        }
        fromUnit = toUnit
        return 1.0
    }
}
