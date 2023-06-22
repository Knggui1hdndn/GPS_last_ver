package com.gps.speedometer.odometer.gpsspeedtracker.ui

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
  import com.gps.speedometer.odometer.gpsspeedtracker.interfaces.SignalInterface
import com.gps.speedometer.odometer.gpsspeedtracker.`object`.CheckPermission
import com.gps.speedometer.odometer.gpsspeedtracker.presenter.SignalPresenter
import com.gps.speedometer.odometer.gpsspeedtracker.R
import com.gps.speedometer.odometer.gpsspeedtracker.databinding.FragmentSignalBinding


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