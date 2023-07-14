package com.gps.speedometer.odometer.gpsspeedtracker.biiling

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gps.speedometer.odometer.gpsspeedtracker.R
import com.gps.speedometer.odometer.gpsspeedtracker.databinding.ActivitySupVipBinding
import com.gps.speedometer.odometer.gpsspeedtracker.ui.adpater.AdapterSupVip


class SupVipActivity : AppCompatActivity() {
    private var _binding: ActivitySupVipBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySupVipBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val adapter = AdapterSupVip(
            listOf(
                R.drawable.user_star_1,
                R.drawable.user_star_2,
                R.drawable.user_star_3,
                R.drawable.user_star_4
            )
        )
        binding.viewPager2.adapter=adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}