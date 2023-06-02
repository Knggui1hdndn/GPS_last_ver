package com.example.gps.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
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
import android.view.OrientationEventListener
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.gps.MyReceiver
import com.example.gps.R
import com.example.gps.SettingConstants
import com.example.gps.SettingConstants.Companion.CLOCK_DISPLAY
import com.example.gps.SharedData
import com.example.gps.dao.MyDataBase
import com.example.gps.databinding.ActivityMain2Binding
import com.example.gps.model.Speed
import com.example.gps.ui.setting.Setting
import com.example.gps.utils.TimeUtils
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity2 : AppCompatActivity()   {


    private lateinit var binding: ActivityMain2Binding

    private lateinit var sharedPreferences: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n", "MissingPermission", "InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_home_black_24dp)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        sharedPreferences = getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE)
        binding.times.typeface = Typeface.createFromAsset(assets, "font_lcd.ttf")
        binding.times.text = "00 : 00 : 00"
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main2)

//        val googleApiAvailability = GoogleApiAvailability.getInstance()
//        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
//
//        if (resultCode == ConnectionResult.SUCCESS) {
//            // Thiết bị hỗ trợ hoạt động nhận dạng
//            Log.d("ActivitySupport", "Thiết bị hỗ trợ hoạt động nhận dạng")
//        } else {
//            // Thiết bị không hỗ trợ hoạt động nhận dạng
//            Log.d("ActivitySupport", "Thiết bị không hỗ trợ hoạt động nhận dạng")
//        }

        requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 1)
        requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)

        binding.times.isVisible = sharedPreferences.getBoolean(CLOCK_DISPLAY, true)
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
                val unit = MyDataBase.getInstance(this).SpeedDao().getChecked().type
                SharedData.fromUnit = if (unit == 1) {
                    "mph";
                } else if (unit == 2) "km/h" else "knot"
                if (unit == 1 || unit == 2) {
                    SharedData.speedAnalog.value = 240
                } else {
                    SharedData.speedAnalog.value = 320
                }
                SharedData.toUnit = SharedData.fromUnit
                dialog.cancel()
            }
            builder.create().show()
        }
        val speed = MyDataBase.getInstance(this).SpeedDao().getChecked()
        if (speed != null) {
            val unit = speed.type
            SharedData.fromUnit = if (unit == 1) "mph" else if (unit == 2) "km/h" else "knot"
            SharedData.toUnit = SharedData.fromUnit
        }

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

        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), 1
        )
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
        binding.times.isVisible = sharedPreferences.getBoolean(CLOCK_DISPLAY, true)

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
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        if (item.itemId == android.R.id.home) startActivity(Intent(this, Setting::class.java))
        return true
    }


}