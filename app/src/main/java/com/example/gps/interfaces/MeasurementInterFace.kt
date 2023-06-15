package com.example.gps.interfaces

interface MeasurementInterFace {
    interface View {
        fun displayTimeChange(long: Long)
        fun displayColorChange(int: Int)
        fun displayCurrentSpeedChange(string: String,l:Long)
    }

    interface Presenter {
        fun onTimeChange()
        fun onColorChange()
        fun onCurrentSpeedChange()

    }
}