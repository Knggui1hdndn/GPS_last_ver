package com.example.gps.ui


import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.gps.R
import com.example.gps.SettingConstants
import com.example.gps.SettingConstants.Companion.CLOCK_DISPLAY
import com.example.gps.SharedData
import com.example.gps.dao.MyDataBase
import com.example.gps.databinding.ActivityMain2Binding
import com.example.gps.model.Speed
import com.example.gps.ui.setting.Setting
import com.example.gps.utils.TimeUtils
import com.example.gps.utils.UnitUtils
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity2 : AppCompatActivity() {


    private lateinit var binding: ActivityMain2Binding
    private var check = false
    private lateinit var sharedPreferences: SharedPreferences
    fun getCurrentUnit(): String {
        val myDataBase = MyDataBase.getInstance(this).SpeedDao()
        return UnitUtils.getUnit(myDataBase.getChecked().type)
    }

    override fun onStart() {
        super.onStart()
        if (!check) setUnitSpeepAndDistance();check = true
    }

    private fun setUnitSpeepAndDistance() {
        try {
            SharedData.toUnit = getCurrentUnit()
            SharedData.fromUnit = SharedData.toUnit
            when (SharedData.fromUnit) {
                "km/h" -> SharedData.fromUnitDistance = "km"
                "mph" -> SharedData.fromUnitDistance = "mi"
                "knot" -> SharedData.fromUnitDistance = "nm"
            }
            SharedData.toUnitDistance = SharedData.fromUnitDistance
        } catch (e: Exception) {
            Log.d("okok312o1", "${e.message}")

        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n", "MissingPermission", "InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        SharedData.activity = this
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        setUnitSpeepAndDistance()
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_home_black_24dp)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        sharedPreferences = getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE)
        binding.times.typeface = Typeface.createFromAsset(assets, "font_lcd.ttf")
        binding.times.text = "00 : 00 : 00"
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main2)


        binding.times.isVisible = sharedPreferences.getBoolean(CLOCK_DISPLAY, true)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, _, _ ->
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_home_black_24dp)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

        }

        navController.addOnDestinationChangedListener { controller, destination, arguments -> }
        if (!sharedPreferences.getBoolean(
                SettingConstants.CHECK_OPEN,
                false
            )
        ) {
            val myDataBase = MyDataBase.getInstance(this)
            val builder = AlertDialog.Builder(this)
            val list = arrayOf("Mph", "Km", "Knot")
            builder.setTitle("Chọn đơn vị đo").setItems(
                list
            ) { dialog, which ->
                sharedPreferences.edit().apply {
                    putBoolean(SettingConstants.CHECK_OPEN, true)
                    putInt(SettingConstants.COLOR_DISPLAY, 2)
                    putBoolean(SettingConstants.DISPLAY_SPEED, true)
                    putBoolean(SettingConstants.TRACK_ON_MAP, true)
                    putBoolean(SettingConstants.SHOW_RESET_BUTTON, true)
                    putBoolean(SettingConstants.SPEED_ALARM, true)
                }.apply()
                myDataBase.SpeedDao().insertSpeed(Speed(1, which + 1 == 1))
                myDataBase.SpeedDao().insertSpeed(Speed(2, which + 1 == 2))
                myDataBase.SpeedDao().insertSpeed(Speed(3, which + 1 == 3))
                insert(myDataBase)
                setUnitSpeepAndDistance()
                val navHostFragment: NavHostFragment? = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main2) as NavHostFragment?
                (navHostFragment!!.childFragmentManager.fragments[0] as HomeFragment).setSpeedAndUnit()
                (navHostFragment.childFragmentManager.fragments[0].childFragmentManager.findFragmentById(R.id.frag) as ParameterFragment).setDataWhenComeBack()
                dialog.cancel()
            }
            builder.create().show()
        }
        try {
            val navHostFragment: NavHostFragment? =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main2) as NavHostFragment?
            (navHostFragment?.childFragmentManager?.fragments?.get(0)?.childFragmentManager?.findFragmentById(
                R.id.frag
            ) as ParameterFragment).setDataWhenComeBack()
            (navHostFragment.childFragmentManager.fragments[0] as HomeFragment).setSpeedAndUnit()
        } catch (e: java.lang.Exception) {
        }

        SharedData.distanceLiveData.value = 20.0
        SharedData.time.observe(this) { binding.times.text = TimeUtils.formatTime(it) }
    }

    private fun insert(myDataBase: MyDataBase) {
        for (i in 1..3) {
            myDataBase.vehicleDao().insertVehicle(240, 100, 1, 1, i)
            myDataBase.vehicleDao().insertVehicle(240, 20, 2, 0, i)
            myDataBase.vehicleDao().insertVehicle(320, 100, 3, 0, i)
        }

    }

    override fun onResume() {
        super.onResume()
        binding.navView.itemIconTintList = getColorRes()
        binding.times.visibility = if (sharedPreferences.getBoolean(CLOCK_DISPLAY, true)) View.VISIBLE else View.GONE

    }

    private fun getColorRes(): ColorStateList {
        val intColor = getSharedPreferences(
            SettingConstants.SETTING,
            Service.MODE_PRIVATE
        ).getInt(SettingConstants.COLOR_DISPLAY, 2)
        Log.d("okookoo", intColor.toString())
        when (intColor) {
            1 -> return getColorStateList(R.color.color1)
            3 -> return getColorStateList(R.color.color2)
            2 -> return getColorStateList(R.color.color3)
            4 -> return getColorStateList(R.color.color4)
            5 -> return getColorStateList(R.color.color5)
            6 -> return getColorStateList(R.color.color6)
            7 -> return getColorStateList(R.color.color7)
        }
        return getColorStateList(R.color.color2)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.history, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.history) {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
        if (item.itemId == android.R.id.home) startActivity(Intent(this, Setting::class.java))
        return true
    }


}