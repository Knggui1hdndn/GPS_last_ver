package com.gps.speedometer.odometer.gpsspeedtracker.presenter

import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.gps.speedometer.odometer.gpsspeedtracker.interfaces.MeasurementInterFace
import com.gps.speedometer.odometer.gpsspeedtracker.`object`.SharedData

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
         view.displayColorChange(it)
        }
    }

    override fun currentSpeedChange() {
        SharedData.currentSpeedLiveData.observe(fragment) {
             if (it!=null){
                 it[it.keys.first()]?.let { it1 ->
                     view.displayCurrentSpeedChange(
                         if (it.keys.first() <= 0) "000" else "%03d".format(
                             SharedData.convertSpeed(it.keys.first()).toInt()
                         ), it1
                     )
                 }
             }
        }
    }
}