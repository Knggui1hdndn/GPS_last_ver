package com.example.gps.interfaces

interface SettingInterface {
    interface View{
        fun onRemoveBackGroundVehicle()
        fun onRemoveBackGroundUnit()
        fun onRemoveBackGroundViewMode()
        fun onClickVehicle()
        fun onClickUnit()
        fun onClickViewMode()
        fun onColorChange()
    }
    interface Presenter{
        fun updateVehicle()
        fun updateViewMode()
        fun updateUnit()
    }
}