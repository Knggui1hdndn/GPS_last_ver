package com.example.gps.ui


import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
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
import com.example.gps.utils.ColorUtils
import com.google.android.material.tabs.TabLayoutMediator

interface onRecever {
    fun sendDataToSecondFragment()
}

class MainActivity2 : AppCompatActivity(), onRecever {


    lateinit var binding: ActivityMain2Binding
    lateinit var tabAdapter: TabAdapter
    lateinit var viewPager: ViewPager2
    private lateinit var sharedPreferences: SharedPreferences
    var color = if (ColorUtils.isThemeDark()) Color.WHITE else Color.BLACK


    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpActivity()

    }

    @SuppressLint("MissingPermission")
    private fun setUpActivity() {
        binding = ActivityMain2Binding.inflate(layoutInflater)
        SharedData.activity = this
        setContentView(binding.root)
        binding.toolbar.title = "ODOMETER"
        binding.toolbar.setTitleTextColor(color)
        setSupportActionBar(binding.toolbar)
        sharedPreferences = getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE)
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
        val viewMode = sharedPreferences.getInt(SettingConstants.ViEW_MODE, 0)
        binding.viewPager2.setCurrentItem(viewMode - 1)
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    private fun getColorRes(): ColorStateList {
        val intColor = getSharedPreferences(
            SettingConstants.SETTING,
            Service.MODE_PRIVATE
        ).getInt(SettingConstants.COLOR_DISPLAY, 2)
        when (intColor) {
            3 -> return getColorStateList(R.color.color1)
            1 -> return getColorStateList(R.color.color2)
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
        for (i in 1 until menu!!.size()) {
            menu.getItem(i).iconTintList = ColorStateList.valueOf(color)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> startActivity(Intent(this, Setting::class.java))
            R.id.history -> startActivity(Intent(this, HistoryActivity::class.java))
            R.id.tip -> startActivity(Intent(this, TipActivity::class.java))
            R.id.subcribe -> startActivity(Intent(this, Setting::class.java))
        }
        return true
    }

    override fun sendDataToSecondFragment() {
        try {
            val frag = supportFragmentManager.findFragmentByTag("f0") as? FragmentSignal
            val frag1 = supportFragmentManager.findFragmentByTag("f1") as? FragmentSignal

            frag?.onStrengthGPSDataReceived(0, 0)
            frag1?.onStrengthGPSDataReceived(0, 0)
        } catch (_: Exception) {
        }

    }
}