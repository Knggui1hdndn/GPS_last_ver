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
import java.math.BigDecimal

class DashboardFragment : Fragment(R.layout.fragment_dashboard), DigitalInterface {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesStates: SharedPreferences

    @SuppressLint("SetTextI18n", "SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentDashboardBinding.bind(view)
        sharedPreferencesStates =
            requireActivity().getSharedPreferences("state", Service.MODE_PRIVATE)
        sharedPreferences = requireActivity().getSharedPreferences(
            SettingConstants.SETTING,
            Service.MODE_PRIVATE
        )

        val allDistance = sharedPreferencesStates.getInt(MyLocationConstants.DISTANCE, 0)
        val positionsColor = sharedPreferences.getInt(SettingConstants.COLOR_DISPLAY, 2)
        onColorChange(positionsColor)

        with(binding) {
            this.txtKm3.text = if (SharedData.toUnit != "km/h") "mi" else "km"
            binding.txtKm4.text = SharedData.toUnit
            FontUtils.setFont(requireContext(), this.txtSpeed)
            SharedData.currentSpeedLiveData.observe(viewLifecycleOwner) {
                if (it.keys.first() == 0.0) txtSpeed.text = "000"
                when (String.format("%.0f", it.keys.first()).length) {
                    1 -> {
                        txtSpeed.text = "00" + SharedData.convertSpeed(it.keys.first()).toInt()
                    }

                    2 -> {
                        txtSpeed.text = "0" +  SharedData.convertSpeed(it.keys.first()).toInt()
                    }

                    else -> txtSpeed.text =  SharedData.convertSpeed(it.keys.first()).toInt().toString()
                }
            }
            SharedData.distanceLiveData.observe(viewLifecycleOwner) {
                txtDistance1.text =
                    SharedData.convertDistance(allDistance.toDouble()).toInt().toString()
            }
            txtDistance1.text = SharedData.convertDistance(allDistance.toDouble()).toInt().toString()
            txtKm4.text = SharedData.toUnit
        }
    }


    @SuppressLint("SetTextI18n")
    private fun setDataWhenComBack() {
        with(binding) {
            txtKm4.text = SharedData.toUnit
            this.txtKm3.text = if (SharedData.toUnit != "km/h") "mi" else "km"
            when (SharedData.convertSpeed(txtSpeed.text.toString().toDouble()).toInt()) {
                1 -> {
                    txtSpeed.text =
                        "00" + SharedData.convertSpeed(txtSpeed.text.toString().toDouble()).toInt()
                }

                2 -> {
                    txtSpeed.text =
                        "0" + SharedData.convertSpeed(txtSpeed.text.toString().toDouble()).toInt()
                }

                else -> txtSpeed.text = "000"
            }

            this.txtDistance1.text =
                SharedData.convertSpeed(txtDistance1.text.toString().toDouble()).toInt().toString()
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