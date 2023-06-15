package com.example.gp

import com.example.gps.ui.MainActivity2


import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.gps.R
import com.example.gps.databinding.FragmentNotificationsBinding
import com.example.gps.interfaces.MapInterface
import com.example.gps.interfaces.MeasurementInterFace
import com.example.gps.presenter.NotificationPresenter
import com.example.gps.`object`.SharedData
import com.example.gps.presenter.MeasurementPresenter
import com.example.gps.utils.ColorUtils
import com.example.gps.utils.FontUtils
import com.example.gps.utils.MapUtils
import com.example.gps.utils.TimeUtils
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.PolylineOptions


class NotificationsFragment : Fragment(R.layout.fragment_notifications), MapInterface.View,
    MeasurementInterFace.View {
    private var mapFragment: SupportMapFragment? = null
    private lateinit var presenter: NotificationPresenter
    private lateinit var googleMap: GoogleMap
    private var binding: FragmentNotificationsBinding? = null

    @SuppressLint("SuspiciousIndentation", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentNotificationsBinding.bind(view)
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        presenter = NotificationPresenter(this, mapFragment!!)
        presenter.setUpMap()
        val measurement = MeasurementPresenter(this, this)
        measurement.onColorChange()
        measurement.onTimeChange()
        measurement.onCurrentSpeedChange()

        with(binding) {

            this!!.imgCurrent.setOnClickListener {
                presenter.getCurrentPosition()
            }

            imgRotate.setOnClickListener {
                requireActivity().requestedOrientation =
                    if (requireActivity().requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }

            imgTypeMap.setOnClickListener {
                MapUtils.setStStyleMap(googleMap)
            }

        }
    }


    override fun setMap(googleMap: GoogleMap) {
        this.googleMap = googleMap
    }

    override fun clearMap() {
        googleMap.clear()
    }

    override fun showCurrentSpeed(string: String) {
        binding!!.speed!!.text = string
    }

    override fun onMoveCamera() {
        (requireActivity() as MainActivity2).binding.viewPager2.isUserInputEnabled = false
    }

    override fun onCameraIdle() {
        (requireActivity() as MainActivity2).binding.viewPager2.isUserInputEnabled = true
    }

    override fun displayTimeChange(long: Long) {
        binding?.time?.text = TimeUtils.formatTime(long)
        FontUtils.setFont(requireContext(), binding?.time, binding?.speed)

    }

    override fun displayColorChange(int: Int) {
        binding!!.time?.setTextColor(ColorUtils.checkColor(int))
        binding!!.speed?.setTextColor(ColorUtils.checkColor(int))
        FontUtils.setFont(requireContext(), binding?.time, binding?.speed)
    }

    override fun displayCurrentSpeedChange(string: String, l: Long) {
        binding?.speed?.text = string + SharedData.toUnit
        FontUtils.setFont(requireContext(), binding?.time, binding?.speed)
    }
}

