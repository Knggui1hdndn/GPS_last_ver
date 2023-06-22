package com.gps.speedometer.odometer.gpsspeedtracker.ui

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
 import com.gps.speedometer.odometer.gpsspeedtracker.constants.SettingConstants
import com.gps.speedometer.odometer.gpsspeedtracker.dao.MyDataBase
 import com.gps.speedometer.odometer.gpsspeedtracker.`object`.SharedData
import com.google.android.material.button.MaterialButton
import com.gps.speedometer.odometer.gpsspeedtracker.R
import com.gps.speedometer.odometer.gpsspeedtracker.databinding.ActivitySettingOptionsBinding

class SettingOptionsActivitys : AppCompatActivity() {
    private lateinit var binding: ActivitySettingOptionsBinding
    private lateinit var myDataBase: MyDataBase
    private var vehicleClick = 2
    private var unitClick = "km/h"
    private var viewModeClick = 2
    private lateinit var dialog: Dialog
    private fun setUnitSpeedAndDistance() {
        try {
            SharedData.toUnit =
                getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE).getString(
                    SettingConstants.UNIT,
                    ""
                ).toString()
            when (SharedData.toUnit) {
                "km/h" -> SharedData.toUnitDistance = "km"
                "mph" -> SharedData.toUnitDistance = "mi"
                "knot" -> SharedData.toUnitDistance = "nm"
            }
        } catch (_: Exception) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE)
        myDataBase = MyDataBase.getInstance(this)

        with(binding) {
            clickVehicle(btnBicycle, btnCar, btnMotorbike)
            clickUnit(btnKm, btnMph, btnKnot)
            clickViewMode(btnMap, btnDigital, btnAnalog)
            setBackGroundBtnClick(btnMotorbike)
            setBackGroundBtnClick(btnDigital)
            setBackGroundBtnClick(btnKm)
            btnOK.setOnClickListener {
                if(edtSpeedLimit.text.isNotEmpty()){
                    if (edtSpeedLimit.text.toString().toInt() >= 0
                    ) {
                        sharedPreferences.edit().apply {
                            putString(SettingConstants.UNIT, unitClick)
                            putInt(SettingConstants.ViEW_MODE, viewModeClick)
                            putInt(SettingConstants.COLOR_DISPLAY, 0)
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
                        setUnitSpeedAndDistance()
                        startActivity(Intent(this@SettingOptionsActivitys, MainActivity2::class.java))
                        finish()
                    }else {
                        Toast.makeText(
                            this@SettingOptionsActivitys,
                            ">0",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                 else {
                    Toast.makeText(
                        this@SettingOptionsActivitys,
                        "do not leave blank",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }



    fun setBackGroundBtnClick(btn: MaterialButton) {
        btn.strokeWidth = 5
        btn.setTextColor(Color.WHITE)
    }

    fun clickVehicle(vararg btn: MaterialButton) {
        btn.forEach { button ->
            button.setOnClickListener {
                setDefaultVehicle()
                vehicleClick = when (button.id) {
                    R.id.btnBicycle -> 1
                    R.id.btnMotorbike -> 2
                    R.id.btnCar -> 3
                    else -> 0
                }
                setBackGroundBtnClick(button)
            }
        }
    }

    fun clickUnit(vararg btn: MaterialButton) {
        btn.forEach { button ->
            button.setOnClickListener {
                setDefaultUnit()
                unitClick = when (button.id) {
                    R.id.btnKm -> "km/h"
                    R.id.btnKnot -> "knot"
                    R.id.btnMph -> "mph"
                    else -> ""
                }
                setBackGroundBtnClick(button)
                binding.a.text = unitClick
            }
        }
    }

    fun clickViewMode(vararg btn: MaterialButton) {
        btn.forEach { button ->
            button.setOnClickListener {
                setDefaultViewMode()
                viewModeClick = when (button.id) {
                    R.id.btnAnalog -> 1
                    R.id.btnDigital -> 2
                    R.id.btnMap -> 3
                    else -> 0
                }
                setBackGroundBtnClick(button)
            }
        }
    }



    fun setTextAndStrokeWidthColor(vararg btn: MaterialButton) {
        btn.forEach {
            it.strokeWidth = 0
            it.setTextColor(resources.getColor(R.color.unchanged, null))
        }
    }

    private fun setDefaultVehicle() {
        with(binding) {
            setTextAndStrokeWidthColor(btnBicycle, btnCar, btnMotorbike)
        }
    }

    private fun setDefaultViewMode() {
        with(binding) {
            setTextAndStrokeWidthColor(btnMap, btnAnalog, btnDigital)
        }
    }

    private fun setDefaultUnit() {
        with(binding) {
            setTextAndStrokeWidthColor(btnKm, btnKnot, btnMph)

        }
    }
}