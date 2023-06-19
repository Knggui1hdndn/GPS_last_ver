package com.example.gps.presenter

import android.content.Context.MODE_PRIVATE
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.gps.constants.SettingConstants
import com.example.gps.interfaces.MeasurementInterFace
import com.example.gps.`object`.SharedData

class MeasurementPresenter(val view: MeasurementInterFace.View, val fragment: Fragment) :
    MeasurementInterFace.Presenter {
    override fun setVisibilityTime() {
        SharedData.onShowTime.observe(fragment) {
            view.onVisibilityTime(it)
        }

    }

    override fun timeChange() {
        SharedData.time.observe(fragment) {
            view.displayTimeChange(it)
        }
    }

    override fun colorChange() {
        SharedData.color.observe(fragment) {
            Log.d("okokodds",it.toString())
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