package com.example.gps.interfaces

import com.example.gps.model.Speed

interface HomeInterface : DisplayInterface, MeasurementInterFace {
    fun onVisibilityChanged(boolean: Boolean)
    fun onMaxSpeedAnalogChange(speed: Int)
    fun setSpeedAndUnit()

}