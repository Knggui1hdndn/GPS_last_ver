package com.example.gps.utils

import com.google.android.gms.maps.GoogleMap

class MapUtils {
    companion object {
        var count =0
        fun setStStyleMap(map: GoogleMap ) {
            when (count) {
                0 -> {
                    map.mapType = GoogleMap.MAP_TYPE_HYBRID
                }

                1 -> {
                    map.mapType = GoogleMap.MAP_TYPE_NONE
                }

                2 -> {
                    map.mapType = GoogleMap.MAP_TYPE_NORMAL
                }

                3 -> {
                    map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                }

                4 -> {
                    map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                }
            }
            if (count == 4) count = 0 else count++
        }
    }
}