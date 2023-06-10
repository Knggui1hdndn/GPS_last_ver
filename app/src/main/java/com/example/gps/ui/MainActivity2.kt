package com.example.gps.ui


import android.Manifest
import android.annotation.SuppressLint
import android.app.Instrumentation.ActivityResult
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.location.GnssStatus
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.gps.ListenBattery
import com.example.gps.R
import com.example.gps.SettingConstants
import com.example.gps.SettingConstants.Companion.CLOCK_DISPLAY
import com.example.gps.SharedData
import com.example.gps.dao.MyDataBase
import com.example.gps.databinding.ActivityMain2Binding
import com.example.gps.interfaces.SignalInterface
import com.example.gps.ui.setting.Setting
import com.example.gps.utils.TimeUtils
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity2 : AppCompatActivity(), SignalInterface {


    private lateinit var binding: ActivityMain2Binding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var intentFilter: IntentFilter
    private lateinit var broadCast: ListenBattery
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var childFragment: Fragment
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

        requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 1)
        checkOpenFirst()
        setDataDefault()
        SharedData.time.observe(this) { binding.times.text = TimeUtils.formatTime(it) }

    }

    @SuppressLint("MissingPermission")
    private fun setUpActivity() {
        binding = ActivityMain2Binding.inflate(layoutInflater)
        SharedData.activity = this
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        setUnitSpeedAndDistance()
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_settings_24)
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
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.toolbar.visibility = View.GONE
        }
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main2) as NavHostFragment
        childFragment = navHostFragment?.childFragmentManager?.fragments!![0]

        fragmentSignal =
            (childFragment.childFragmentManager.findFragmentById(R.id.signal) as FragmentSignal)
        broadCast = ListenBattery()
        intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
    }

    private fun setDataDefault() {
        try {
            childFragment.childFragmentManager.findFragmentById(
                R.id.frag
            )?.let { fragment ->
                if (fragment is ParameterFragment) {
                    Log.d("oisjaijx", "oko")
                    fragment.setDataWhenComeBack()
                }
            }
            childFragment.let { fragment ->
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
            val list = arrayOf("mph", "km/h", "knot")
            builder.setCancelable(false)
            builder.setTitle("Chọn đơn vị đo").setItems(list) { dialog, which ->
                saveInShared(list[which])
                insert(myDataBase, which)
                setUnitSpeedAndDistance()
                setDataDefault()
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
        binding.navView.itemIconTintList = getColorRes()
        binding.times.visibility =
            if (sharedPreferences.getBoolean(CLOCK_DISPLAY, true)) View.VISIBLE else View.GONE
        registerReceiver(broadCast, intentFilter)
        Log.d("okokko", "sodkf")
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadCast)
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

    override fun onBatteryDataReceived(int: Int) {
        fragmentSignal.onBatteryDataReceived(int)
        Log.d("okokko", "sodkf1"+int)

    }

    override fun onStrengthGPSDataReceived(int: Int, satelliteCount: Int) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            result.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        } else {
            getStrengthGPS()
        }

    }

    @SuppressLint("MissingPermission")
    private fun getStrengthGPS() {
        if(this::navHostFragment.isInitialized){
            childFragment = navHostFragment?.childFragmentManager?.fragments!![0]
            fragmentSignal = (childFragment.childFragmentManager.findFragmentById(R.id.signal) as FragmentSignal)
            (getSystemService(LOCATION_SERVICE) as LocationManager).registerGnssStatusCallback(
                object : GnssStatus.Callback() {
                    @RequiresApi(Build.VERSION_CODES.R)
                    @SuppressLint("MissingPermission")
                    override fun onSatelliteStatusChanged(status: GnssStatus) {
                        val satelliteCount = status.satelliteCount
                        var totalSignalStrength = 0.0
                        for (i in 0 until satelliteCount) {
                            val cn0DbHz = status.getCn0DbHz(i)
                            totalSignalStrength += cn0DbHz
                        }
                        val averageSignalStrength = totalSignalStrength / satelliteCount
                        fragmentSignal.onStrengthGPSDataReceived(
                            averageSignalStrength.toInt(),
                            status.satelliteCount
                        )
                    }
                }, Handler(
                    Looper.getMainLooper()!!
                )
            )
        }

    }

    private val result =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
            if (granted.entries.all { it.value }) {
                getStrengthGPS()
            }
        }
}