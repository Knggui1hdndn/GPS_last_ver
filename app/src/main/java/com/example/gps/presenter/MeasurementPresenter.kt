package com.example.gps.presenter

import androidx.fragment.app.Fragment
import com.example.gps.interfaces.MeasurementInterFace
import com.example.gps.`object`.SharedData

class MeasurementPresenter(val view: MeasurementInterFace.View, val fragment: Fragment) :
    MeasurementInterFace.Presenter {
    override fun onTimeChange() {
        SharedData.time.observe(fragment) {
            view.displayTimeChange(it)
        }
    }

    override fun onColorChange() {
        SharedData.color.observe(fragment) {
            view.displayColorChange(it)
        }
    }

    override fun onCurrentSpeedChange() {
        SharedData.currentSpeedLiveData.observe(fragment) {
            it[it.keys.first()]?.let { it1 ->
                view.displayCurrentSpeedChange(
                    if (it1 <= 0) "000" else "%03d".format(
                        SharedData.convertSpeed(it.keys.first()).toInt()
                    ) ,it1
                )
            }
        }
    }
}