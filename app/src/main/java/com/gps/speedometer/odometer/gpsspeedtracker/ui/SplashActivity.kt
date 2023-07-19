package com.gps.speedometer.odometer.gpsspeedtracker.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.access.pro.config.ConfigModel
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.gps.speedometer.odometer.gpsspeedtracker.R
import com.gps.speedometer.odometer.gpsspeedtracker.biiling.BaseActivity
import com.gps.speedometer.odometer.gpsspeedtracker.biiling.SubVipActivity
import com.gps.speedometer.odometer.gpsspeedtracker.constants.SettingConstants
import com.gps.speedometer.odometer.gpsspeedtracker.`object`.SharedData


class SplashActivity : BaseActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        setupBilling{p0,p1-> }
        // Force a crash

         getConfigData(true)
        createTimer(3L)
        MobileAds.initialize(this)
        FirebaseApp.initializeApp(applicationContext)
        sharedPreferences = getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE)
        with(SharedData) {
            onShowTime.value = if (sharedPreferences.getBoolean(
                    SettingConstants.CLOCK_DISPLAY,
                    true
                )
            ) View.VISIBLE else View.INVISIBLE
            onShowResetButton.value = if (sharedPreferences.getBoolean(
                    SettingConstants.SHOW_RESET_BUTTON,
                    true
                )
            ) View.VISIBLE else View.INVISIBLE
            color.value = sharedPreferences.getInt(SettingConstants.COLOR_DISPLAY, 0)

        }
    }

    fun getBatteryCapacity(context: Context): Long {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val mBatteryManager = context.getSystemService(BATTERY_SERVICE) as BatteryManager
            val chargeCounter =
                mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            val capacity = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            return if (chargeCounter == Int.MIN_VALUE || capacity == Int.MIN_VALUE) 0 else (chargeCounter / capacity * 100).toLong()
        }
        return 0
    }

    private fun createTimer(seconds: Long) {
        val countDownTimer: CountDownTimer = object : CountDownTimer(seconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                Log.d("sssssssssssssss",ConfigModel.showSub.toString())
                if (ConfigModel.showSub){
                    startActivity(Intent(this@SplashActivity, SubVipActivity::class.java))
                    finish()
                }else{
                    if (!getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE).getBoolean(
                            SettingConstants.CHECK_OPEN,
                            false
                        )
                    ) {
                        val intent = Intent(this@SplashActivity , SettingOptionsActivitys::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this@SplashActivity, MainActivity2::class.java)
                        startActivity(intent)
                    }
                }

//                val application = application as? MyApplication
//                // If the application is not an instance of MyApplication, log an error message and
//                // start the MainActivity without showing the app open ad.
//                if (application == null) {
//                    Log.e("LOG_TAG", "Failed to cast application to MyApplication.")
//                    startMainActivity()
//                    return
//                }
//                // Show the app open ad.
//                application.showAdIfAvailable(
//                    this@SplashActivity,
//                    object : MyApplication.OnShowAdCompleteListener {
//                        override fun onShowAdComplete() {
//
//                        }
//                    })
            }
        }
        countDownTimer.start()
    }

    /** Start the MainActivity. */

}