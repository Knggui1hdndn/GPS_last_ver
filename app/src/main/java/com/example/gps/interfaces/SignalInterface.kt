package com.example.gps.interfaces

interface  SignalInterface{
    fun onBatteryDataReceived(int: Int)
    fun onStrengthGPSDataReceived(strength: Int, satelliteCount: Int)

}