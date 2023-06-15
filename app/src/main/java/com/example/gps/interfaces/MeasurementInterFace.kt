package com.example.gps.interfaces

interface MeasurementInterFace {
    interface View {
        fun onVisibilityTime(visibility:Int)
        fun displayTimeChange(long: Long)
        fun displayColorChange(int: Int)
        fun displayCurrentSpeedChange(string: String,l:Long)
    }

    interface Presenter {
        fun setVisibilityTime()
        fun onTimeChange()
        fun onColorChange()
        fun onCurrentSpeedChange()

    }
}