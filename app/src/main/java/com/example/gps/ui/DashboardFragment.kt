package com.example.gps.ui

import android.annotation.SuppressLint
import android.app.Service
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.gps.constants.MyLocationConstants
import com.example.gps.utils.FontUtils
import com.example.gps.R
import com.example.gps.constants.SettingConstants
import com.example.gps.`object`.SharedData
import com.example.gps.databinding.FragmentDashboardBinding
import com.example.gps.interfaces.MeasurementInterFace
import com.example.gps.presenter.MeasurementPresenter
import com.example.gps.utils.ColorUtils
import com.example.gps.utils.TimeUtils

class DashboardFragment : Fragment(R.layout.fragment_dashboard), MeasurementInterFace.View {

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
        val measurement = MeasurementPresenter(this, this)
        measurement.onColorChange()
        measurement.onTimeChange()
        measurement.onCurrentSpeedChange()
        binding.imgRotate?.setOnClickListener {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }



    }

    override fun onResume() {
        super.onResume()
        this.binding.txtUnit?.text = SharedData.toUnit

    }

    override fun displayTimeChange(long: Long) {
        binding.time?.text = TimeUtils.formatTime(long)
        FontUtils.setFont(requireContext(), binding.time )
    }

    override fun displayColorChange(int: Int) {
        with(binding) {
            txtSpeed.setTextColor(ColorUtils.checkColor(int))
            txtUnit?.setTextColor(ColorUtils.checkColor(int))
            time?.setTextColor(ColorUtils.checkColor(int))
        }
        FontUtils.setFont(requireContext(), binding.time, binding.txtUnit, binding.txtSpeed)
    }

    override fun displayCurrentSpeedChange(string: String, l: Long) {
        binding.txtSpeed.text = string
        FontUtils.setFont(requireContext(),  binding.txtSpeed)
    }
}