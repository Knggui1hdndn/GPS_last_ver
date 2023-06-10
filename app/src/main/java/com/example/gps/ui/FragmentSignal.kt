package com.example.gps.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.gps.R
import com.example.gps.databinding.FragmentSignalBinding


class FragmentSignal : Fragment(R.layout.fragment_signal) {
    private lateinit var binding: FragmentSignalBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentSignalBinding.bind(view)
        super.onViewCreated(binding.root, savedInstanceState)
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        (requireContext().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager).registerGnssStatusCallback(
            object : GnssStatus.Callback() {
                @SuppressLint("MissingPermission")
                override fun onSatelliteStatusChanged(status: GnssStatus) {
                    val satelliteCount = status.satelliteCount
                    var totalSignalStrength = 0.0
                    for (i in 0 until satelliteCount) {
                        val cn0DbHz = status.getCn0DbHz(i)
                        // Xử lý dữ liệu cường độ tín hiệu vệ tinh ở đây
                        totalSignalStrength += cn0DbHz
                    }
                    val averageSignalStrength = totalSignalStrength / satelliteCount
                    binding.txtStrengthGPS.text = "${averageSignalStrength.toInt()}/${satelliteCount}"
                    binding.txtStrengthNetwork.text="${ ((averageSignalStrength/satelliteCount)*100).toInt()}%"
                }
            }, Handler(
                Looper.getMainLooper()!!
            )
        )
    }
}