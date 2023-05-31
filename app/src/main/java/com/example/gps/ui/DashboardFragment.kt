package com.example.gps.ui

import android.annotation.SuppressLint
import android.app.Service
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
import java.math.BigDecimal

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private var binding: FragmentDashboardBinding? = null
private var unit=""
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentDashboardBinding.bind(view)
        Log.d("UNIT","s")
        val allDistance = requireActivity().getSharedPreferences("state", Service.MODE_PRIVATE)
            .getInt(MyLocationConstants.DISTANCE, 0)
        with(binding) {
            unit=SharedData.fromUnit
            setBackgroundColor()
            this!!.txtKm3.text = if (SharedData.toUnit != "km/h") "mi" else "km"
            binding!!.txtKm4.text = SharedData.toUnit
            FontUtils.setFont(requireContext(), this.txtSpeed)
            SharedData.currentSpeedLiveData.observe(viewLifecycleOwner) {
                if (it == 0F) txtSpeed.text = "000"
                when (String.format("%.0f", it).length) {
                    1 -> {
                        txtSpeed.text = "00" + SharedData.convertSpeed(it).toInt()
                    }

                    2 -> {
                        txtSpeed.text = "0" + SharedData.convertSpeed(it).toInt()
                    }

                    else -> txtSpeed.text = SharedData.convertSpeed(it).toInt().toString()
                }

            }
            SharedData.distanceLiveData.observe(viewLifecycleOwner) {
                txtDistance1.text = (allDistance + it).toInt().toString()
            }
            txtDistance1.text = SharedData.convertSpeed(allDistance.toFloat()).toInt().toString()
            txtKm4.text = SharedData.toUnit
        }
    }

    override fun onResume() {
        setDataWhenComBack()
        setBackgroundColor()
        super.onResume()

    }

    @SuppressLint("SetTextI18n")
    private fun setDataWhenComBack() {

        if (SharedData.toUnit != unit) {
             with(binding) {
                this!!.txtKm3.text = if (SharedData.toUnit != "km/h") "mi" else "km"
                txtKm4.text = SharedData.toUnit
                 when (txtSpeed.text.length) {
                     1 -> {
                         txtSpeed.text = "00" + SharedData.convertSpeed(txtSpeed.text.toString().toFloat()).toInt()
                     }
                     2 -> {
                         txtSpeed.text ="0" + SharedData.convertSpeed(txtSpeed.text.toString().toFloat()).toInt()
                     }
                     else -> txtSpeed.text = "" + SharedData.convertSpeed(txtSpeed.text.toString().toFloat()).toInt()
                 }

                this.txtDistance1.text =
                    SharedData.convertSpeed(txtDistance1.text.toString().toFloat()).toInt()
                        .toString()
            }
            unit=SharedData.toUnit
        }
    }

    private fun setBackgroundColor() {
        val intColor = requireActivity().getSharedPreferences(
            SettingConstants.SETTING,
            Service.MODE_PRIVATE
        ).getInt(SettingConstants.COLOR_DISPLAY, 2)
        FontUtils.setTextColor(intColor, binding!!.txtSpeed, binding!!.txtKm4)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}