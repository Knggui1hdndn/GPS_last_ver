package com.example.gps.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gps.R
import com.example.gps.databinding.FragmentSignalBinding
import com.example.gps.interfaces.SignalInterface
import com.example.gps.`object`.CheckPermission
import com.example.gps.presenter.SignalPresenter


class FragmentSignal : Fragment(R.layout.fragment_signal), SignalInterface.View {
    private lateinit var binding: FragmentSignalBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentSignalBinding.bind(view)
        super.onViewCreated(binding.root, savedInstanceState)
        val presenter = SignalPresenter(this, requireContext())
        binding.imgRotatesa.setOnClickListener {
            requireActivity().requestedOrientation =ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

       if(CheckPermission.hasLocationPermission(requireContext()))  presenter.registerGnssStatusCallback()

    }


    @SuppressLint("SetTextI18n")
    override fun onStrengthGPSDataReceived(strength: Int, satelliteCount: Int) {
        if (this::binding.isInitialized) {
            binding.txtStrengthGPS.text = "${strength}/${satelliteCount}"
            binding.txtStrengthNetwork.text =
                "${((strength.toFloat() / satelliteCount) * 100).toInt()}%"
        }

    }
}