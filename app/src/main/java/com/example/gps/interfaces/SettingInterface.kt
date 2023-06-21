package com.example.gps.interfaces

interface SettingInterface {
    interface View{
        fun onRemoveBackGroundVehicle()
        fun onRemoveBackGroundUnit()
        fun onRemoveBackGroundViewMode()

    }
    interface Presenter{
        fun updateVehicle()
        fun updateViewMode()
        fun updateUnit()
    }
}