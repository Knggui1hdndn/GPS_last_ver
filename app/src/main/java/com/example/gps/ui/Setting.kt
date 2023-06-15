package com.example.gps.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.Service
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.gp.NotificationsFragment
import com.example.gps.R
import com.example.gps.constants.MyLocationConstants
import com.example.gps.constants.SettingConstants
import com.example.gps.dao.MyDataBase
import com.example.gps.dao.VehicleDao
import com.example.gps.databinding.ActivitySettingBinding
import com.example.gps.`object`.SharedData
import com.example.gps.utils.ColorUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.internal.ContextUtils.getActivity


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
    private lateinit var txtDistance: TextView
    private lateinit var edtWarningLimit: EditText
    private lateinit var vehicleDao: VehicleDao
    private var colorPosition = 1
    private var checkUnitClick = 0
    private var checkVehicleClick = 0
    private lateinit var myDataBase: MyDataBase

    val fragmentManager = (SharedData.activity as MainActivity2).supportFragmentManager
    private lateinit var notificationsFragment: NotificationsFragment
    private lateinit var homeFragment: HomeFragment
    private lateinit var dashboardFragment: DashboardFragment


    var color = Color.BLACK
    private var distance: Int = 0

    @SuppressLint("CommitPrefEdits", "SetTextI18n", "RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        try {
            homeFragment = fragmentManager.findFragmentByTag("f0") as HomeFragment
            dashboardFragment = fragmentManager.findFragmentByTag("f1") as DashboardFragment
            notificationsFragment = fragmentManager.findFragmentByTag("f2") as NotificationsFragment
        } catch (e: java.lang.Exception) {
        }
        if (!ColorUtils.isThemeDark()) {
            btnResetDistance.iconTint = ColorStateList.valueOf(Color.BLACK)
            color = Color.WHITE
            binding.mToolBar.setTitleTextColor(Color.BLACK)
        } else {
            binding.mToolBar.setTitleTextColor(Color.WHITE)
        }
        setSupportActionBar(binding.mToolBar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        sharedPreferences = getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE)
        myDataBase = MyDataBase.getInstance(this)
        vehicleDao = myDataBase.vehicleDao()
        colorPosition = sharedPreferences.getInt(SettingConstants.COLOR_DISPLAY, 0)

         edtWarningLimit.setOnKeyListener(object : DialogInterface.OnKeyListener,
            View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (event != null) {
                    if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                        try {
                            val url = edtWarningLimit.text.toString()
                            if (url.toInt() >= 0 && url.isNotEmpty()) {
                                myDataBase.vehicleDao().updateWarning(url.toInt())
                                edtWarningLimit.setText(url)
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@Setting,
                                "Tốc độ không để trống",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return true
                    }
                }
                return false
            }

            override fun onKey(dialog: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean {
                return false
            }
        })

        distance = getSharedPreferences("state", Service.MODE_PRIVATE).getInt(
            MyLocationConstants.DISTANCE,
            0
        )
        setBackgroundALL()
        swtAlarm.setOnCheckedChangeListener { _, isChecked ->
            saveSettingBoolean(SettingConstants.SPEED_ALARM, isChecked, swtAlarm)
        }

        swtShowReset.setOnCheckedChangeListener { _, isChecked ->
            toggleShowReset()
        }

        swtClockDisplay.setOnCheckedChangeListener { _, isChecked ->
            toggleClockVisibilityLandscape()
        }

        swtTrackOnMap.setOnCheckedChangeListener { _, isChecked ->
            saveSettingBoolean(SettingConstants.TRACK_ON_MAP, isChecked, swtTrackOnMap)
            toggleTrackOnMap()
        }

        swtShowSpeedInNoti.setOnCheckedChangeListener { _, isChecked ->
            saveSettingBoolean(SettingConstants.DISPLAY_SPEED, isChecked, swtShowSpeedInNoti)
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
            getSharedPreferences("state", Service.MODE_PRIVATE).edit()
                .putInt(MyLocationConstants.DISTANCE, 0).apply()
        }


        btnOto.setOnClickListener {
            updateVehicle(1)
        }

        btnBicycle.setOnClickListener {
            updateVehicle(2)
        }

        btnTrain.setOnClickListener {
            updateVehicle(3)
        }

        btnMph.setOnClickListener {
            updateSpeedUnit("mph", "mi")
        }

        btnKm.setOnClickListener {
            updateSpeedUnit("km/h", "km")
        }
        btnKnot.setOnClickListener {
            updateSpeedUnit("knot", "nm")
        }
        when(sharedPreferences.getBoolean(SettingConstants.THEME,true)){
            true->binding.night.setBackgroundColor(ColorUtils.checkColor(colorPosition))
            false->binding.dark.setBackgroundColor(ColorUtils.checkColor(colorPosition))

        }

        with(binding) {
            dark.setOnClickListener {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                sharedPreferences.edit().putBoolean(SettingConstants.THEME,false).apply()
                dark.setBackgroundColor(ColorUtils.checkColor(colorPosition))
                night.setBackgroundColor(color)

            }
            night.setOnClickListener {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                sharedPreferences.edit().putBoolean(SettingConstants.THEME,true).apply()
                night.setBackgroundColor(ColorUtils.checkColor(colorPosition))
                dark.setBackgroundColor(color)

            }
            btnAnalog.setOnClickListener {
                removeBackGroundViewMode()
                btnAnalog.setBackgroundColor(ColorUtils.checkColor(colorPosition))
                sharedPreferences.edit().putInt(SettingConstants.ViEW_MODE,1).apply()

            }
            btnMap.setOnClickListener {
                removeBackGroundViewMode()
                btnMap.setBackgroundColor(ColorUtils.checkColor(colorPosition))
                sharedPreferences.edit().putInt(SettingConstants.ViEW_MODE,3).apply()

            }
            btnDigital.setOnClickListener {
                removeBackGroundViewMode()
                btnDigital.setBackgroundColor(ColorUtils.checkColor(colorPosition))
                sharedPreferences.edit().putInt(SettingConstants.ViEW_MODE,2).apply()

            }
        }
        onClickBtnColor(btnColor2, btnColor3, btnColor4, btnColor5, btnColor6, btnColor7)
     }

    private fun removeBackGroundViewMode() {
        with(binding) {
            btnAnalog.setBackgroundColor(color)
            btnMap.setBackgroundColor(color)
            btnDigital.setBackgroundColor(color)
        }
    }

    private fun toggleShowReset() {
        saveSettingBoolean(SettingConstants.SHOW_RESET_BUTTON, swtShowReset.isChecked, swtShowReset)

    }

    private fun toggleClockVisibilityLandscape() {
        saveSettingBoolean(
            SettingConstants.CLOCK_DISPLAY,
            swtClockDisplay.isChecked,
            swtClockDisplay
        )
    }

    private fun saveSettingBoolean(key: String, value: Boolean, sw: Switch) {
        sw.trackTintList =
            if (sw.isChecked) ColorStateList.valueOf(ColorUtils.checkColor(colorPosition)) else ColorStateList.valueOf(
                Color.GRAY
            )
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    private fun updateVehicle(checkVehicleClick: Int) {
        myDataBase.vehicleDao().updateUnChecked()
        myDataBase.vehicleDao().updateVehicle(checkVehicleClick)
        this.checkVehicleClick = checkVehicleClick
        setSpecifications()
        removeBackgroundButtonVehicle()
        setBackGroundButtonVehicleClick()
        registerReceiverUnitFromFragmentM2()
    }


    @SuppressLint("CommitPrefEdits")
    private fun updateSpeedUnit(speedUnit: String, distanceUnit: String) {
        sharedPreferences.edit().putString(SettingConstants.UNIT, speedUnit)
        SharedData.toUnitDistance = distanceUnit
        SharedData.toUnit = speedUnit
        registerReceiverUnitFromFragmentM2()
        removeBackgroundButtonUnit()
        setBackGroundButtonUnitClick()
        setBackGroundButtonViewMode()
    }

    private fun setBackGroundButtonUnitClick() {
        when (SharedData.toUnit) {
            "km/h" -> {
                btnKm.setBackgroundColor(ColorUtils.checkColor(colorPosition)); }

            "mph" -> {
                btnMph.setBackgroundColor(ColorUtils.checkColor(colorPosition));
            }

            "knot" -> {
                btnKnot.setBackgroundColor(ColorUtils.checkColor(colorPosition));
            }
        }
    }

    private fun setBackGroundButtonViewMode() {
        val i = sharedPreferences.getInt(SettingConstants.ViEW_MODE, 0)
         when (i) {
            1 -> {
                binding.btnAnalog.setBackgroundColor(ColorUtils.checkColor(colorPosition));
            }

            2 -> {
                binding.btnDigital.setBackgroundColor(ColorUtils.checkColor(colorPosition));
            }

            3 -> {
                binding.btnMap.setBackgroundColor(ColorUtils.checkColor(colorPosition));
            }
        }
    }

    private fun setBackGroundButtonVehicleClick() {

        when (myDataBase.vehicleDao().getVehicleChecked().type) {
            1 -> {
                btnOto.setBackgroundColor(ColorUtils.checkColor(colorPosition));
            }

            2 -> {
                btnBicycle.setBackgroundColor(ColorUtils.checkColor(colorPosition));
            }

            3 -> {
                btnTrain.setBackgroundColor(ColorUtils.checkColor(colorPosition));
            }
        }
    }


    private fun registerReceiverUnitFromFragmentM2() {
        SharedData.speedAnalog.value = SharedData.speedAnalog.value
        SharedData.locationLiveData.value = SharedData.locationLiveData.value
        SharedData.averageSpeedLiveData.value = SharedData.averageSpeedLiveData.value
        SharedData.distanceLiveData.value = SharedData.distanceLiveData.value
        SharedData.maxSpeedLiveData.value = SharedData.maxSpeedLiveData.value
        SharedData.currentSpeedLiveData.value = SharedData.currentSpeedLiveData.value

    }


    private fun toggleTrackOnMap() {
        if (this::notificationsFragment.isInitialized) notificationsFragment.clearMap(swtTrackOnMap.isChecked)
    }

    private fun getDialogSpeedAnalog(): AlertDialog.Builder {
        var item = 0
        val array = arrayOf(
            "80",
            "160",
            "240",
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
            ) { _, which ->
                item = array[which].toInt()
            }
            setNegativeButton("Hủy Bỏ") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            setPositiveButton("Đồng Ý") { dialog: DialogInterface, _: Int ->
                SharedData.speedAnalog.value = item
                myDataBase.vehicleDao().updateMaxSpeed(item)
                btnMaxSpeepAnalog.text = item.toString()
                registerReceiverUnitFromFragmentM2()
                dialog.cancel()
                registerReceiverUnitFromFragmentM2()
            }
        }
    }

    private fun setSpecifications() {
        val vehicleChecked = myDataBase.vehicleDao().getVehicleChecked()
        btnMaxSpeepAnalog.text = vehicleChecked.clockSpeed.toString()
        edtWarningLimit.setText(vehicleChecked.limitWarning.toString())
        SharedData.speedAnalog.value = vehicleChecked.clockSpeed
        txtDistance.text = SharedData.convertDistance(distance.toDouble()).toInt().toString()
    }


    private fun removeBackgroundButtonVehicle() {
        btnBicycle.setBackgroundColor(color)
        btnOto.setBackgroundColor(color)
        btnTrain.setBackgroundColor(color)

    }


    private fun setBackgroundALL() {
        edtWarningLimit.setTextColor(ColorUtils.checkColor(colorPosition))
        btnMaxSpeepAnalog.setTextColor(ColorUtils.checkColor(colorPosition))
        setColorSwt()
        setSpecifications()
        setBackGroundButtonUnitClick()
        setBackGroundButtonVehicleClick()
        setBackGroundButtonViewMode()

    }

    private fun removeBackgroundButtonUnit() {
        btnKm.setBackgroundColor(color)
        btnMph.setBackgroundColor(color)
        btnKnot.setBackgroundColor(color)

    }


    private fun setColorSwt() {
        checkStateSwitch(swtAlarm, SettingConstants.SPEED_ALARM)
        checkStateSwitch(swtClockDisplay, SettingConstants.CLOCK_DISPLAY)
        checkStateSwitch(swtShowSpeedInNoti, SettingConstants.DISPLAY_SPEED)
        checkStateSwitch(swtShowReset, SettingConstants.SHOW_RESET_BUTTON)
        checkStateSwitch(swtTrackOnMap, SettingConstants.TRACK_ON_MAP)
    }


    private fun onClickBtnColor(vararg btnColor: MaterialButton) {
        btnColor.forEach {
            it.setOnClickListener {
                when (it.id) {


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
                SharedData.color.value = colorPosition
                saveColorChecked()
                removeBackgroundButtonUnit()
                removeBackgroundButtonVehicle()
                removeBackGroundViewMode()
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
        sw.trackTintList =
            if (sw.isChecked) ColorStateList.valueOf(ColorUtils.checkColor(colorPosition)) else ColorStateList.valueOf(
                Color.GRAY
            )
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
            this@Setting.btnMph = this.btnMph
            this@Setting.btnKm = this.btnKm
            this@Setting.btnMph = this.btnMph
            this@Setting.btnBicycle = this.btnBicycle
            this@Setting.btnOto = this.btnOto
            this@Setting.btnTrain = this.btnTrain
            this@Setting.btnColor2 = this.btnColor2
            this@Setting.btnColor3 = this.btnColor3
            this@Setting.btnColor4 = this.btnColor4
            this@Setting.btnColor5 = this.btnColor5
            this@Setting.btnColor6 = this.btnColor6
            this@Setting.btnColor7 = this.btnColor7
            this@Setting.edtWarningLimit = this.edtWarningLimit
            this@Setting.btnResetDistance = this.btnResetDistance
            this@Setting.btnMaxSpeepAnalog = this.btnMaxSpeepAnalog
        }
    }
}