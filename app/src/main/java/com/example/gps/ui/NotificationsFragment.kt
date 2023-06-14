package com.example.gps.ui

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.gps.R
import com.example.gps.databinding.FragmentNotificationsBinding
import com.example.gps.interfaces.MapInterface
import com.example.gps.presenter.NotificationPresenter
import com.example.gps.`object`.SharedData
import com.example.gps.utils.ColorUtils
import com.example.gps.utils.FontUtils
import com.example.gps.utils.MapUtils
import com.example.gps.utils.TimeUtils
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.PolylineOptions


class NotificationsFragment : Fragment(R.layout.fragment_notifications), MapInterface.View {
    private var mapFragment: SupportMapFragment? = null
    private lateinit var presenter: NotificationPresenter
    private lateinit var googleMap: GoogleMap
    private var polyline = PolylineOptions()
    private var binding: FragmentNotificationsBinding? = null

    @SuppressLint("SuspiciousIndentation", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentNotificationsBinding.bind(view)
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        presenter = NotificationPresenter(this, mapFragment!!)
        presenter.setUpMap()

        with(binding) {
            SharedData.currentSpeedLiveData.observe(viewLifecycleOwner) {
                it[it.keys.first()]?.let { it1 ->
                    this!!.speed?.text  =
                        if (it1 <= 0) "0000"+SharedData.toUnit else "%04d".format(
                            SharedData.convertSpeed(
                                it1.toDouble()
                            ).toInt()
                        )+SharedData.toUnit
                }
                FontUtils.setFont(requireContext(), this!!.time, speed)
            }
            SharedData.color.observe(viewLifecycleOwner) {
                this!!.time?.setTextColor(ColorUtils.checkColor(it))
                speed?.setTextColor(ColorUtils.checkColor(it))
                FontUtils.setFont(requireContext(), this!!.time, speed)

            }
            this!!.imgCurrent.setOnClickListener {
                presenter.getCurrentPosition()
            }

            imgRotate.setOnClickListener {
                requireActivity().requestedOrientation =
                    if (requireActivity().requestedOrientation === ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }

            imgTypeMap.setOnClickListener {
                MapUtils.setStStyleMap(googleMap)
            }

        }

        SharedData.time.observe(viewLifecycleOwner) {
            binding!!.time?.text = TimeUtils.formatTime(it)
        }
    }

    override fun onVisibilityPolyLine(boolean: Boolean) {

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
}

//    override fun onColorChange(i: Int) {
//        with(binding) {
//            FontUtils.setTextColor(
//                i,
//                this!!.longitude,
//                latitude,
//                txtSpeed,
//            )
//        }
//    }
//
//    @SuppressLint("SetTextI18n")
//    override fun onUnitChange() {
//        binding?.txtSpeed?.text = "${
//            SharedData.convertSpeed(
//                SharedData.currentSpeedLiveData.value!!.keys.first()
//            )
//        }+${SharedData.toUnit}"
//        FontUtils.setFont(requireContext(), binding?.txtSpeed!!)
//    }
