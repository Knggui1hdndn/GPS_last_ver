package com.example.gps.ui.setting

import android.annotation.SuppressLint
import android.app.Service
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.gps.MyLocationConstants
import com.example.gps.R
import com.example.gps.SettingConstants
import com.example.gps.SharedData
import com.example.gps.dao.MyDataBase
import com.example.gps.dao.VehicleDao
import com.example.gps.databinding.ActivitySettingBinding
import com.example.gps.utils.ColorUtils
import com.google.android.material.button.MaterialButton

class Setting : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var btnMph: MaterialButton
    private lateinit var btnKm: MaterialButton
    private lateinit var btnKnot: MaterialButton
    private lateinit var btnOto: MaterialButton
    private lateinit var btnLetGo: Button
    private lateinit var btnBicycle: MaterialButton
    private lateinit var btnTrain: MaterialButton
    private lateinit var btnMaxSpeepAnalog: MaterialButton
    private lateinit var swtShowSpeedInNoti: Switch
    private lateinit var swtTrackOnMap: Switch

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var swtAlarm: Switch

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var swtClockDisplay: Switch
    private lateinit var swtShowReset: Switch
    private lateinit var btnColor1: MaterialButton
    private lateinit var btnColor2: MaterialButton
    private lateinit var btnColor3: MaterialButton
    private lateinit var btnColor4: MaterialButton
    private lateinit var btnColor5: MaterialButton
    private lateinit var btnColor6: MaterialButton
    private lateinit var btnColor7: MaterialButton
    private lateinit var btnResetDistance: MaterialButton
    private lateinit var txtDistance: TextView
    private lateinit var btnWarningLimit: MaterialButton
    private lateinit var vehicleDao: VehicleDao
    private var colorPosition = 1
    private var checkUnitClick = 0
    private var checkVehicleClick = 0

    private lateinit var myDataBase: MyDataBase

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        setSupportActionBar(binding.mToolBar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        sharedPreferences = getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE)
        vehicleDao = MyDataBase.getInstance(this).vehicleDao()
        myDataBase = MyDataBase.getInstance(this)
        checkUnitClick = myDataBase.SpeedDao().getChecked().type
        colorPosition = sharedPreferences.getInt(SettingConstants.COLOR_DISPLAY, 0)
        setBackgroundALL()
        txtDistance.text = getSharedPreferences("state", Service.MODE_PRIVATE).getInt(
            MyLocationConstants.DISTANCE,
            0
        ).toString()
        swtAlarm.setOnCheckedChangeListener { buttonView, isChecked ->
            sharedPreferences.edit().putBoolean(SettingConstants.SPEED_ALARM, isChecked).apply()
        }
        swtShowReset.setOnCheckedChangeListener { buttonView, isChecked ->
            sharedPreferences.edit().putBoolean(SettingConstants.SHOW_RESET_BUTTON, isChecked)
                .apply()
        }
        swtClockDisplay.setOnCheckedChangeListener { buttonView, isChecked ->
            sharedPreferences.edit().putBoolean(SettingConstants.CLOCK_DISPLAY, isChecked).apply()
        }
        swtTrackOnMap.setOnCheckedChangeListener { buttonView, isChecked ->
            sharedPreferences.edit().putBoolean(SettingConstants.TRACK_ON_MAP, isChecked).apply()
        }
        swtShowSpeedInNoti.setOnCheckedChangeListener { buttonView, isChecked ->
            sharedPreferences.edit().putBoolean(SettingConstants.DISPLAY_SPEED, isChecked).apply()
        }
        btnMaxSpeepAnalog.setOnClickListener {
            getDialogSpeedAnalog().show()
        }
        btnResetDistance.setOnClickListener {
            when (checkUnitClick) {
                1 -> txtDistance.text = "000000 Mph"
                2 -> txtDistance.text = "000000 Km/h"
                3 -> txtDistance.text = "000000 Knot"
            }
            getSharedPreferences("state", Service.MODE_PRIVATE).edit().putInt(
                MyLocationConstants.DISTANCE, 0
            ).apply()
        }
        btnLetGo.setOnClickListener {
            finish()
        }
        btnOto.setOnClickListener {
            myDataBase.vehicleDao().updateUnChecked(checkUnitClick)
            myDataBase.vehicleDao().updateVehicle(checkUnitClick, 1)
            setBackgroundSpeedAndVehicle()
            checkVehicleClick = 1
        }
        btnBicycle.setOnClickListener {
            myDataBase.vehicleDao().updateUnChecked(checkUnitClick)
            myDataBase.vehicleDao().updateVehicle(checkUnitClick, 2)
            setBackgroundSpeedAndVehicle()
            checkVehicleClick = 2
        }
        btnTrain.setOnClickListener {
            myDataBase.vehicleDao().updateUnChecked(checkUnitClick)
            myDataBase.vehicleDao().updateVehicle(checkUnitClick, 3)
            setBackgroundSpeedAndVehicle()
            checkVehicleClick = 3
        }
        btnMph.setOnClickListener {
            SharedData.unitSpeed.value = "mph"
            myDataBase.SpeedDao().updateUnChecked()
            myDataBase.SpeedDao().updateChecked(1)
            setBackgroundSpeedAndVehicle()
            checkUnitClick = 1

        }
        btnKm.setOnClickListener {
            SharedData.unitSpeed.value = "km/h"
            myDataBase.SpeedDao().updateUnChecked()
            myDataBase.SpeedDao().updateChecked(2)
            setBackgroundSpeedAndVehicle()
            checkUnitClick = 2
        }
        btnKnot.setOnClickListener {
            SharedData.unitSpeed.value = "knot"
            myDataBase.SpeedDao().updateUnChecked()
            myDataBase.SpeedDao().updateChecked(3)
            setBackgroundSpeedAndVehicle()
            checkUnitClick = 3
        }
        onClickBtnColor(btnColor1, btnColor2, btnColor3, btnColor4, btnColor5, btnColor6, btnColor7)
    }

    private fun getDialogSpeedAnalog(): AlertDialog.Builder {
        var item = 0
        val array = arrayOf(
            "320",
            "400",
            "480",
            "560",
            "640"
        )
        val position: Int = array.indexOf(btnMaxSpeepAnalog.text.toString())
        return AlertDialog.Builder(this@Setting).apply {
            setTitle("Chọn Tốc độ Đông hồ Analog").setSingleChoiceItems(
                array, position
            ) { dialog, which ->
                item = array[which].toInt()

            }
            setNegativeButton("Hủy Bỏ") { dialog: DialogInterface, i: Int ->
                dialog.dismiss()
            }
            setPositiveButton("Đồng Ý") { dialog: DialogInterface, i: Int ->
                Log.d("áaaaaa", "$checkUnitClick xx $checkVehicleClick")
                myDataBase.vehicleDao().updateMaxSpeed(checkUnitClick, checkVehicleClick, item)
                btnMaxSpeepAnalog.text = item.toString()
                dialog.cancel()
            }
        }
    }

    private fun setSpecifications(typeId: Int) {
        val vehicleChecked = myDataBase.vehicleDao().getVehicleChecked(typeId)
        btnMaxSpeepAnalog.text = vehicleChecked.clockSpeed.toString()
        btnWarningLimit.text = vehicleChecked.limitWarning.toString()
        Log.d("áaaaaa", "setSpecifications" + vehicleChecked)
    }


    private fun removeBackgroundButtonVehicle() {
        btnBicycle.setBackgroundColor(Color.parseColor("#0d0d0d"))
        btnOto.setBackgroundColor(Color.parseColor("#0d0d0d"))
        btnTrain.setBackgroundColor(Color.parseColor("#0d0d0d"))
    }


    private fun setBackgroundALL() {
        btnLetGo.setBackgroundColor(
            ColorUtils.checkColor(colorPosition)
        )
        btnWarningLimit.setTextColor(ColorUtils.checkColor(colorPosition))
        btnMaxSpeepAnalog.setTextColor(ColorUtils.checkColor(colorPosition))
        setBackgroundSpeedAndVehicle()
        setColorSwt()
    }

    private fun removeBackgroundButtonUnit() {

        btnKm.setBackgroundColor(Color.parseColor("#0d0d0d"))
        btnMph.setBackgroundColor(Color.parseColor("#0d0d0d"))
        btnKnot.setBackgroundColor(Color.parseColor("#0d0d0d"))
    }

    private fun setColorSwt() {
        checkStateSwitch(swtAlarm, SettingConstants.SPEED_ALARM)
        checkStateSwitch(swtClockDisplay, SettingConstants.CLOCK_DISPLAY)
        checkStateSwitch(swtShowSpeedInNoti, SettingConstants.DISPLAY_SPEED)
        checkStateSwitch(swtShowReset, SettingConstants.SHOW_RESET_BUTTON)
        checkStateSwitch(swtTrackOnMap, SettingConstants.TRACK_ON_MAP)
    }

    private fun setBackgroundSpeedAndVehicle() {
        val type = myDataBase.SpeedDao().getChecked().type
        removeBackgroundButtonUnit()
        when (type) {
            1 -> {
                btnMph.setBackgroundColor(ColorUtils.checkColor(colorPosition))
                setBackgroundVehicleWhenOnCreate(1)
                setSpecifications(1)

            }

            2 -> {
                btnKm.setBackgroundColor(ColorUtils.checkColor(colorPosition))
                setBackgroundVehicleWhenOnCreate(2)
                setSpecifications(2)
            }

            3 -> {
                btnKnot.setBackgroundColor(ColorUtils.checkColor(colorPosition))
                setBackgroundVehicleWhenOnCreate(3)
                setSpecifications(3)
            }
        }
    }

    private fun setBackgroundVehicleWhenOnCreate(typeId: Int) {
        removeBackgroundButtonVehicle()
        val x = myDataBase.vehicleDao().getVehicleChecked(typeId)
        when (x.type) {
            1 -> {
                btnOto.setBackgroundColor(ColorUtils.checkColor(colorPosition))
                checkVehicleClick = 1
            }

            2 -> {
                btnBicycle.setBackgroundColor(ColorUtils.checkColor(colorPosition))
                checkVehicleClick = 2
            }

            3 -> {
                btnTrain.setBackgroundColor(ColorUtils.checkColor(colorPosition))
                checkVehicleClick = 3
            }
        }
        Log.d("lslsl", myDataBase.vehicleDao().getVehicleChecked(1).type.toString())
    }


    private fun onClickBtnColor(vararg btnColor: MaterialButton) {
        btnColor.forEach {
            it.setOnClickListener {
                when (it.id) {
                    R.id.btnColor1 -> {
                        colorPosition = 1
                    }

                    R.id.btnColor2 -> {
                        colorPosition = 2
                    }

                    R.id.btnColor3 -> {
                        colorPosition = 3
                    }

                    R.id.btnColor4 -> {
                        colorPosition = 4
                    }

                    R.id.btnColor5 -> {
                        colorPosition = 5
                    }

                    R.id.btnColor6 -> {
                        colorPosition = 6

                    }

                    R.id.btnColor7 -> {
                        colorPosition = 7

                    }

                }
                saveColorChecked()
                removeBackgroundButtonUnit()
                removeBackgroundButtonVehicle()
                setBackgroundALL()

            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return true
    }

    private fun saveColorChecked() =
        sharedPreferences.edit().putInt(SettingConstants.COLOR_DISPLAY, colorPosition).apply()

    private fun checkStateSwitch(
        @SuppressLint("UseSwitchCompatOrMaterialCode") sw: Switch,
        constants: String
    ) {
        sw.isChecked = sharedPreferences.getBoolean(constants, false)
        sw.trackTintList = ColorStateList.valueOf(ColorUtils.checkColor(colorPosition))
    }

    private fun initView() {
        with(binding)
        {
            this@Setting.txtDistance = this.txtDistance
            this@Setting.swtShowSpeedInNoti = this.swtShowSpeedInNoti
            this@Setting.swtAlarm = this.swtAlarm
            this@Setting.swtShowReset = this.swtShowReset
            this@Setting.swtTrackOnMap = this.swtTrackOnMap
            this@Setting.swtClockDisplay = this.swtClockDisplay
            this@Setting.btnKnot = this.btnKnot
            this@Setting.btnLetGo = this.btnLetGo
            this@Setting.btnMph = this.btnMph
            this@Setting.btnKm = this.btnKm
            this@Setting.btnMph = this.btnMph
            this@Setting.btnBicycle = this.btnBicycle
            this@Setting.btnOto = this.btnOto
            this@Setting.btnTrain = this.btnTrain
            this@Setting.btnColor1 = this.btnColor1
            this@Setting.btnColor2 = this.btnColor2
            this@Setting.btnColor3 = this.btnColor3
            this@Setting.btnColor4 = this.btnColor4
            this@Setting.btnColor5 = this.btnColor5
            this@Setting.btnColor6 = this.btnColor6
            this@Setting.btnColor7 = this.btnColor7
            this@Setting.btnWarningLimit = this.btnWarningLimit
            this@Setting.btnResetDistance = this.btnResetDistance
            this@Setting.btnMaxSpeepAnalog = this.btnMaxSpeepAnalog
        }
    }
}