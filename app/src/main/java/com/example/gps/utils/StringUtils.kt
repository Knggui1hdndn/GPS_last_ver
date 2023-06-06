package com.example.gps.utils

import com.example.gps.SharedData

class StringUtils {
    companion object{
        fun convert(db:Double): String  {
            return "${SharedData.convertSpeed(db).toInt()}" + SharedData.toUnit
        }
    }
}