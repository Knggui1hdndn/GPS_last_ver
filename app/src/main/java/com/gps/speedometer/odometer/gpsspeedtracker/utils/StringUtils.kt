package com.gps.speedometer.odometer.gpsspeedtracker.utils

import com.gps.speedometer.odometer.gpsspeedtracker.`object`.SharedData

class StringUtils {
    companion object{
        fun convert(db:Double): String  {
            return "${SharedData.convertSpeed(db).toInt()}" + SharedData.toUnit
        }
    }
}