package com.example.gps.ui

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.gps.R
import com.example.gps.constants.SettingConstants
import com.example.gps.dao.MyDataBase
import com.example.gps.databinding.ActivitySettingOptionsBinding
import com.example.gps.`object`.SharedData
import com.google.android.material.button.MaterialButton

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
        getDialog().show()

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

                            putInt(SettingConstants.COLOR_DISPLAY, 2)
                            putBoolean(SettingConstants.DISPLAY_SPEED, true)
                            putBoolean(SettingConstants.TRACK_ON_MAP, true)
                            putBoolean(SettingConstants.SHOW_RESET_BUTTON, true)
                            putBoolean(SettingConstants.SPEED_ALARM, true)
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
                        startActivity(Intent(this@SettingOptionsActivitys, TipActivity::class.java))
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

    private fun getDialog(): Dialog {
        dialog = Dialog(this).apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent);
            setContentView(R.layout.dialog_show_rq)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        }
        val onGPS = dialog.findViewById<MaterialButton>(R.id.btnOnGPS)
        onGPS.setOnClickListener {
            rq.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        return dialog
    }

    private val rq =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            dialog.cancel()
            binding.mLinear.visibility = View.VISIBLE
        }

    fun setBackGroundBtnClick(btn: MaterialButton) {
        btn.strokeWidth = 5
        btn.setTextColor(Color.WHITE)
    }

    fun clickVehicle(vararg btn: MaterialButton) {
        btn.forEach {
            it.setOnClickListener {
                setDefaultVehicle()
                when (it.id) {
                    R.id.btnBicycle -> {
                        vehicleClick = 1
                        setBackGroundBtnClick(binding.btnBicycle)
                    }

                    R.id.btnCar -> {
                        vehicleClick = 3
                        setBackGroundBtnClick(binding.btnCar)

                    }

                    R.id.btnMotorbike -> {
                        vehicleClick = 2
                        setBackGroundBtnClick(binding.btnMotorbike)

                    }
                }
            }
        }
    }

    fun clickUnit(vararg btn: MaterialButton) {

        btn.forEach {
            it.setOnClickListener {
                setDefaultUnit()
                when (it.id) {
                    R.id.btnKm -> {
                        unitClick = "km/h"
                        setBackGroundBtnClick(binding.btnKm)
                    }

                    R.id.btnKnot -> {
                        unitClick = "knot"
                        setBackGroundBtnClick(binding.btnKnot)
                    }

                    R.id.btnMph -> {
                        unitClick = "mph"
                        setBackGroundBtnClick(binding.btnMph)
                    }
                }
                binding.a.text=unitClick
            }
        }
    }

    fun clickViewMode(vararg btn: MaterialButton) {
        btn.forEach {
            it.setOnClickListener {
                setDefaultViewMode()
                when (it.id) {
                    R.id.btnAnalog -> {
                        viewModeClick = 1
                        setBackGroundBtnClick(binding.btnAnalog)

                    }

                    R.id.btnMap -> {
                        viewModeClick = 3
                        setBackGroundBtnClick(binding.btnMap)

                    }

                    R.id.btnDigital -> {
                        viewModeClick = 2
                        setBackGroundBtnClick(binding.btnDigital)

                    }
                }
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