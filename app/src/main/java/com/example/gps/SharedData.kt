package com.example.gps

import android.annotation.SuppressLint
import android.app.Activity
import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData

@SuppressLint("StaticFieldLeak")
object SharedData {
    val averageSpeedLiveData = MutableLiveData<Double>(0.0)
    val maxSpeedLiveData = MutableLiveData<Double>(0.0)
    val currentSpeedLiveData = MutableLiveData<HashMap<Double, Long>>(hashMapOf(0.0 to 0L))
    val distanceLiveData = MutableLiveData<Double>(0.0)
    val locationLiveData = MutableLiveData<Location>()


    val speedAnalog = MutableLiveData<Int>()
    val time = MutableLiveData<Long>(0)
    val checkPermission = MutableLiveData<Boolean>( )
    var fromUnit = "km/h"
    var toUnit = ""

    var fromUnitDistance = "km"
    var toUnitDistance = ""
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
        )

        )
    private val conversionRatesDistance = mapOf(
        "mi" to mapOf(
            "mi" to 1.0,
            "km" to 1.60934,
            "nm" to 1.15078
        ),
        "km" to mapOf(
            "mi" to 0.621371,
            "km" to 1.0,
            "nm" to 0.539957
        ),
        "nm" to mapOf(
            "mi" to 0.868976,
            "km" to 1.852,
            "nm" to 1.0
        )
    )


    // Hàm chuyển đổi vận tốc
    fun convertSpeed(speed: Double ): Double {
        try {
            var sp = speed * conversionRates[fromUnit]!![toUnit]!!
             return sp
        } catch (e: Exception) {
            Log.d("convertSpeed","$fromUnit $toUnit  ")

        }

        return 0.0
    }
    fun convertDistance(distance:Double ): Double {
        try {
            val sp :Double= distance * conversionRatesDistance[fromUnitDistance]!![toUnitDistance]!!
            Log.d("convertDistance","$fromUnitDistance $toUnitDistance $distance ${conversionRatesDistance["km"]!![toUnitDistance]!!}   $sp")
            return sp
        } catch (e: Exception) {
            Log.d("convertDistance","${e.message.toString()}")

        }
        return 0.0
    }
    var activity: Activity? = null

}
