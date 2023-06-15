package com.example.gps.ui

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.gps.R
import com.example.gps.constants.SettingConstants
import com.example.gps.dao.MyDataBase
import com.example.gps.databinding.FragmentHomeBinding
import com.example.gps.interfaces.MeasurementInterFace
import com.example.gps.`object`.SharedData
import com.example.gps.presenter.MeasurementPresenter
import com.example.gps.utils.ColorUtils
import com.example.gps.utils.TimeUtils
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
        val isNightMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        val backgroundColor = if (isNightMode) Color.BLACK else Color.WHITE
        binding.speed.backgroundCircleColor = backgroundColor
        textColor = if (!isNightMode) Color.BLACK else Color.WHITE
        val measurement = MeasurementPresenter(this, this)
        measurement.onColorChange()
        measurement.onTimeChange()
        measurement.onCurrentSpeedChange()

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

    override fun displayTimeChange(long: Long) {
        binding.time?.text = TimeUtils.formatTime(long)
    }

    override fun displayColorChange(int: Int) {
        binding.speed.speedTextColor = textColor
        binding.speed.textColor = textColor
        binding.speed.trianglesColor = ColorUtils.checkColor(int)
        binding.speed.unitTextColor = ColorUtils.checkColor(int)
        binding.time?.setTextColor(ColorUtils.checkColor(int))
        binding.speed.setSpeedometerColor(ColorUtils.checkColor(int))
    }

    override fun displayCurrentSpeedChange(string: String, l: Long) {
        binding.speed.speedTo(string.toFloat(), l * 1000)
    }

}