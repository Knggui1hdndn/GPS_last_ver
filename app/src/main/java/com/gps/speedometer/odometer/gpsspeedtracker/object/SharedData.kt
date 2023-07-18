package com.gps.speedometer.odometer.gpsspeedtracker.`object`

import android.annotation.SuppressLint
import android.app.Activity
import android.location.Location
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat

@SuppressLint("StaticFieldLeak")
object SharedData {
    val averageSpeedLiveData = MutableLiveData(0.0)
    val maxSpeedLiveData = MutableLiveData(0.0)
    val currentSpeedLiveData = MutableLiveData(hashMapOf(0.0 to 0L))
    val distanceLiveData = MutableLiveData(0.0)
    val locationLiveData = MutableLiveData<Location>()
    val speedAnalog = MutableLiveData<Int>()
    val color = MutableLiveData<Int>()
    val time = MutableLiveData<Long>(0)
    val onShowResetButton = MutableLiveData(0)
    val onShowTime = MutableLiveData(0)
    @SuppressLint("SimpleDateFormat")
    val onTimeStart=MutableLiveData(SimpleDateFormat("dd/MM/yyyy\n00:00:00").format(System.currentTimeMillis()))
    var fromUnit = "km/h"
    var toUnit = ""
    var fromUnitDistance = "km"
    var toUnitDistance = ""
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


     fun convertSpeed(speed: Double): Double {
        var sp = speed * conversionRates[fromUnit]!![toUnit]!!
        return sp

    }

    fun convertDistance(distance: Double): Double {

        return try {
            distance * conversionRatesDistance[fromUnitDistance]!![toUnitDistance]!!
        }catch (e:Exception){
            0.0
        }
    }

    var activity: Activity? = null

}
