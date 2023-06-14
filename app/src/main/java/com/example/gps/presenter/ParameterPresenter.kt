package com.example.gps.presenter

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gps.constants.MyLocationConstants
import com.example.gps.dao.MyDataBase
import com.example.gps.interfaces.ParameterContracts
import com.example.gps.model.MovementData
import com.example.gps.`object`.SharedData
import com.example.gps.service.MyService

class ParameterPresenter(
    private val context: Fragment,
    private val view: ParameterContracts.View
) : ParameterContracts.Presenter {
    private var sharedPreferences: SharedPreferences =
        context.requireContext().getSharedPreferences("state", Service.MODE_PRIVATE)
    private var myDataBase: MyDataBase = MyDataBase.getInstance(context.requireContext())

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
            view.showAverageSpeed(
                if (it <= 0) "0" + SharedData.toUnit else String.format(
                    "%.0f",
                    SharedData.convertSpeed(it)
                ) + SharedData.toUnit
            )
        }
    }


    override fun callMyService(action: String) {
        val intent = Intent(context.requireContext(), MyService::class.java)
        intent.action = action
        context.requireContext().startService(intent)
    }

    override fun insertMovementDataWhenStart() {
        myDataBase.movementDao().insertMovementData(
            MovementData(
                0,
                System.currentTimeMillis(),
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0
            )
        )
    }

    override fun startService() {
        insertMovementDataWhenStart()
        setState(MyLocationConstants.START)
        callMyService(MyLocationConstants.START)
        hideStart()
    }


    override fun stopService() {
        setState(MyLocationConstants.STOP)
        callMyService(MyLocationConstants.STOP)
        showStart()
    }


    override fun updateUIState() {
        when (sharedPreferences?.getString(MyLocationConstants.STATE, null)) {
            MyLocationConstants.START -> {
                hideStart()
            }

            MyLocationConstants.PAUSE -> {
                hidePause()
            }

            MyLocationConstants.RESUME -> {
                hideResume()
            }

            MyLocationConstants.STOP -> {
                showStart()
            }
        }
    }

    override fun pauseService() {
        setState(MyLocationConstants.PAUSE)
        callMyService(MyLocationConstants.PAUSE)
        hidePause()
    }


    override fun resumeService() {
        setState(MyLocationConstants.RESUME)
        callMyService(MyLocationConstants.RESUME)
        hideResume()
    }

    private fun hidePause() {
        view.hidePause()
        view.showResume()
    }

    private fun hideResume() {
        view.hideResume()
        view.showPause()
    }

    private fun showStart() {
        view.showStart()
        view.hideStop()
        view.hidePause()
        view.hideResume()
        view.hideReset()
    }

    private fun hideStart() {
        view.hideStart()
        view.showStop()
        view.showReset()
        view.showPause()
    }

    override fun setState(state: String) {
        sharedPreferences.edit().putString(MyLocationConstants.STATE, state).apply()
    }

    override fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager =
            context.activity?.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }


}