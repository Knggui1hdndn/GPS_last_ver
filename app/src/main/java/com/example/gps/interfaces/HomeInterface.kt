package com.example.gps.interfaces

interface HomeInterface : DisplayInterface, MeasurementInterFace {
    fun onVisibilityChanged(boolean: Boolean)
    fun onMaxSpeedAnalogChange(speed: Int)
    fun setSpeedAndUnit()
    fun toggleButtonVisibility(boolean: Boolean)


}