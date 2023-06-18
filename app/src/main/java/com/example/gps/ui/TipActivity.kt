package com.example.gps.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.gps.R
import com.example.gps.constants.SettingConstants
import com.example.gps.databinding.ActivityTipActvityBinding

class TipActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTipActvityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTipActvityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferences=getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE)
        binding.btn1.setOnClickListener {
            val intent=Intent(this,ShowWebActitvity::class.java)
            intent.putExtra("link","https://www.youtube.com/watch?v=VOCuXMWyZyo&ab_channel=PiiMusic")
            startActivity(intent)
        }
        binding.btn2.setOnClickListener {

        }
        binding.btn3.setOnClickListener {

        }
        binding.btn4.setOnClickListener {

        }
        binding.btnMoreTip.setOnClickListener {
            sharedPreferences.edit().putBoolean(SettingConstants.CHECK_OPEN,true).apply()
            startActivity(Intent(this,MainActivity2::class.java))
        }
        binding.btnStartTrip.setOnClickListener {
            sharedPreferences.edit().putBoolean(SettingConstants.CHECK_OPEN,true).apply()
            startActivity(Intent(this,MainActivity2::class.java))
        }
    }
}