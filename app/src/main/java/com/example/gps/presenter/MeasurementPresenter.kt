package com.example.gps.presenter

import android.content.Context.MODE_PRIVATE
import android.view.View
import androidx.fragment.app.Fragment
import com.example.gps.constants.SettingConstants
import com.example.gps.interfaces.MeasurementInterFace
import com.example.gps.`object`.SharedData

class MeasurementPresenter(val view: MeasurementInterFace.View, val fragment: Fragment) :
    MeasurementInterFace.Presenter {
    override fun setVisibilityTime() {
        val sharedPreferences =
            fragment.activity?.getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE)
        val visibility=if(sharedPreferences!!.getBoolean(SettingConstants.CLOCK_DISPLAY, true)) View.VISIBLE else View.INVISIBLE
        view.onVisibilityTime(visibility)
    }

    override fun timeChange() {
        SharedData.time.observe(fragment) {
            view.displayTimeChange(it)
        }
    }

    override fun colorChange() {
        SharedData.color.observe(fragment) {
            view.displayColorChange(it)
        }
    }

    override fun currentSpeedChange() {
        SharedData.currentSpeedLiveData.observe(fragment) {
            it[it.keys.first()]?.let { it1 ->
                view.displayCurrentSpeedChange(
                    if (it1 <= 0) "000" else "%03d".format(
                        SharedData.convertSpeed(it.keys.first()).toInt()
                    ), it1
                )
            }
        }
    }
}