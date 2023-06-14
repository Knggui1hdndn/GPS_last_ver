package com.example.gps.ui


import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.gps.broadcast.ListenBattery
import com.example.gps.R
import com.example.gps.constants.SettingConstants
import com.example.gps.constants.SettingConstants.Companion.CLOCK_DISPLAY
import com.example.gps.`object`.SharedData
import com.example.gps.dao.MyDataBase
import com.example.gps.databinding.ActivityMain2Binding
import com.example.gps.interfaces.SignalInterface
import com.example.gps.ui.adpater.TabAdapter
import com.example.gps.utils.TimeUtils
import com.google.android.material.tabs.TabLayoutMediator


class MainActivity2 : AppCompatActivity(), SignalInterface {


    private lateinit var binding: ActivityMain2Binding
    private lateinit var sharedPreferences: SharedPreferences
     private lateinit var broadCast: ListenBattery
     private lateinit var fragmentSignal: FragmentSignal
    private fun setUnitSpeedAndDistance() {
        try {
            SharedData.toUnit = sharedPreferences.getString(SettingConstants.UNIT, "").toString()
            when (SharedData.toUnit) {
                "km/h" -> SharedData.toUnitDistance = "km"
                "mph" -> SharedData.toUnitDistance = "mi"
                "knot" -> SharedData.toUnitDistance = "nm"
            }

        } catch (e: Exception) {


        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpActivity()
      //  SharedData.time.observe(this) { binding.times.text = TimeUtils.formatTime(it) }

    }

    @SuppressLint("MissingPermission")
    private fun setUpActivity() {
        binding = ActivityMain2Binding.inflate(layoutInflater)
        SharedData.activity = this
        setContentView(binding.root)
        binding.toolbar.title= "ODOMETER"

        setSupportActionBar(binding.toolbar)
        setUnitSpeedAndDistance()


        supportActionBar?.title = "ODOMETER"
        sharedPreferences = getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE)
//        binding.times.typeface = Typeface.createFromAsset(assets, "font_lcd.ttf")

        val tabAdapter = TabAdapter(supportFragmentManager, lifecycle)
        binding.viewPager2.adapter = tabAdapter

        TabLayoutMediator(binding.tabLayout!!, binding.viewPager2!!) { tab, position ->

            when (position) {
                0 -> tab.text = "Analog"
                1 -> tab.text = "Kĩ thuật số"
                2 -> {
                    tab.text = "Bản đồ"
                     val currentFragment = tabAdapter.createFragment(2) as NotificationsFragment
                }
            }
        }.attach()
        binding.viewPager2.isUserInputEnabled = false
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
         }


    }


    private fun checkOpenFirst() {
        if (!sharedPreferences.getBoolean(SettingConstants.CHECK_OPEN, false)) {
            val myDataBase = MyDataBase.getInstance(this)
            val builder = AlertDialog.Builder(this)
            val list = arrayOf("mph", "km/h", "knot")
            builder.setCancelable(false)
            builder.setTitle("Chọn đơn vị đo").setItems(list) { dialog, which ->
                saveInShared(list[which])
                insert(myDataBase, which)
                setUnitSpeedAndDistance()
                dialog.cancel()
            }
            builder.create().show()
        } else {
            setUnitSpeedAndDistance()
        }
    }


    private fun saveInShared(which: String) {
        SharedData.toUnit = which
        sharedPreferences.edit().apply {
            putBoolean(SettingConstants.CHECK_OPEN, true)
            putInt(SettingConstants.COLOR_DISPLAY, 2)
            putBoolean(SettingConstants.DISPLAY_SPEED, true)
            putString(SettingConstants.UNIT, which)
            putBoolean(SettingConstants.TRACK_ON_MAP, true)
            putBoolean(SettingConstants.SHOW_RESET_BUTTON, true)
            putBoolean(SettingConstants.SPEED_ALARM, true)
        }.apply()
    }

    private fun insert(myDataBase: MyDataBase, which: Int) {
        myDataBase.vehicleDao().insertVehicle(240, 100, 1, if (which + 1 == 1) 1 else 0)
        myDataBase.vehicleDao().insertVehicle(240, 20, 2, if (which + 1 == 2) 1 else 0)
        myDataBase.vehicleDao().insertVehicle(320, 100, 3, if (which + 1 == 3) 1 else 0)
    }

    override fun onResume() {
        super.onResume()


     }

    override fun onPause() {
        super.onPause()
         Log.d("okokko", "sodkf1")

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
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return true
    }

    override fun onBatteryDataReceived(int: Int) {
        fragmentSignal.onBatteryDataReceived(int)

    }

    override fun onStrengthGPSDataReceived(int: Int, satelliteCount: Int) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        } else {
            getStrengthGPS()
        }

    }

    @SuppressLint("MissingPermission")
    private fun getStrengthGPS() {


    }

    private val result =  registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
            if (granted.entries.all { it.value }) {
                getStrengthGPS()
            }
        }
}