package com.gps.speedometer.odometer.gpsspeedtracker.interfaces

import android.location.Location

interface MotionCalculatorInterface {
    fun calculateSpeed(lastLocation: Location): Double
    fun calculateTime(): Long
    fun calculateMaxSpeed(): Double
    fun updateLocation(lastLocation: Location, call: (Double, Double, Double, Double, Long) -> Unit)
    fun calculateDistance(lastLocation: Location): Double
    fun getAverageSpeed(): Double
    fun getWarningLimit(): Int
    fun startTimer()
    fun pauseTimer()
    fun stopTimer()
    fun broadcastWarning()
    fun stopWarning()
    fun updateMovementData(speed: Double, distance: Double, maxSpeed: Double)
    fun updateMovementDataWhenStart()
    fun updateMovementDataWhenStop()
    fun resetSharedData()
    fun insertLocationData(lastLocation: Location)
    interface MapInterFace {
        fun startCallBack()
        fun removeCallBack()
        fun setStop()
        fun setStart()
    }
}