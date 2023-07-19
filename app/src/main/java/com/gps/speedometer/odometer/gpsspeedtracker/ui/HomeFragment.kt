package com.gps.speedometer.odometer.gpsspeedtracker.ui

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.gps.speedometer.odometer.gpsspeedtracker.R
import com.gps.speedometer.odometer.gpsspeedtracker.constants.SettingConstants
import com.gps.speedometer.odometer.gpsspeedtracker.dao.MyDataBase
import com.gps.speedometer.odometer.gpsspeedtracker.databinding.FragmentHomeBinding
import com.gps.speedometer.odometer.gpsspeedtracker.interfaces.MeasurementInterFace
import com.gps.speedometer.odometer.gpsspeedtracker.`object`.SharedData
import com.gps.speedometer.odometer.gpsspeedtracker.presenter.MeasurementPresenter
import com.gps.speedometer.odometer.gpsspeedtracker.utils.ColorUtils
import com.gps.speedometer.odometer.gpsspeedtracker.utils.TimeUtils
import com.google.android.gms.ads.AdRequest
import com.gps.speedometer.odometer.gpsspeedtracker.biiling.BaseActivity
import kotlin.properties.Delegates


class HomeFragment : Fragment(R.layout.fragment_home), MeasurementInterFace.View {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var sharedPreferencesSetting: SharedPreferences
    private lateinit var sharedPreferencesState: SharedPreferences
    private lateinit var myDataBase: MyDataBase
    private var textColor by Delegates.notNull<Int>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentHomeBinding.bind(view)
        myDataBase = MyDataBase.getInstance(requireContext())
        sharedPreferencesSetting =
            requireContext().getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE)
        sharedPreferencesState = requireContext().getSharedPreferences("state", MODE_PRIVATE)
        val isNightMode =
            AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        val backgroundColor = if (isNightMode) Color.BLACK else Color.WHITE
        binding.speed.backgroundCircleColor = backgroundColor
        textColor = if (!isNightMode) Color.BLACK else Color.WHITE
        val measurement = MeasurementPresenter(this, this)
        measurement.colorChange()
        measurement.timeChange()
        measurement.currentSpeedChange()
        measurement.setVisibilityTime()
        if (binding.bannerContainer != null) (requireActivity() as MainActivity2).showBannerAds(
            binding.bannerContainer!!
        )
        with(binding) {
            imgRotate?.setOnClickListener {
                requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }

            SharedData.speedAnalog.observe(viewLifecycleOwner) {
                binding.speed.maxSpeed = it.toFloat()
            }
         }


    }


    override fun onResume() {
        super.onResume()
        binding.speed.unit = SharedData.toUnit
     }

    override fun onVisibilityTime(visibility: Int) {
        binding.time?.visibility = visibility
    }

    override fun displayTimeChange(long: Long) {
        binding.time?.text = TimeUtils.formatTime(long)
    }

    override fun displayColorChange(int: Int) {
        binding.speed.speedTextColor = textColor
        binding.speed.textColor = textColor
        binding.speed.unitTextColor = ColorUtils.checkColor(int)
        binding.time?.setTextColor(if (ColorUtils.isThemeDark()) Color.BLACK else Color.WHITE)
        binding.time?.backgroundTintList = ColorStateList.valueOf(ColorUtils.checkColor(int))
        try {
            binding.speed.trianglesColor =
                if (!ColorUtils.isThemeDark()) Color.BLACK else Color.WHITE
            binding.speed.setSpeedometerColor(ColorUtils.checkColor(int))
        } catch (_: Exception) {
        }
    }

    override fun displayCurrentSpeedChange(string: String, l: Long) {
        binding.speed.speedTo(string.toFloat(), l)
    }

}