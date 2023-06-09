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
import androidx.appcompat.app.AppCompatDelegate
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
        val positionsColor = sharedPreferencesSetting.getInt(SettingConstants.COLOR_DISPLAY, 2)
        binding.speed.backgroundCircleColor =
            if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES ) Color.WHITE else Color.BLACK
        binding.speed.speedTextColor =
            if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) Color.WHITE else Color.BLACK
        binding.speed.textColor =
            if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) Color.WHITE else Color.BLACK
        onColorChange(positionsColor)
        with(binding) {

            if (requireContext().resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                onVisibilityChanged(
                    sharedPreferencesSetting.getBoolean(
                        SettingConstants.SHOW_RESET_BUTTON,
                        true
                    )
                )

                imgRotateScreen!!.setOnClickListener {
                    requireActivity().requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                }
                imgReset!!.setOnClickListener {
                    stopRunning()
                }
            } else {
                sharedPreferencesSetting.getInt(SettingConstants.COLOR_DISPLAY, 2)
            }

            SharedData.currentSpeedLiveData.observe(viewLifecycleOwner) {
                it[it.keys.first()]?.let { it1 ->
                    this.speed.speedTo(
                        SharedData.convertSpeed(it.keys.first()).toFloat(),
                        it1 / 1000
                    )
                }
            }
        }
    }

    private fun getVehicleChecked(): Vehicle? {
        return try {
            myDataBase.vehicleDao().getVehicleChecked()
        } catch (e: Exception) {
            return null
        }
    }


    private fun stopRunning() {
        val status = sharedPreferencesState.getString(MyLocationConstants.STATE, null)
        if (status != MyLocationConstants.STOP && status != null) {
            (childFragmentManager.findFragmentById(R.id.frag) as ParameterFragment).hideBtnStop()
            val intent = Intent(requireContext(), MyService::class.java)
            intent.action = MyLocationConstants.STOP
            requireActivity().startService(intent)

        }
    }

    override fun onVisibilityChanged(boolean: Boolean) {
        binding.imgReset!!.visibility = if (!boolean) View.GONE else View.VISIBLE
    }

    override fun onMaxSpeedAnalogChange(speed: Int) {
        binding.speed.maxSpeed = speed.toFloat()
    }

    override fun setSpeedAndUnit() {
        try {
            binding.speed.maxSpeed = getVehicleChecked()?.clockSpeed?.toFloat()!!
            binding.speed.unit = SharedData.toUnit
        } catch (e: Exception) {
            Log.d("okkkkkk", e.toString())
        }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun toggleButtonVisibility(boolean: Boolean) {
        binding.imgReset?.visibility = if (boolean) View.VISIBLE else View.GONE
    }

    override fun onColorChange(i: Int) {
        try {
            binding.speed.speedTextColor = ColorUtils.checkColor(i)
            binding.speed.textColor = ColorUtils.checkColor(i)
            binding.speed.trianglesColor = ColorUtils.checkColor(i)
            binding.speed.unitTextColor = ColorUtils.checkColor(i)
            binding.speed.setSpeedometerColor(ColorUtils.checkColor(i))

        } catch (e: Exception) {
        }
    }

    override fun onUnitChange() {
        setSpeedAndUnit()
    }
}