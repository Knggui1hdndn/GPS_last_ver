package com.gps.speedometer.odometer.gpsspeedtracker.ui



import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
  import com.gps.speedometer.odometer.gpsspeedtracker.interfaces.MapInterface
import com.gps.speedometer.odometer.gpsspeedtracker.interfaces.MeasurementInterFace
import com.gps.speedometer.odometer.gpsspeedtracker.presenter.NotificationPresenter
import com.gps.speedometer.odometer.gpsspeedtracker.`object`.SharedData
import com.gps.speedometer.odometer.gpsspeedtracker.presenter.MeasurementPresenter
import com.gps.speedometer.odometer.gpsspeedtracker.utils.ColorUtils
import com.gps.speedometer.odometer.gpsspeedtracker.utils.FontUtils
import com.gps.speedometer.odometer.gpsspeedtracker.utils.MapUtils
import com.gps.speedometer.odometer.gpsspeedtracker.utils.TimeUtils
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment

import com.gps.speedometer.odometer.gpsspeedtracker.R
import com.gps.speedometer.odometer.gpsspeedtracker.databinding.FragmentNotificationsBinding
import kotlin.properties.Delegates


class NotificationsFragment : Fragment(R.layout.fragment_notifications), MapInterface.View,
    MeasurementInterFace.View {
    private var mapFragment: SupportMapFragment? = null
    private lateinit var presenter: NotificationPresenter
    private lateinit var googleMap: GoogleMap
    private var binding: FragmentNotificationsBinding? = null
    private var check by Delegates.notNull<Boolean>()

    @SuppressLint("SuspiciousIndentation", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentNotificationsBinding.bind(view)
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        check=  requireActivity().resources.configuration.orientation!= Configuration.ORIENTATION_LANDSCAPE
        presenter = NotificationPresenter(this, mapFragment!!)
        presenter.setUpMap()
        val measurement = MeasurementPresenter(this, this)
        measurement.colorChange()
        measurement.timeChange()
        measurement.currentSpeedChange()
        measurement.setVisibilityTime()
        if(binding!!.bannerContainer!=null)   (requireActivity() as MainActivity2).showBannerAds(
            binding!!.bannerContainer!!)


        with(binding) {
            this!!.imgCurrent.setOnClickListener {
                presenter.getCurrentPosition()
            }
          if (check)  FontUtils.setFont(requireContext(), binding?.time, binding?.speed)
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

    override fun onClearMap(boolean: Boolean) {
         if(boolean) presenter.checkShowPolyLine() else  googleMap.clear();presenter.checkShowPolyLine()
    }

    override fun onShowCurrentSpeed(string: String) {
        binding!!.speed!!.text = string

    }

    override fun onMoveCamera() {
        (requireActivity() as MainActivity2).binding.viewPager2.isUserInputEnabled = false
    }

    override fun onCameraIdle() {
        (requireActivity() as MainActivity2).binding.viewPager2.isUserInputEnabled = true
    }

    override fun onVisibilityTime(visibility: Int) {
        binding?.time?.visibility=visibility
    }

    override fun displayTimeChange(long: Long) {
        binding?.time?.text = TimeUtils.formatTime(long)
    }

    override fun displayColorChange(int: Int) {
        binding!!.time?.setTextColor(if (ColorUtils.isThemeDark()) Color.BLACK else Color.WHITE)
        binding!!.time?.backgroundTintList = ColorStateList.valueOf(ColorUtils.checkColor(int))
        binding!!.speed?.backgroundTintList = ColorStateList.valueOf(ColorUtils.checkColor(int))
        binding!!.speed?.setTextColor(if (ColorUtils.isThemeDark()) Color.BLACK else Color.WHITE)
     }

    @SuppressLint("SetTextI18n")
    override fun displayCurrentSpeedChange(string: String, l: Long) {
        binding?.speed?.text = string + SharedData.toUnit
        if (check) FontUtils.setFont(requireContext(),binding?.speed)

    }
}

