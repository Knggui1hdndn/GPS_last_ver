package com.example.gps.ui.setting

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Switch
import android.widget.Toast
import com.example.gps.R
import com.example.gps.SettingConstants
import com.example.gps.dao.MyDataBase
import com.example.gps.dao.VehicleDao
import com.example.gps.databinding.ActivitySettingBinding
import com.example.gps.utils.ColorUtils
import com.google.android.material.button.MaterialButton
import java.lang.Exception

class Setting : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var btnMph: MaterialButton
    private lateinit var btnKm: MaterialButton
    private lateinit var btnKnot: MaterialButton
    private lateinit var btnOto: MaterialButton
    private lateinit var btnBicycle: MaterialButton
    private lateinit var btnTrain: MaterialButton
    private lateinit var btnMaxSpeepAnalog: MaterialButton
    private lateinit var swtShowSpeedInNoti: Switch
    private lateinit var swtTrackOnMap: Switch
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
    private lateinit var btnWarningLimit: MaterialButton
    private lateinit var vehicleDao: VehicleDao
    private var colorPosition = 1
    private var checkUnitClick = 0

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


        btnOto.setOnClickListener {
            setBackgroundButtonVehicle(btnOto)
            myDataBase.vehicleDao().updateUnChecked(checkUnitClick)
            myDataBase.vehicleDao().updateVehicle(1, checkUnitClick)
        }
        btnBicycle.setOnClickListener {
            setBackgroundButtonVehicle(btnBicycle)
            myDataBase.vehicleDao().updateUnChecked(checkUnitClick)
            myDataBase.vehicleDao().updateVehicle(1, checkUnitClick)
        }
        btnTrain.setOnClickListener {
            setBackgroundButtonVehicle(btnTrain)
            myDataBase.vehicleDao().updateUnChecked(checkUnitClick)
            myDataBase.vehicleDao().updateVehicle(1, checkUnitClick)
        }
        btnMph.setOnClickListener {
            setBackgroundButtonUnit(btnMph)
            myDataBase.SpeedDao().updateUnChecked()
            myDataBase.SpeedDao().updateChecked(1)
            checkUnitClick = 1
        }
        btnKm.setOnClickListener {
            setBackgroundButtonUnit(btnKm)
            myDataBase.SpeedDao().updateUnChecked()
            myDataBase.SpeedDao().updateChecked(2)
            checkUnitClick = 2
        }
        btnKnot.setOnClickListener {
            setBackgroundButtonUnit(btnKnot)
            myDataBase.SpeedDao().updateUnChecked()
            myDataBase.SpeedDao().updateChecked(3)
            checkUnitClick = 3
        }
        onClickBtnColor(btnColor1, btnColor2, btnColor3, btnColor4, btnColor5, btnColor6, btnColor7)
    }

    private fun setBackgroundButtonVehicle(btnTrain: MaterialButton) {
        removeBackgroundButtonVehicle()
        btnTrain.setBackgroundColor(ColorUtils.checkColor(colorPosition))

    }

    private fun removeBackgroundButtonVehicle() {
        btnBicycle.setBackgroundColor(Color.parseColor("#0d0d0d"))
        btnOto.setBackgroundColor(Color.parseColor("#0d0d0d"))
        btnTrain.setBackgroundColor(Color.parseColor("#0d0d0d"))
    }

    private fun setBackgroundButtonUnit(btn: MaterialButton) {
        removeBackgroundButtonUnit()
        btn.setBackgroundColor(ColorUtils.checkColor(colorPosition))
    }

    private fun setBackgroundALL() {
        btnWarningLimit.setTextColor(ColorUtils.checkColor(colorPosition))
        btnMaxSpeepAnalog.setTextColor(ColorUtils.checkColor(colorPosition))
        setBackgroundSpeedWhenOnCreate()
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

    private fun setBackgroundSpeedWhenOnCreate() {
        val type = myDataBase.SpeedDao().getChecked().type

        when (type) {
            1 -> {
                btnMph.setBackgroundColor(ColorUtils.checkColor(colorPosition))
                setBackgroundVehicleWhenOnCreate(1)
            }

            2 -> {
                btnKm.setBackgroundColor(ColorUtils.checkColor(colorPosition))
                setBackgroundVehicleWhenOnCreate(2)

            }

            3 -> {
                btnKnot.setBackgroundColor(ColorUtils.checkColor(colorPosition))
                setBackgroundVehicleWhenOnCreate(3)
            }
        }
    }

    private fun setBackgroundVehicleWhenOnCreate(typeId: Int) {
        val x = myDataBase.vehicleDao().getVehicleChecked(typeId)
        when (x.type) {
            1 -> {
                btnOto.setBackgroundColor(ColorUtils.checkColor(colorPosition))
            }

            2 -> {
                btnBicycle.setBackgroundColor(ColorUtils.checkColor(colorPosition))
            }

            3 -> {
                btnTrain.setBackgroundColor(ColorUtils.checkColor(colorPosition))
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
            this@Setting.swtShowSpeedInNoti = this.swtShowSpeedInNoti
            this@Setting.swtAlarm = this.swtAlarm
            this@Setting.swtShowReset = this.swtShowReset
            this@Setting.swtTrackOnMap = this.swtTrackOnMap
            this@Setting.swtClockDisplay = this.swtClockDisplay
            this@Setting.btnKnot = this.btnKnot
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