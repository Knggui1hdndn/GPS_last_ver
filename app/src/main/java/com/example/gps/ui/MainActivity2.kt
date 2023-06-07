package com.example.gps.ui


import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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
            when (SharedData.toUnit) {
                "km/h" -> SharedData.toUnitDistance = "km"
                "mph" -> SharedData.toUnitDistance = "mi"
                "knot" -> SharedData.toUnitDistance = "nm"
            }
        } catch (e: Exception) {
            Log.d("okok312o1", "${e.message}")

        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n", "MissingPermission", "InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpActivity()
        checkOpenFirst()
        setDataDefault()
        SharedData.time.observe(this) { binding.times.text = TimeUtils.formatTime(it) }
    }

    private fun setUpActivity() {
        binding = ActivityMain2Binding.inflate(layoutInflater)
         SharedData.activity = this
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        setUnitSpeepAndDistance()
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_home_black_24dp)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        sharedPreferences = getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE)
        binding.times.typeface = Typeface.createFromAsset(assets, "font_lcd.ttf")
        binding.times.text = "00 : 00 : 00"
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main2)
        val checkShowClock = sharedPreferences.getBoolean(CLOCK_DISPLAY, false)
        binding.times.visibility = if (checkShowClock) View.VISIBLE else View.GONE
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, _, _ ->
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_home_black_24dp)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        if (resources.configuration.orientation==Configuration.ORIENTATION_LANDSCAPE){
            binding.toolbar.visibility=View.GONE
        }
    }

    private fun setDataDefault() {
        try {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main2) as? NavHostFragment
            navHostFragment?.childFragmentManager?.fragments?.getOrNull(0)?.childFragmentManager?.findFragmentById(
                R.id.frag
            )?.let { fragment ->
                if (fragment is ParameterFragment) {
                    fragment.setDataWhenComeBack()
                }
            }
            navHostFragment?.childFragmentManager?.fragments?.getOrNull(0)?.let { fragment ->
                if (fragment is HomeFragment) {
                    fragment.setSpeedAndUnit()
                }
            }
        } catch (_: Exception) {
        }

    }

    private fun checkOpenFirst() {
        if (!sharedPreferences.getBoolean(SettingConstants.CHECK_OPEN, false)) {
            val myDataBase = MyDataBase.getInstance(this)
            val builder = AlertDialog.Builder(this)
            val list = arrayOf("Mph", "Km", "Knot")
            builder.setCancelable(false)
            builder.setTitle("Chọn đơn vị đo").setItems(list) { dialog, which ->
                saveInShared()
                myDataBase.SpeedDao().insertSpeed(Speed(1, which + 1 == 1))
                myDataBase.SpeedDao().insertSpeed(Speed(2, which + 1 == 2))
                myDataBase.SpeedDao().insertSpeed(Speed(3, which + 1 == 3))
                insert(myDataBase)
                setUnitSpeepAndDistance()
                setDataDefault()
                dialog.cancel()
            }
            builder.create().show()
        }
    }


    private fun saveInShared() {
        sharedPreferences.edit().apply {
            putBoolean(SettingConstants.CHECK_OPEN, true)
            putInt(SettingConstants.COLOR_DISPLAY, 2)
            putBoolean(SettingConstants.DISPLAY_SPEED, true)
            putBoolean(SettingConstants.TRACK_ON_MAP, true)
            putBoolean(SettingConstants.SHOW_RESET_BUTTON, true)
            putBoolean(SettingConstants.SPEED_ALARM, true)
        }.apply()
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
        binding.times.visibility =
            if (sharedPreferences.getBoolean(CLOCK_DISPLAY, true)) View.VISIBLE else View.GONE

    }

    private fun getColorRes(): ColorStateList {
        val intColor = getSharedPreferences(
            SettingConstants.SETTING,
            Service.MODE_PRIVATE
        ).getInt(SettingConstants.COLOR_DISPLAY, 2)
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.history, menu)
        val check = AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES
        menu!!.getItem(0).iconTintList =
            if (check) ColorStateList.valueOf(Color.BLACK) else ColorStateList.valueOf(Color.WHITE)
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