package com.example.gps.ui.setting

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.Service
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.example.gps.MyLocationConstants
import com.example.gps.R
import com.example.gps.SettingConstants
import com.example.gps.SharedData
import com.example.gps.dao.MyDataBase
import com.example.gps.dao.VehicleDao
import com.example.gps.databinding.ActivitySettingBinding
import com.example.gps.ui.DashboardFragment
import com.example.gps.ui.HomeFragment
import com.example.gps.ui.MainActivity2
import com.example.gps.ui.NotificationsFragment
import com.example.gps.ui.ParameterFragment
import com.example.gps.utils.ColorUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.internal.ContextUtils.getActivity
import java.lang.Exception


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
    private lateinit var btnWarningLimit: MaterialButton
    private lateinit var vehicleDao: VehicleDao
    private var colorPosition = 1
    private var checkUnitClick = 0
    private var checkVehicleClick = 0
    private lateinit var mainActivity2: MainActivity2
    private lateinit var myDataBase: MyDataBase
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var parameterFragment: ParameterFragment
    private lateinit var childFragment: Fragment
    private var homeFragment: HomeFragment? = null
    private var dashboardFragment: DashboardFragment? = null
    private var notificationsFragment: NotificationsFragment? = null
    var color = Color.BLACK
    private var distance: Int = 0

    @SuppressLint("CommitPrefEdits", "SetTextI18n", "RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
            btnResetDistance.iconTint = ColorStateList.valueOf(Color.BLACK)
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
        mainActivity2 = getActivity(SharedData.activity) as MainActivity2
        navHostFragment =
            (mainActivity2.supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main2) as NavHostFragment?)!!
        childFragment = navHostFragment.childFragmentManager.fragments[0]
        parameterFragment =
            childFragment.childFragmentManager.findFragmentById(R.id.frag) as ParameterFragment

        distance = getSharedPreferences("state", Service.MODE_PRIVATE).getInt(
            MyLocationConstants.DISTANCE,
            0
        )
        setBackgroundALL()
        swtAlarm.setOnCheckedChangeListener { _, isChecked ->
            saveSettingBoolean(SettingConstants.SPEED_ALARM, isChecked)
        }

        swtShowReset.setOnCheckedChangeListener { _, isChecked ->
            toggleShowReset()
        }

        swtClockDisplay.setOnCheckedChangeListener { _, isChecked ->
            toggleClockVisibilityLandscape()
        }

        swtTrackOnMap.setOnCheckedChangeListener { _, isChecked ->
            saveSettingBoolean(SettingConstants.TRACK_ON_MAP, isChecked)
            toggleTrackOnMap()
        }

        swtShowSpeedInNoti.setOnCheckedChangeListener { _, isChecked ->
            saveSettingBoolean(SettingConstants.DISPLAY_SPEED, isChecked)
        }


        btnMaxSpeepAnalog.setOnClickListener {
            getDialogSpeedAnalog().show()
        }
        btnWarningLimit.setOnClickListener {
            getDialogWarningLimit().show()
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
        btnLetGo.setOnClickListener {
            finish()
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

        onClickBtnColor(btnColor2, btnColor3, btnColor4, btnColor5, btnColor6, btnColor7)

    }

    private fun toggleShowReset() {
        saveSettingBoolean(SettingConstants.SHOW_RESET_BUTTON, swtShowReset.isChecked)
        when (childFragment) {
            is HomeFragment -> {
                if (homeFragment == null) homeFragment = childFragment as HomeFragment
                homeFragment!!.onVisibilityChanged(swtShowReset.isChecked)
            }
        }
    }

    private fun toggleClockVisibilityLandscape() {
        saveSettingBoolean(SettingConstants.CLOCK_DISPLAY, swtClockDisplay.isChecked)
        parameterFragment.toggleClockVisibilityLandscape(swtClockDisplay.isChecked)


    }

    private fun saveSettingBoolean(key: String, value: Boolean) {
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
        parameterFragment.onUnitChange()
        removeBackgroundButtonUnit()
        setBackGroundButtonUnitClick()
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
        when (childFragment) {
            is HomeFragment -> {
                if (homeFragment == null) homeFragment = childFragment as HomeFragment
                homeFragment!!.onUnitChange()
            }

            is DashboardFragment -> {
                if (dashboardFragment == null) dashboardFragment =
                    childFragment as DashboardFragment
                dashboardFragment!!.onUnitChange()
            }

            is NotificationsFragment -> {
                if (notificationsFragment == null) notificationsFragment =
                    childFragment as NotificationsFragment
                notificationsFragment!!.onUnitChange()
            }
        }
    }

    private fun registerReceiverColorFromFragmentM2() {
        when (childFragment) {
            is HomeFragment -> {
                if (homeFragment == null) homeFragment = childFragment as HomeFragment
                homeFragment!!.onColorChange(colorPosition)
            }

            is DashboardFragment -> {
                if (dashboardFragment == null) dashboardFragment =
                    childFragment as DashboardFragment
                dashboardFragment!!.onColorChange(colorPosition)
            }

            is NotificationsFragment -> {
                if (notificationsFragment == null) notificationsFragment =
                    childFragment as NotificationsFragment
                notificationsFragment!!.onColorChange(colorPosition)
            }
        }
    }

    private fun toggleTrackOnMap() {
        when (childFragment) {
            is NotificationsFragment -> {
                if (notificationsFragment == null) notificationsFragment =
                    childFragment as NotificationsFragment
                notificationsFragment!!.onVisibilityPolyLine(swtTrackOnMap.isChecked)
            }
        }
    }

    private fun getDialogWarningLimit(): Dialog {
        val txtUrl = EditText(this)
        txtUrl.inputType = EditorInfo.TYPE_CLASS_NUMBER
        return AlertDialog.Builder(this).setView(txtUrl)
            .setPositiveButton(
                "Đông ý"
            ) { dialog, whichButton ->
                try {
                    val url = txtUrl.text.toString()
                    if (url.toInt() >= 0 && url.isNotEmpty()) {
                        myDataBase.vehicleDao().updateWarning(url.toInt())
                        btnWarningLimit.text = url
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Tốc độ không để trống", Toast.LENGTH_SHORT).show()
                }

                dialog.cancel()
            }
            .setNegativeButton(
                "Hủy bỏ"
            ) { dialog, whichButton ->
                dialog.cancel()
            }.setMessage("Nhập giới hạn tốc độ")
            .create()

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
        btnWarningLimit.text = vehicleChecked.limitWarning.toString()
        SharedData.speedAnalog.value = vehicleChecked.clockSpeed
        txtDistance.text = SharedData.convertDistance(
            distance.toDouble()
        ).toInt().toString()

    }


    private fun removeBackgroundButtonVehicle() {
        btnBicycle.setBackgroundColor(color)
        btnOto.setBackgroundColor(color)
        btnTrain.setBackgroundColor(color)

    }


    private fun setBackgroundALL() {
        btnLetGo.setBackgroundColor(ColorUtils.checkColor(colorPosition))
        btnWarningLimit.setTextColor(ColorUtils.checkColor(colorPosition))
        btnMaxSpeepAnalog.setTextColor(ColorUtils.checkColor(colorPosition))
        setColorSwt()
        setSpecifications()
        setBackGroundButtonUnitClick()
        setBackGroundButtonVehicleClick()


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
                parameterFragment.onColorChange(colorPosition)
                registerReceiverColorFromFragmentM2()
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