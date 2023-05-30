package com.example.gps.utils

import com.example.gps.model.Speed
import com.google.android.gms.maps.OnMapReadyCallback

class UnitUtils {
    companion object {
        fun check(typeId: Int, callback: (String, Double) -> Unit) {
            when (typeId) {
                1 -> callback("Mph", 0.621)
                3 -> callback("Knot", 0.539957)
                2 -> callback("Km/h", 1.0)
            }

        }
    }
}