package com.example.gps.interfaces

import android.widget.TextView

interface MapLiveDataInterface {
    interface View {
        fun showMaxSpeed(string:String)
        fun showDistance(string:String)
        fun showAverageSpeed(string:String)

    }

    interface Presenter {
        fun getMaxSpeed()
        fun getDistance()
        fun getAverageSpeed()

    }
}