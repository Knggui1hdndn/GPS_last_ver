package com.example.gps.utils

import com.example.gps.model.Speed
import com.google.android.gms.maps.OnMapReadyCallback

class UnitUtils {
    companion object {
        fun getUnit(type: Int): String {
            when (type) {
                3 -> return "knot"
                2 -> return "km/h"
                1 -> return "mph"
            }
            return ""
        }
    }
}