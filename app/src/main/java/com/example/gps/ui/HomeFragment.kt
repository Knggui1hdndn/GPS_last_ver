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
import com.example.gps.interfaces.HomeInterface
import com.example.gps.model.Vehicle
import com.example.gps.service.MyService
import com.example.gps.utils.ColorUtils
import com.example.gps.utils.UnitUtils


class HomeFragment : Fragment(R.layout.fragment_home), HomeInterface {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var sharedPreferencesSetting: SharedPreferences
    private lateinit var sharedPreferencesState: SharedPreferences
    private lateinit var myDataBase: MyDataBase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentHomeBinding.bind(view)
        myDataBase = MyDataBase.getInstance(requireContext())
        sharedPreferencesSetting =
            requireContext().getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE)
        sharedPreferencesState = requireContext().getSharedPreferences("state", MODE_PRIVATE)

        with(binding) {
            imgRotateScreen!!.setOnClickListener {
                requireActivity().requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }

            imgReset!!.setOnClickListener {
                stopRunning()
            }
            SharedData.currentSpeedLiveData.observe(viewLifecycleOwner) {
                it[it.keys.first()]?.let { it1 -> this.speed.speedTo(it.keys.first(), it1 * 1000) }
            }
            setSpeedAndUnit()
        }
    }

    private fun getVehicleChecked(): Vehicle {
        return myDataBase.vehicleDao()
            .getVehicleChecked(myDataBase.SpeedDao().getChecked().type)
    }

    private fun stopRunning() {
        val status = sharedPreferencesState.getString(MyLocationConstants.STATE, null)
        if (status != MyLocationConstants.STOP && status != null) {
            val intent = Intent(requireContext(), MyService::class.java)
            intent.action = MyLocationConstants.STOP
            requireActivity().startService(intent)
        }
    }

    override fun onVisibilityChanged(boolean: Boolean) {
        binding.imgReset!!.visibility = if (boolean) View.GONE else View.VISIBLE
    }

    override fun onMaxSpeedAnalogChange(speed: Int) {
        binding.speed.unit
    }

    override fun setSpeedAndUnit() {
        try {
            binding.speed.maxSpeed = getVehicleChecked().clockSpeed.toFloat()
            binding.speed.unit = UnitUtils.getUnit(myDataBase.SpeedDao().getChecked().type)
        } catch (e: Exception) {

        }
    }

    override fun onColorChange() {
        val positionsColor = sharedPreferencesSetting.getInt(SettingConstants.COLOR_DISPLAY, 2)
        if (positionsColor == 1) binding.speed.trianglesColor = Color.BLUE
        binding.speed.setSpeedometerColor(ColorUtils.checkColor(positionsColor));
    }

    override fun onUnitChange() {
        setSpeedAndUnit()
    }
}