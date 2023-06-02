package com.example.gps.ui

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.OrientationEventListener
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gps.MyLocationConstants
import com.example.gps.R
import com.example.gps.SettingConstants
import com.example.gps.SharedData
import com.example.gps.dao.MyDataBase
import com.example.gps.databinding.FragmentHomeBinding
import com.example.gps.service.MyService
import com.example.gps.utils.ColorUtils
import com.google.android.gms.ads.RequestConfiguration


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
            try {
                imgRotateScreen!!.setOnClickListener {
                    requireActivity().requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                }

                val myDataBase = MyDataBase.getInstance(requireContext())
                imgReset!!.setOnClickListener {
                    val status = view.context.getSharedPreferences("state", MODE_PRIVATE)
                        .getString(MyLocationConstants.STATE, null)
                    if (status != MyLocationConstants.STOP && status != null) {
                        val intent = Intent(requireContext(), MyService::class.java)
                        intent.action = MyLocationConstants.STOP
                        requireActivity().startService(intent)
                        SharedData.checkService = true

                    }
                }
                val maxSpeedAnalog =
                    myDataBase.vehicleDao()
                        .getVehicleChecked(myDataBase.SpeedDao().getChecked().type)
                speed.maxSpeed = maxSpeedAnalog.clockSpeed.toFloat()
                speed.unit = SharedData.toUnit
            } catch (e: Exception) {

            }
            // change ring color, have fun with this method
            SharedData.speedAnalog.observe(viewLifecycleOwner) {
                speed.maxSpeed = SharedData.convertSpeed(it.toFloat()).toFloat()
            }
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
            binding.speed.unit = SharedData.toUnit
            val positionsColor = sharedPreferences.getInt(SettingConstants.COLOR_DISPLAY, 2)
            if (positionsColor == 1) binding.speed.trianglesColor = Color.BLUE
            binding.speed.setSpeedometerColor(ColorUtils.checkColor(positionsColor));
        } catch (e: Exception) {
            Log.d("Sssssssssssssss", e.toString())
        }
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(requireContext(), "Landscape Mode", Toast.LENGTH_SHORT).show()
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(requireContext(), "Portrait Mode", Toast.LENGTH_SHORT).show()
        }
        super.onConfigurationChanged(newConfig)

    }
    override fun onResume() {
        changeBackGroundSpeedView()
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }
}