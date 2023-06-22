package com.gps.speedometer.odometer.gpsspeedtracker.ui


import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.viewpager2.widget.ViewPager2
import com.gps.speedometer.odometer.gpsspeedtracker.constants.SettingConstants
import com.gps.speedometer.odometer.gpsspeedtracker.`object`.SharedData
import com.gps.speedometer.odometer.gpsspeedtracker.ui.adpater.TabAdapter
import com.gps.speedometer.odometer.gpsspeedtracker.utils.ColorUtils
import com.google.android.material.tabs.TabLayoutMediator
import com.gps.speedometer.odometer.gpsspeedtracker.MyApplication
import com.gps.speedometer.odometer.gpsspeedtracker.R
import com.gps.speedometer.odometer.gpsspeedtracker.databinding.ActivityMain2Binding

interface onRecever {
    fun sendDataToSecondFragment()

}

class MainActivity2 : AppCompatActivity(), onRecever {


    lateinit var binding: ActivityMain2Binding
    private lateinit var tabAdapter: TabAdapter
    lateinit var viewPager: ViewPager2
    private lateinit var sharedPreferences: SharedPreferences
    var color = if (ColorUtils.isThemeDark()) Color.WHITE else Color.BLACK


    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpActivity()

    }

    @SuppressLint("MissingPermission", "ResourceType")
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
        viewPager.setPageTransformer { _, position ->
            if (position == 2F) {
                binding.viewPager2.isUserInputEnabled = false
            }
        }
        SharedData.color.observe(this) {
            binding.tabLayout.setTabTextColors(getColor(R.color.unchanged), getColor(getColorRes()))
            binding.tabLayout.setSelectedTabIndicatorColor(getColor(getColorRes()))
        }
        binding.viewPager2.adapter = tabAdapter
        TabLayoutMediator(binding.tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Analog"
                1 -> tab.text = "Digital"
                2 -> {
                    tab.text = "Map"
                }
            }
        }.attach()
        val viewMode = sharedPreferences.getInt(SettingConstants.ViEW_MODE, 0)
        if(MyApplication.check) binding.viewPager2.currentItem = viewMode - 1;MyApplication.check=false
    }


    private fun getColorRes(): Int {
        val intColor = getSharedPreferences(
            SettingConstants.SETTING,
            Service.MODE_PRIVATE
        ).getInt(SettingConstants.COLOR_DISPLAY, 0)
        if (intColor == 0) {
            return if (ColorUtils.isThemeDark()) R.color.white else R.color.black
        }
        when (intColor) {
            2 -> return R.color.color_2
            3 -> return R.color.color_3
            4 -> return R.color.color_4
            5 -> return R.color.color_5
            6 -> return R.color.color_6
        }
        return R.color.color_2
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            menu!!.getItem(1).isVisible = false
            menu.getItem(0).isVisible = true
        }
        for (i in 0 until menu!!.size()) {
            if (menu.getItem(i).itemId != R.id.subcribe) menu.getItem(i).iconTintList =
                ColorStateList.valueOf(color)
        }

        return true
    }

    private var checkRotation = false
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> startActivity(Intent(this, Setting::class.java))
            R.id.history -> startActivity(Intent(this, HistoryActivity::class.java))
            R.id.tip -> {
//                val intent = Intent(this, MoreTipActivity::class.java)
//                intent.putExtra("activityLaunchedFrom", "Main2")
//                startActivity(intent)
            }

            R.id.rotation -> {
                val check = binding.container.rotationX
                binding.container.rotationX = if (check == 0F) 180F else 0F
            }
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