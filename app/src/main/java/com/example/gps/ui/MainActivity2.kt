package com.example.gps.ui


import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
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
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.gps.R
import com.example.gps.constants.SettingConstants
import com.example.gps.`object`.SharedData
import com.example.gps.dao.MyDataBase
import com.example.gps.databinding.ActivityMain2Binding
import com.example.gps.databinding.DialogRateBinding
import com.example.gps.interfaces.SignalInterface
import com.example.gps.`object`.CheckPermission
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
        viewPager.setPageTransformer { page, position ->
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
                1 -> tab.text = "Kĩ thuật số"
                2 -> {
                    tab.text = "Bản đồ"
                }
            }
        }.attach()
        val viewMode = sharedPreferences.getInt(SettingConstants.ViEW_MODE, 0)
        binding.viewPager2.setCurrentItem(viewMode - 1)
    }


    private fun getColorRes(): Int {
        val intColor = getSharedPreferences(
            SettingConstants.SETTING,
            Service.MODE_PRIVATE
        ).getInt(SettingConstants.COLOR_DISPLAY, 0)
        Log.d("okoko", intColor.toString())
        if (intColor == 0) {
            val isNightMode =
                AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
            if (isNightMode) return R.color.white else return R.color.black
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
            R.id.subcribe -> getDialogRate().show()
        }
        return true
    }

      fun getDialogRate(): Dialog {
        val dialogBinding = DialogRateBinding.inflate(LayoutInflater.from(this))
        val dialog = Dialog(this).apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent);
            setContentView(dialogBinding.root)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            if (ColorUtils.isThemeDark()) {
                dialogBinding.mRcy.setBackgroundColor(getColor(R.color.unchanged))
                dialogBinding.txt1.setTextColor(Color.WHITE)
                dialogBinding.txt2.setTextColor(Color.WHITE)
            }
        }
        return dialog
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