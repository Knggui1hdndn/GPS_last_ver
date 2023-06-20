package com.example.gps.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.gps.MyApplication
import com.example.gps.R
import com.example.gps.constants.SettingConstants
import com.example.gps.`object`.SharedData
import com.google.android.gms.ads.MobileAds


class SplashActivity : AppCompatActivity() {
    private fun setUnitSpeedAndDistance() {
        try {
            SharedData.toUnit =
                getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE).getString(
                    SettingConstants.UNIT,
                    ""
                ).toString()
            when (SharedData.toUnit) {
                "km/h" -> SharedData.toUnitDistance = "km"
                "mph" -> SharedData.toUnitDistance = "mi"
                "knot" -> SharedData.toUnitDistance = "nm"
            }
        } catch (_: Exception) {
        }
    }


    private lateinit var sharedPreferences:SharedPreferences

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        setUnitSpeedAndDistance()
        createTimer(3L)
        MobileAds.initialize(this)
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
            color.value= sharedPreferences.getInt(SettingConstants.COLOR_DISPLAY,0)
            Log.d("okokodds",color.value.toString())

        }
    }


    private fun createTimer(seconds: Long) {
        val countDownTimer: CountDownTimer = object : CountDownTimer(seconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                startMainActivity()
                finish()
                val application = application as? MyApplication
                // If the application is not an instance of MyApplication, log an error message and
                // start the MainActivity without showing the app open ad.
                if (application == null) {
                    Log.e("LOG_TAG", "Failed to cast application to MyApplication.")
                    startMainActivity()
                    return
                }
                // Show the app open ad.
                application.showAdIfAvailable(
                    this@SplashActivity,
                    object : MyApplication.OnShowAdCompleteListener {
                        override fun onShowAdComplete() {

                        }
                    })
            }
        }
        countDownTimer.start()
    }

    /** Start the MainActivity. */
    fun startMainActivity() {
        if (!sharedPreferences.getBoolean(SettingConstants.CHECK_OPEN, false)) {
            val intent = Intent(this, SettingOptionsActivitys::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }
    }
}