package com.example.gps.ui

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.gps.R
import com.example.gps.SettingConstants
import com.example.gps.SharedData
import com.example.gps.databinding.FragmentHomeBinding
import com.example.gps.utils.ColorUtils


class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var sharedPreferences: SharedPreferences

    // This property is only valid between onCreateView and
    // onDestroyView.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentHomeBinding.bind(view)

        sharedPreferences = view.context.getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE)

        var timePrevious = System.currentTimeMillis()
        with(binding) {
            // change ring color, have fun with this method
            changeBackGroundSpeedView()
            SharedData.currentSpeedLiveData.observe(viewLifecycleOwner) {
                var speed1 = String.format("%.1f", it)
                speed1 = speed1.replace(",", ".");
                this.speed.speedTo(speed1.toFloat(), System.currentTimeMillis() - timePrevious)
                timePrevious = System.currentTimeMillis()

            }
        }
    }

    private fun changeBackGroundSpeedView() {
        try {
            val positionsColor = sharedPreferences.getInt(SettingConstants.COLOR_DISPLAY, 2)
            if (positionsColor == 1) binding.speed.trianglesColor = Color.BLUE
            binding.speed.setSpeedometerColor(ColorUtils.checkColor(positionsColor));
        } catch (e: Exception) {
            Log.d("Sssssssssssssss", e.toString())
        }
    }

    override fun onResume() {
        changeBackGroundSpeedView()
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }
}