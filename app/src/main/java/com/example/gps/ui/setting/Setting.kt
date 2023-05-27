package com.example.gps.ui.setting

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Switch
import com.example.gps.R
import com.example.gps.SettingConstants
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
    private lateinit var btnBicycle: MaterialButton
    private lateinit var btnTrain: MaterialButton
    private lateinit var btnMaxSpeepAnalog: MaterialButton
    private lateinit var swtShowSpeedInNoti: Switch
    private lateinit var swtTrackOnMap: Switch
    private lateinit var swtAlarm: Switch
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
    private var checkedOnclick = 0
    private var check = 0
   private lateinit var   myDataBase  :MyDataBase

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
        val edit = sharedPreferences.edit()
        vehicleDao = MyDataBase.getInstance(this).vehicleDao()
          myDataBase = MyDataBase.getInstance(this)
        setBackgroundWhenOncrearte()



         checkStateSwitch(swtAlarm, SettingConstants.SPEED_ALARM)
        checkStateSwitch(swtClockDisplay, SettingConstants.CLOCK_DISPLAY)
        checkStateSwitch(swtShowSpeedInNoti, SettingConstants.DISPLAY_SPEED)
        checkStateSwitch(swtShowReset, SettingConstants.SHOW_RESET_BUTTON)
        checkStateSwitch(swtTrackOnMap, SettingConstants.TRACK_ON_MAP)
        btnOto.setOnClickListener {
            checkedOnclick = 4
            checkAndSetBackground()

        }
        btnBicycle.setOnClickListener {
            checkedOnclick = 5
            checkAndSetBackground()

        }
        btnTrain.setOnClickListener {
            checkedOnclick = 6
            checkAndSetBackground()

        }
        btnMph.setOnClickListener {
            checkedOnclick = 1
            checkAndSetBackground()
            myDataBase.SpeedDao().updateUnChecked(1)
            myDataBase.SpeedDao().updateChecked(1)
        }

        btnKm.setOnClickListener {
            checkedOnclick = 2
            checkAndSetBackground()
            myDataBase.SpeedDao().updateUnChecked(1)
            myDataBase.SpeedDao().updateChecked(1)
        }

        btnKnot.setOnClickListener {
            checkedOnclick = 3
            checkAndSetBackground()
            myDataBase.SpeedDao().updateUnChecked(1)
            myDataBase.SpeedDao().updateChecked(1)
        }
        onClickBtnColor(btnColor1, btnColor2, btnColor3, btnColor4, btnColor5, btnColor6, btnColor7)
    }

    private fun setBackgroundSpeedWhenOnCreate() {
        val type=myDataBase.SpeedDao().getChecked().type
        when(type){
            1 -> {
                btnMph.setBackgroundColor(ColorUtils.checkColor(colorPosition))
                setBackgroundVehicleWhenOnCreate(type)
            }
            2 -> {
                btnMph.setBackgroundColor(ColorUtils.checkColor(colorPosition))
                setBackgroundVehicleWhenOnCreate(type)

            }
            3 -> {
                btnMph.setBackgroundColor(ColorUtils.checkColor(colorPosition))
                setBackgroundVehicleWhenOnCreate(type)

            }


        }
    }

    private fun setBackgroundVehicleWhenOnCreate(typeId:Int) {
        when(myDataBase.vehicleDao().getVehicleChecked(typeId).type){

            2 -> {
                btnBicycle.setBackgroundColor(ColorUtils.checkColor(colorPosition))
            }
            3 -> {
                btnTrain.setBackgroundColor(ColorUtils.checkColor(colorPosition))
            }
        }
     }



    private fun setBackgroundWhenOncrearte() {
        setBackgroundSpeedWhenOnCreate()
    }

    private fun checkAndSetBackground() {
        btnWarningLimit.setTextColor(ColorUtils.checkColor(colorPosition))
        btnMaxSpeepAnalog.setTextColor(ColorUtils.checkColor(colorPosition))
        when (checkedOnclick) {
            1 -> {
                btnKm.setBackgroundColor(Color.parseColor("#0d0d0d"))
                btnMph.setBackgroundColor(ColorUtils.checkColor(colorPosition))
                btnKnot.setBackgroundColor(Color.parseColor("#0d0d0d"))
            }

            2 -> {
                btnMph.setBackgroundColor(Color.parseColor("#0d0d0d"))
                btnKm.setBackgroundColor(ColorUtils.checkColor(colorPosition))
                btnKnot.setBackgroundColor(Color.parseColor("#0d0d0d"))
            }

            3 -> {
                btnMph.setBackgroundColor(Color.parseColor("#0d0d0d"))
                btnKm.setBackgroundColor(Color.parseColor("#0d0d0d"))
                btnKnot.setBackgroundColor(ColorUtils.checkColor(colorPosition))
            }

            4 -> {
                btnBicycle.setBackgroundColor(Color.parseColor("#0d0d0d"))
                btnOto.setBackgroundColor(ColorUtils.checkColor(colorPosition))
                btnTrain.setBackgroundColor(Color.parseColor("#0d0d0d"))
            }

            5 -> {
                btnTrain.setBackgroundColor(Color.parseColor("#0d0d0d"))
                btnBicycle.setBackgroundColor(ColorUtils.checkColor(colorPosition))
                btnOto.setBackgroundColor(Color.parseColor("#0d0d0d"))
            }

            6 -> {
                btnBicycle.setBackgroundColor(Color.parseColor("#0d0d0d"))
                btnTrain.setBackgroundColor(ColorUtils.checkColor(colorPosition))
                btnOto.setBackgroundColor(Color.parseColor("#0d0d0d"))
            }


        }


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
                checkAndSetBackground()

            }
        }
    }

    private fun saveColorChecked() =
        sharedPreferences.edit().putInt(SettingConstants.COLOR_DISPLAY, colorPosition)

    private fun checkStateSwitch(
        @SuppressLint("UseSwitchCompatOrMaterialCode") sw: Switch,
        constants: String
    ) {
        sw.isChecked = sharedPreferences.getBoolean(constants, false)
    }

    private fun initView() {
        with(binding)
        {
            this@Setting.swtShowSpeedInNoti = this.swtShowSpeedInNoti
            this@Setting.swtAlarm = this.swtAlarm
            this@Setting.swtShowReset = this.swtShowReset!!
            this@Setting.swtTrackOnMap = this.swtTrackOnMap
            this@Setting.swtClockDisplay = this.swtClockDisplay!!
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