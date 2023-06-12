package com.example.gps.presenter

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import com.example.gps.SharedData
import com.example.gps.interfaces.MapLiveDataInterface

class MapLiveDataPresenter(
    private val context: Fragment,
    private val view: MapLiveDataInterface.View
) : MapLiveDataInterface.Presenter {
    override fun getMaxSpeed() {
        SharedData.maxSpeedLiveData.observe(context.viewLifecycleOwner) {
            view.showMaxSpeed(
                if (it <= 0) "0" + SharedData.toUnit else String.format(
                    "%.0f",
                    SharedData.convertSpeed(it)
                ) + SharedData.toUnit
            )
        }
    }

    override fun getDistance() {
        SharedData.distanceLiveData.observe(context.viewLifecycleOwner) {
            view.showDistance(
                String.format(
                    "%.2f",
                    SharedData.convertDistance(it)
                ) + SharedData.toUnitDistance
            )
        }
    }

    override fun getAverageSpeed() {
        SharedData.averageSpeedLiveData.observe(context.viewLifecycleOwner) {
            view.showAverageSpeed( if (it <= 0) "0" + SharedData.toUnit else String.format(
                "%.0f",
                SharedData.convertSpeed(it)
            ) + SharedData.toUnit)
        }
    }


}