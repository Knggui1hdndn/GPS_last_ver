package com.gps.speedometer.odometer.gpsspeedtracker.presenter

import android.content.Context
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.gps.speedometer.odometer.gpsspeedtracker.constants.ColorConstants
import com.gps.speedometer.odometer.gpsspeedtracker.constants.SettingConstants
import com.gps.speedometer.odometer.gpsspeedtracker.dao.MyDataBase
import com.gps.speedometer.odometer.gpsspeedtracker.interfaces.SettingOptionsContract

class SettingOptionsPresenter(val context:Context):SettingOptionsContract.Presenter {
    override fun setDataConfig(unitClick:String,viewModeClick:Int,vehicleClick:Int,edtSpeedLimit:EditText) {
        var myDataBase: MyDataBase = MyDataBase.getInstance(context)
        val sharedPreferences = context.getSharedPreferences(
            SettingConstants.SETTING,
            AppCompatActivity.MODE_PRIVATE
        )
        sharedPreferences.edit().apply {
            putString(SettingConstants.UNIT, unitClick)
            putInt(SettingConstants.ViEW_MODE, viewModeClick)
            putInt(SettingConstants.COLOR_DISPLAY,  ColorConstants.COLOR_DEFAULT)
            putBoolean(SettingConstants.DISPLAY_SPEED, true)
            putBoolean(SettingConstants.TRACK_ON_MAP, true)
            putBoolean(SettingConstants.SHOW_RESET_BUTTON, true)
            putBoolean(SettingConstants.SPEED_ALARM, true)
            putBoolean(SettingConstants.CHECK_OPEN, true)
        }.apply()
        myDataBase.vehicleDao().deleteAll()
        myDataBase.vehicleDao()
            .insertVehicle(80, 40, 1, if (vehicleClick == 1) 1 else 0)
        myDataBase.vehicleDao()
            .insertVehicle(180, 80, 2, if (vehicleClick == 2) 1 else 0)
        myDataBase.vehicleDao()
            .insertVehicle(360, 120, 3, if (vehicleClick == 3) 1 else 0)
        myDataBase.vehicleDao().updateWarning(edtSpeedLimit.text.toString().toInt())
    }
}