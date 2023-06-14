package com.example.gps.interfaces

import android.annotation.SuppressLint
import android.location.GnssStatus
import android.os.Build
import androidx.annotation.RequiresApi

interface SignalInterface {
    interface View {

        fun onStrengthGPSDataReceived(strength: Int, satelliteCount: Int)
    }

    interface Presenter {
        fun registerGnssStatusCallback()
        fun unRegisterGnssStatusCallback()
    }
}