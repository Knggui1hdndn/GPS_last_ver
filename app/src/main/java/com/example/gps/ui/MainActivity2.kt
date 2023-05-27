package com.example.gps.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.gps.MyApplication
import com.example.gps.R
import com.example.gps.SettingConstants
import com.example.gps.SharedData
import com.example.gps.dao.MyDataBase
import com.example.gps.databinding.ActivityMain2Binding
import com.example.gps.model.Speed
import com.example.gps.utils.TimeUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity2 : AppCompatActivity() {

    private val locationCallback1 = object : LocationCallback() {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        @SuppressLint("SuspiciousIndentation")
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation = locationResult.lastLocation
            Log.d("abcxyz", "lastLocation $lastLocation")
        }
    }
    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
        .setWaitForAccurateLocation(true)
        .setMinUpdateIntervalMillis(0)
        .setMaxUpdateDelayMillis(0)
        .build()
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var binding: ActivityMain2Binding

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n", "MissingPermission", "InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_home_black_24dp)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.times.typeface = Typeface.createFromAsset(assets, "font_lcd.ttf")
        binding.times.text = "00 : 00 : 00"
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main2)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback1,
            Looper.getMainLooper()
        )

        requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 1)
        val sharedPreferences = getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE)
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
                dialog.cancel()
            }
            builder.create().show()
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


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.history, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.history) {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        return true
    }
}