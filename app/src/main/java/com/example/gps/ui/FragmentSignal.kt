package com.example.gps.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.gps.R
import com.example.gps.databinding.FragmentSignalBinding
import com.example.gps.interfaces.SignalInterface


class FragmentSignal : Fragment(R.layout.fragment_signal), SignalInterface {
    private lateinit var binding: FragmentSignalBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentSignalBinding.bind(view)
        super.onViewCreated(binding.root, savedInstanceState)
        getCurrenBattery()
        (requireActivity() as MainActivity2).onStrengthGPSDataReceived(0, 0)
    }

    private fun getCurrenBattery() {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context?.registerReceiver(null, ifilter)
        }
        val batteryPct: Int? = batteryStatus?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level * 100 / scale
        }
        if (batteryPct != null) {
            onBatteryDataReceived(batteryPct)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBatteryDataReceived(int: Int) {
        if (this::binding.isInitialized) {
            binding.pin.text = "$int%"
        }
    }

    override fun onStrengthGPSDataReceived(strength: Int, satelliteCount: Int) {
        if (this::binding.isInitialized) {
            binding.txtStrengthGPS.text = "${strength}/${satelliteCount}"
            Log.d(
                "okokokdasd",
                (((strength / satelliteCount) * 100).toString()) + "$strength  $satelliteCount"
            )
            binding.txtStrengthNetwork.text =
                "${((strength.toFloat() / satelliteCount) * 100).toInt()}%"
        }

    }
}