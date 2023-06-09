package com.example.gps.ui

import android.annotation.SuppressLint
import android.app.Service
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gps.MyLocationConstants
import com.example.gps.utils.FontUtils
import com.example.gps.R
import com.example.gps.SettingConstants
import com.example.gps.SharedData
import com.example.gps.dao.MyDataBase
import com.example.gps.databinding.FragmentDashboardBinding
import com.example.gps.interfaces.DigitalInterface
import com.example.gps.utils.StringUtils
import java.math.BigDecimal

class DashboardFragment : Fragment(R.layout.fragment_dashboard), DigitalInterface {

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
        val positionsColor = sharedPreferences.getInt(SettingConstants.COLOR_DISPLAY, 2)
        onColorChange(positionsColor)

        with(binding) {
            this.txtKm3.text = if (SharedData.toUnit != "km/h") "mi" else "km"
            binding.txtKm4.text = SharedData.toUnit
            FontUtils.setFont(requireContext(), this.txtSpeed, this.txtKm4, txtKm3, txtDistance1)
            onDataChange()
            txtKm4.text = SharedData.toUnit
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onDataChange() {
        SharedData.currentSpeedLiveData.observe(viewLifecycleOwner) {
            val key = it.keys.first()
            binding.txtSpeed.text = "%03d".format(SharedData.convertSpeed(key).toInt())
            with(binding)
            {
                FontUtils.setFont(
                    requireContext(),
                    this.txtSpeed,
                     txtKm3,
                    txtDistance1
                )
            }
        }
        SharedData.distanceLiveData.observe(viewLifecycleOwner) {
            if (it.toInt() != 0) {
                binding.txtDistance1.text = "%09d".format ( (SharedData.convertDistance(allDistance.toDouble()) + SharedData.convertDistance(
                    it
                )).toInt())
                with(binding)
                {
                    FontUtils.setFont(
                        requireContext(),
                        this.txtSpeed,
                         txtKm3,
                        txtDistance1
                    )
                }
            }
        }
        setDataWhenComBack()
    }


    @SuppressLint("SetTextI18n")
    private fun setDataWhenComBack() {
        with(binding) {
            txtKm4.text = SharedData.toUnit
            txtKm3.text = if (SharedData.toUnit != "km/h") "mi" else "km"
            val convertedSpeed =
                SharedData.convertSpeed(SharedData.currentSpeedLiveData.value!!.keys.first())
            txtSpeed.text = "%03d".format(convertedSpeed.toInt())
            txtDistance1.text ="%09d".format (SharedData.convertDistance(
            sharedPreferencesStates.getInt(MyLocationConstants.DISTANCE, 0).toDouble() ).toInt() )

        }
    }


    override fun onResetDistances() {
        binding.txtDistance1.text = "000000"
    }

    override fun onColorChange(i: Int) {
        FontUtils.setTextColor(i, binding.txtSpeed, binding.txtKm4)
    }

    override fun onUnitChange() {
        setDataWhenComBack()
    }
}