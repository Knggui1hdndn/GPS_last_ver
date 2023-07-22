package com.gps.speedometer.odometer.gpsspeedtracker.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.Service
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.access.pro.config.ConfigModel
import com.gps.speedometer.odometer.gpsspeedtracker.constants.MyLocationConstants
import com.gps.speedometer.odometer.gpsspeedtracker.constants.SettingConstants
import com.gps.speedometer.odometer.gpsspeedtracker.dao.MyDataBase
import com.gps.speedometer.odometer.gpsspeedtracker.dao.VehicleDao

import com.gps.speedometer.odometer.gpsspeedtracker.interfaces.SettingInterface
import com.gps.speedometer.odometer.gpsspeedtracker.`object`.SharedData
import com.gps.speedometer.odometer.gpsspeedtracker.utils.ColorUtils
import com.google.android.material.button.MaterialButton
import com.gps.speedometer.odometer.gpsspeedtracker.R
import com.gps.speedometer.odometer.gpsspeedtracker.biiling.BaseActivity
import com.gps.speedometer.odometer.gpsspeedtracker.biiling.SubVipActivity
import com.gps.speedometer.odometer.gpsspeedtracker.constants.ColorConstants
import com.gps.speedometer.odometer.gpsspeedtracker.databinding.ActivitySettingBinding
import com.gps.speedometer.odometer.gpsspeedtracker.databinding.DialogRateBinding


class Setting : BaseActivity(), SettingInterface.View, View.OnKeyListener {
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
    private lateinit var txtDistance: TextView
    private lateinit var edtWarningLimit: EditText
    private lateinit var vehicleDao: VehicleDao
    private var colorPosition = 0
    private var checkUnitClick = 0
    private var checkVehicleClick = 0
    private lateinit var myDataBase: MyDataBase
    val fragmentManager = (SharedData.activity as MainActivity2).supportFragmentManager
    private var notificationsFragment: NotificationsFragment? = null
    var color = Color.BLACK
    private var distance: Int = 0
    private var checkTheme = true

    @SuppressLint("CommitPrefEdits", "SetTextI18n", "RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        setUpActivity()
        setBackgroundALL()
        var check = ConfigModel.showSub
        if (check) {
            check = !proApplication.isSubVip
        }
        binding.premium.visibility = if (check) View.VISIBLE else View.GONE


        binding.btnSub.setOnClickListener {
            val intent = Intent(this@Setting, SubVipActivity::class.java)
            intent.putExtra("activity", "setting")
            startActivity(intent)
        }
        showNativeAds(binding.nativeContainer) {
        }
        swtAlarm.setOnCheckedChangeListener { _, isChecked ->
            saveSettingBoolean(SettingConstants.SPEED_ALARM, isChecked, swtAlarm)
        }
        binding.txtPolicy.setOnClickListener {
            val intent = Intent(this, ShowWebActitvity::class.java)
            intent.putExtra("link", "https://sites.google.com/view/policytosforgpsspeedometer/")
            startActivity(intent)
        }
        swtShowReset.setOnCheckedChangeListener { _, isChecked ->
            toggleShowReset()
        }

        binding.txtRating.setOnClickListener {
            getDialogRate().show()
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

        binding.resetColor.setOnClickListener {
            sharedPreferences.edit()
                .putInt(SettingConstants.COLOR_DISPLAY, ColorConstants.COLOR_DEFAULT).apply()
            SharedData.color.value = 0
            recreate()
        }


        btnMaxSpeepAnalog.setOnClickListener {
            getDialogSpeedAnalog().show()
        }

        btnResetDistance.setOnClickListener {
            txtDistance.text = when (checkUnitClick) {
                1 -> "000000 Mph"
                2 -> "000000 Km/h"
                3 -> "000000 Knot"
                else -> ""
            }
            getSharedPreferences("state", Service.MODE_PRIVATE).edit()
                .putInt(MyLocationConstants.DISTANCE, 0)
                .apply()
        }


        val listVehicle = arrayOf(
            btnTrain to 3,
            btnBicycle to 2,
            btnOto to 1
        )

        val listUnit = arrayOf(
            Triple(btnMph, "mph", "mi"),
            Triple(btnKm, "km/h", "km"),
            Triple(btnKnot, "knot", "nm"),
        )

        val listViewMode = arrayOf(
            binding.btnAnalog to 1,
            binding.btnDigital to 2,
            binding.btnMap to 3
        )

        listVehicle.forEach { (btn, vehicle) ->
            btn.setOnClickListener {
                updateVehicle(vehicle)
                removeTextWhenColorPositionIs0(btn, "vehicle")
            }
        }

        listUnit.forEach { (btn, speedUnit, distanceUnit) ->
            btn.setOnClickListener {
                updateSpeedUnit(speedUnit, distanceUnit)
                removeTextWhenColorPositionIs0(btn, "unit")
            }
        }

        listViewMode.forEach { (btn, viewMode) ->
            btn.setOnClickListener {
                setButtonViewModeClickListener(btn, viewMode)
            }
        }


        binding.dark.setOnClickListener { setThemeClickListener(binding.dark, true) }
        binding.light.setOnClickListener { setThemeClickListener(binding.light, false) }


        onClickBtnColor(btnColor2, btnColor3, btnColor4, btnColor5, btnColor6, btnColor7)
    }

    fun setBackGroundButtonAppTheme() {

        val isLightTheme = sharedPreferences.getBoolean(SettingConstants.THEME, true)
        Log.e("ssssssssssssssss",isLightTheme.toString())
        val themeButton = if (!isLightTheme) binding.light else binding.dark
        val themeColor = ColorUtils.checkColor(colorPosition)
        themeButton.setBackgroundColor(themeColor)
        removeTextWhenColorPositionIs0(themeButton, "theme")
    }

    private fun setUpActivity() {
        notificationsFragment = try {
            fragmentManager.findFragmentByTag("f2") as NotificationsFragment
        } catch (e: java.lang.Exception) {
            null
        }
        setTitleTextColors()
        setupActionBar()
        initializeData()
        edtWarningLimit.setOnKeyListener(this)
    }

    private fun initializeData() {
        sharedPreferences = getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE)
        myDataBase = MyDataBase.getInstance(this)
        vehicleDao = myDataBase.vehicleDao()
        colorPosition =
            sharedPreferences.getInt(SettingConstants.COLOR_DISPLAY, ColorConstants.COLOR_DEFAULT)
        distance = getSharedPreferences("state", Service.MODE_PRIVATE)
            .getInt(MyLocationConstants.DISTANCE, 0)
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.mToolBar)
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setTitleTextColors() {
        if (!ColorUtils.isThemeDark()) {
            btnResetDistance.iconTint = ColorStateList.valueOf(Color.BLACK)
            color = Color.WHITE
            binding.mToolBar.setTitleTextColor(Color.BLACK)
        } else {
            binding.mToolBar.setTitleTextColor(Color.WHITE)
        }
    }

    fun getDialogRate(): Dialog {
        val dialogBinding = DialogRateBinding.inflate(LayoutInflater.from(this))
        val dialog = Dialog(this).apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent);
            window?.setGravity(Gravity.TOP)
            setContentView(dialogBinding.root)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            if (ColorUtils.isThemeDark()) {
                dialogBinding.mRcy.setBackgroundColor(getColor(R.color.unchanged))
                dialogBinding.txt1.setTextColor(Color.WHITE)
                dialogBinding.txt2.setTextColor(Color.WHITE)
            }
            dialogBinding.btnRate.setOnClickListener {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.gps.speedometer.odometer.gpsspeedtracker")
                )
                startActivity(intent)
            }
        }
        return dialog
    }

    fun setThemeClickListener(button: MaterialButton, theme: Boolean) {
        //theme true là sáng

        val nightMode =
            if (!theme) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
        AppCompatDelegate.setDefaultNightMode(nightMode)
        sharedPreferences.edit().putBoolean(SettingConstants.THEME, theme).apply()
        button.setBackgroundColor(ColorUtils.checkColor(colorPosition))
        button.setTextColor(ColorUtils.checkColor(colorPosition))

        removeTextWhenColorPositionIs0(button, "theme")

    }

    fun setButtonViewModeClickListener(button: MaterialButton, viewMode: Int) {
        onRemoveBackGroundViewMode()
        button.setBackgroundColor(ColorUtils.checkColor(colorPosition))
        sharedPreferences.edit().putInt(SettingConstants.ViEW_MODE, viewMode).apply()
        removeTextWhenColorPositionIs0(button, "view_mode")
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
        SharedData.onShowTime.value =
            if (!swtClockDisplay.isChecked) View.INVISIBLE else View.VISIBLE
    }

    private fun saveSettingBoolean(key: String, value: Boolean, sw: Switch) {
        sw.thumbTintList =
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
        onRemoveBackGroundVehicle()
        setBackGroundButtonVehicleClick()
        registerReceiverUnitFromFragmentM2()
    }


    @SuppressLint("CommitPrefEdits")
    private fun updateSpeedUnit(speedUnit: String, distanceUnit: String) {
        sharedPreferences.edit().putString(SettingConstants.UNIT, speedUnit)
        SharedData.toUnitDistance = distanceUnit
        SharedData.toUnit = speedUnit
        registerReceiverUnitFromFragmentM2()
        onRemoveBackGroundUnit()
        setBackGroundButtonUnitClick()
        setBackGroundButtonViewMode()
        binding.a.text = speedUnit

    }

    private fun removeTextWhenColorPositionIs0(btnSetText: MaterialButton, type: String) {
        val colorButtonClick = if (!ColorUtils.isThemeDark()) Color.WHITE else Color.BLACK
        val colorButtonDefault = if (!ColorUtils.isThemeDark()) Color.BLACK else Color.WHITE

        if (colorPosition == 0) {
            val buttons = when (type) {
                "view_mode" -> listOf(binding.btnAnalog, binding.btnMap, binding.btnDigital)
                "unit" -> listOf(binding.btnKm, binding.btnKnot, binding.btnMph)
                "vehicle" -> listOf(binding.btnTrain, binding.btnBicycle, binding.btnOto)
                else -> listOf(binding.dark, binding.light)
            }

            buttons.forEach { it.setTextColor(colorButtonDefault) }
            btnSetText.setTextColor(colorButtonClick)
        }
    }

    private fun setBackGroundButtonUnitClick() {
        val color = ColorUtils.checkColor(colorPosition)
        fun setButtonBackgroundColor(button: MaterialButton, unit: String, color: Int) {
            button.setBackgroundColor(
                if (SharedData.toUnit == unit) {
                    removeTextWhenColorPositionIs0(button, "unit")
                    color
                } else 0
            )
        }
        setButtonBackgroundColor(btnKm, "km/h", color)
        setButtonBackgroundColor(btnMph, "mph", color)
        setButtonBackgroundColor(btnKnot, "knot", color)
    }


    private fun setBackGroundButtonViewMode() {
        val color = ColorUtils.checkColor(colorPosition)
        val viewMode = sharedPreferences.getInt(SettingConstants.ViEW_MODE, 0)

        fun setButtonBackground(btn: MaterialButton, mode: Int) {
            btn.setBackgroundColor(
                if (viewMode == mode) {
                    removeTextWhenColorPositionIs0(btn, "view_mode")
                    color
                } else 0
            )
        }

        with(binding) {
            setButtonBackground(btnAnalog, 1)
            setButtonBackground(btnDigital, 2)
            setButtonBackground(btnMap, 3)
        }
    }

    private fun setBackGroundButtonVehicleClick() {
        val color = ColorUtils.checkColor(colorPosition)
        val vehicleType = myDataBase.vehicleDao().getVehicleChecked().type

        fun setButtonBackground(btn: MaterialButton, type: Int) {
            btn.setBackgroundColor(
                if (vehicleType == type) {
                    removeTextWhenColorPositionIs0(btn, "vehicle")
                    color
                } else 0
            )
        }

        setButtonBackground(binding.btnOto, 1)
        setButtonBackground(binding.btnBicycle, 2)
        setButtonBackground(binding.btnTrain, 3)
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
        if (notificationsFragment != null) notificationsFragment!!.onClearMap(
            swtTrackOnMap.isChecked
        )
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
        return AlertDialog.Builder(this@Setting, R.style.AlertDialogCustom).apply {
            setTitle("Max Speed of Analog Meter").setSingleChoiceItems(
                array, position
            ) { _, which ->
                item = array[which].toInt()
            }
            setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            setPositiveButton("Ok") { dialog: DialogInterface, _: Int ->
                SharedData.speedAnalog.value = item
                myDataBase.vehicleDao().updateMaxSpeed(item)
                btnMaxSpeepAnalog.text = item.toString()
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


    private fun setBackgroundALL() {
        edtWarningLimit.setTextColor(ColorUtils.checkColor(colorPosition))
        btnMaxSpeepAnalog.setTextColor(ColorUtils.checkColor(colorPosition))
        btnMaxSpeepAnalog.iconTint = ColorStateList.valueOf(ColorUtils.checkColor(colorPosition))
        setColorSwt()
        setSpecifications()
        setBackGroundButtonUnitClick()
        setBackGroundButtonVehicleClick()
        setBackGroundButtonViewMode()
        setBackGroundButtonAppTheme()
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
                colorPosition = getColorPosition(it.id)
                SharedData.color.value = colorPosition
                saveColorChecked()
                setBackgroundALL()
            }
        }
    }

    private fun getColorPosition(id: Int): Int {
        return when (id) {
            R.id.btnColor2 -> 2
            R.id.btnColor3 -> 3
            R.id.btnColor4 -> 4
            R.id.btnColor5 -> 5
            R.id.btnColor6 -> 6
            R.id.btnColor7 -> 7
            else -> 1
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
        sw.isChecked = sharedPreferences.getBoolean(constants, true)
        sw.thumbTintList =
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

    override fun onRemoveBackGroundVehicle() {
        btnBicycle.setBackgroundColor(color)
        btnOto.setBackgroundColor(color)
        btnTrain.setBackgroundColor(color)
    }

    override fun onRemoveBackGroundUnit() {
        btnKm.setBackgroundColor(color)
        btnMph.setBackgroundColor(color)
        btnKnot.setBackgroundColor(color)
    }

    override fun onRemoveBackGroundViewMode() {
        with(binding) {
            btnAnalog.setBackgroundColor(color)
            btnMap.setBackgroundColor(color)
            btnDigital.setBackgroundColor(color)
        }
    }


    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
            try {
                val url = edtWarningLimit.text.toString()
                if (url.toInt() >= 0 && url.isNotEmpty()) {
                    myDataBase.vehicleDao().updateWarning(url.toInt())
                    edtWarningLimit.setText(url)
                }
            } catch (e: Exception) {
                Toast.makeText(this@Setting, "Tốc độ không để trống", Toast.LENGTH_SHORT).show()
            }
            return true
        }

        return false
    }
}