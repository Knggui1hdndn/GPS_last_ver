package com.gps.speedometer.odometer.gpsspeedtracker.interfaces

interface SignalInterface {
    interface View {

        fun onStrengthGPSDataReceived(strength: Int, satelliteCount: Int)
    }

    interface Presenter {
        fun registerGnssStatusCallback()
        fun unRegisterGnssStatusCallback()
    }
}