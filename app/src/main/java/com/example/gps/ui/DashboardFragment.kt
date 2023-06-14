package com.example.gps.ui

import android.annotation.SuppressLint
import android.app.Service
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.gps.constants.MyLocationConstants
import com.example.gps.utils.FontUtils
import com.example.gps.R
import com.example.gps.constants.SettingConstants
import com.example.gps.`object`.SharedData
import com.example.gps.databinding.FragmentDashboardBinding
import com.example.gps.interfaces.DigitalInterface
import com.example.gps.utils.TimeUtils

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesStates: SharedPreferences
    private var allDistance: Int = 0

    @SuppressLint("SetTextI18n", "SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentDashboardBinding.bind(view)
        sharedPreferencesStates =
            requireActivity().getSharedPreferences("state", Service.MODE_PRIVATE)
        sharedPreferences =
            requireActivity().getSharedPreferences(SettingConstants.SETTING, Service.MODE_PRIVATE)
        allDistance = sharedPreferencesStates.getInt(MyLocationConstants.DISTANCE, 0)
        SharedData.time.observe(viewLifecycleOwner) {
            binding.time?.text = TimeUtils.formatTime(it)
        }
    }
}