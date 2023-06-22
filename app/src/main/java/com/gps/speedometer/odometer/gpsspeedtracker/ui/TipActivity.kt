package com.gps.speedometer.odometer.gpsspeedtracker.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gps.speedometer.odometer.gpsspeedtracker.constants.SettingConstants
import com.gps.speedometer.odometer.gpsspeedtracker.databinding.ActivityTipActvityBinding
import com.gps.speedometer.odometer.gpsspeedtracker.utils.ColorUtils


class TipActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTipActvityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTipActvityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE)
        if (ColorUtils.isThemeDark()) binding.mToolBar.setTitleTextColor(Color.WHITE) else binding.mToolBar.setTitleTextColor(
            Color.BLACK
        )
        binding.btn1.setOnClickListener {
            startWebActivity("https://s1partscenter.com/blog/entries/on-the-road-why-are-odometers-so-important")

        }
        binding.btn1.setPaintFlags(binding.btn1.paintFlags or Paint.UNDERLINE_TEXT_FLAG)

        binding.btn2.apply {
            setOnClickListener {
                startWebActivity("https://mechanicbase.com/electric/odometer-reading/")
            }
            setPaintFlags(paintFlags or Paint.UNDERLINE_TEXT_FLAG)
        }

        binding.btn3.apply {
            setOnClickListener {
                startWebActivity("https://www.nhtsa.gov/book/countermeasures/countermeasures/42-reduce-and-enforce-speed-limits")
            }
            setPaintFlags(paintFlags or Paint.UNDERLINE_TEXT_FLAG)
        }

        binding.btn4.apply {
            setOnClickListener {
                startWebActivity("https://www.wikihow.com/Stop-Speeding")
            }
            setPaintFlags(paintFlags or Paint.UNDERLINE_TEXT_FLAG)
        }

        binding.btnMoreTip.setOnClickListener {
            sharedPreferences.edit().putBoolean(SettingConstants.CHECK_OPEN, true).apply()
            val intent1 = Intent(this, MoreTipActivity::class.java)
            startActivity(intent1)
            finish()
        }

        binding.btnStartTrip.setOnClickListener {
            sharedPreferences.edit().putBoolean(SettingConstants.CHECK_OPEN, true).apply()
            startActivity(Intent(this, MainActivity2::class.java))
            finish()
        }
    }

    private fun startWebActivity(link: String) {
        val intent = Intent(this, ShowWebActitvity::class.java)
        intent.putExtra("link", link)
        startActivity(intent)
    }

}