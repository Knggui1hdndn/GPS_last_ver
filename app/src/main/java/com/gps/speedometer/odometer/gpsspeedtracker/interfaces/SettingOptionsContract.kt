package com.gps.speedometer.odometer.gpsspeedtracker.interfaces

import android.widget.EditText
import android.widget.Toast
import com.google.android.material.button.MaterialButton

interface SettingOptionsContract {
    interface View {
        fun btnOkCLick()
        fun setDefaultViewMode()
        fun setDefaultUnit()
        fun setDefaultVehicle()
        fun clickUnit(vararg btn: MaterialButton)
        fun clickViewMode(vararg btn: MaterialButton)
        fun setTextAndStrokeWidthColor(vararg btn: MaterialButton)
        fun clickVehicle(vararg btn: MaterialButton)
        fun setBackGroundBtnClick(btn: MaterialButton)
        fun showDialog()
        fun Toast(s:String)
    }

    interface Presenter {
        fun setDataConfig(
            unitClick: String,
            viewModeClick: Int,
            vehicleClick: Int,
            edtSpeedLimit: EditText
        )
    }
}