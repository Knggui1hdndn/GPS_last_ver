package com.gps.speedometer.odometer.gpsspeedtracker.ui

import android.annotation.SuppressLint
import android.app.Service
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.gps.speedometer.odometer.gpsspeedtracker.R
import com.gps.speedometer.odometer.gpsspeedtracker.utils.FontUtils
import com.gps.speedometer.odometer.gpsspeedtracker.constants.SettingConstants
import com.gps.speedometer.odometer.gpsspeedtracker.databinding.FragmentDashboardBinding
import com.gps.speedometer.odometer.gpsspeedtracker.`object`.SharedData
import com.gps.speedometer.odometer.gpsspeedtracker.interfaces.MeasurementInterFace
import com.gps.speedometer.odometer.gpsspeedtracker.presenter.MeasurementPresenter
import com.gps.speedometer.odometer.gpsspeedtracker.utils.ColorUtils
import com.gps.speedometer.odometer.gpsspeedtracker.utils.TimeUtils

import kotlin.properties.Delegates

class DashboardFragment : Fragment(R.layout.fragment_dashboard), MeasurementInterFace.View {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesStates: SharedPreferences
    private var check by Delegates.notNull<Boolean>()

    @SuppressLint("SetTextI18n", "SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentDashboardBinding.bind(view)
        check =requireActivity().resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
        sharedPreferencesStates = requireActivity().getSharedPreferences("state", Service.MODE_PRIVATE)
        sharedPreferences = requireActivity().getSharedPreferences(SettingConstants.SETTING, Service.MODE_PRIVATE)
        val measurement = MeasurementPresenter(this, this)
        measurement.colorChange()
        measurement.timeChange()
        measurement.currentSpeedChange()
        measurement.setVisibilityTime()
        if (binding.bannerContainer != null) (requireActivity() as MainActivity2).showBannerAds(
            binding.bannerContainer!!
        )
        binding.imgRotate?.setOnClickListener {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        if (check) FontUtils.setFont(requireContext(), binding.time)
        FontUtils.setFont(requireContext(), binding.txtSpeed, binding.txtUnit)
    }

    override fun onResume() {
        super.onResume()
        this.binding.txtUnit.text = SharedData.toUnitDistance

    }

    override fun onVisibilityTime(visibility: Int) {
        if (check) binding.time?.visibility = visibility
    }

    override fun displayTimeChange(long: Long) {
        binding.time?.text = TimeUtils.formatTime(long)

    }

    override fun displayColorChange(int: Int) {
        with(binding) {
            if (check) {
                time?.setTextColor(if (ColorUtils.isThemeDark()) Color.BLACK else Color.WHITE)
                time?.backgroundTintList = ColorStateList.valueOf(ColorUtils.checkColor(int))
            }
            txtSpeed.setTextColor(ColorUtils.checkColor(int))
            txtUnit?.setTextColor(ColorUtils.checkColor(int))

        }

    }

    override fun displayCurrentSpeedChange(string: String, l: Long) {
        binding.txtSpeed.text = string
    }
}