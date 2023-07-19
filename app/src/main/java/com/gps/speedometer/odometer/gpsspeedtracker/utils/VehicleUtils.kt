package com.gps.speedometer.odometer.gpsspeedtracker.utils

class VehicleUtils {
    companion object {
        fun getUnit(type: Int): String {
            when (type) {
                3 -> return "knot"
                2 -> return "km/h"
                1 -> return "mph"
            }
            return ""
        }
        fun getVehicle(type: Int): String {
            when (type) {
                1 -> return "Bicycle"
                2 -> return "Motorbike"
                3 -> return "Car"
            }
            return ""
        }
    }
}