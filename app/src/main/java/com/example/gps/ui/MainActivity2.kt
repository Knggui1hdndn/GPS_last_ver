package com.example.gps.ui


import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.location.GnssStatus
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.gps.R
import com.example.gps.constants.SettingConstants
import com.example.gps.`object`.SharedData
import com.example.gps.dao.MyDataBase
import com.example.gps.databinding.ActivityMain2Binding
import com.example.gps.interfaces.SignalInterface
import com.example.gps.ui.adpater.TabAdapter
import com.google.android.material.tabs.TabLayoutMediator

interface onRecever {
    fun sendDataToSecondFragment()
}

class MainActivity2 : AppCompatActivity(), onRecever {


    lateinit var binding: ActivityMain2Binding
    lateinit var tabAdapter: TabAdapter
    lateinit var viewPager: ViewPager2
    private lateinit var sharedPreferences: SharedPreferences
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
        binding.toolbar.title = "ODOMETER"
        setSupportActionBar(binding.toolbar)
        setUnitSpeedAndDistance()
        supportActionBar?.title = "ODOMETER"
        sharedPreferences = getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE)
//        binding.times.typeface = Typeface.createFromAsset(assets, "font_lcd.ttf")
        checkOpenFirst()
        tabAdapter = TabAdapter(supportFragmentManager, lifecycle)
        viewPager = binding.viewPager2
        viewPager.setPageTransformer { page, position ->
            if (position == 2F) {
                binding.viewPager2.isUserInputEnabled = false
            }
        }
        binding.viewPager2.adapter = tabAdapter
        TabLayoutMediator(binding.tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Analog"
                1 -> tab.text = "Kĩ thuật số"
                2 -> {
                    tab.text = "Bản đồ"
                }
            }
        }.attach()

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
        when (item.itemId) {
            R.id.settings ->startActivity(Intent(this,Setting::class.java))
                R.id.history ->startActivity(Intent(this,HistoryActivity::class.java))
                R.id.tip ->startActivity(Intent(this,TipActivity::class.java))
                R.id.subcribe ->startActivity(Intent(this,Setting::class.java))
        }
        return true
    }

    override fun sendDataToSecondFragment() {

        try {
            val frag = getSupportFragmentManager().findFragmentByTag("f" + 0);
            val frag1 = getSupportFragmentManager().findFragmentByTag("f" + 1);
            (frag!!.childFragmentManager.findFragmentById(R.id.signal) as FragmentSignal).onStrengthGPSDataReceived(
                0,
                0
            )
            (frag1!!.childFragmentManager.findFragmentById(R.id.signal) as FragmentSignal).onStrengthGPSDataReceived(
                0,
                0
            )
        } catch (_: java.lang.Exception) {
        }
    }
}