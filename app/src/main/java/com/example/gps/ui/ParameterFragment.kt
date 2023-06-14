package com.example.gps.ui


import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.gps.constants.MyLocationConstants
import com.example.gps.R
import com.example.gps.constants.SettingConstants
import com.example.gps.`object`.SharedData
import com.example.gps.dao.MyDataBase
import com.example.gps.databinding.FragmentParameterBinding
import com.example.gps.interfaces.MeasurementInterFace
import com.example.gps.interfaces.ParameterContracts
import com.example.gps.model.MovementData
import com.example.gps.presenter.ParameterPresenter
import com.example.gps.service.MyService
import com.example.gps.`object`.CheckPermission
import com.example.gps.utils.ColorUtils
import com.example.gps.utils.FontUtils
import com.example.gps.utils.StringUtils
import com.example.gps.utils.TimeUtils

class ParameterFragment : Fragment(R.layout.fragment_parameter) ,
    ParameterContracts.View {
    private lateinit var binding: FragmentParameterBinding
    private lateinit var presenter: ParameterPresenter
    private var intColor: Int = 0
    private var check = false
    private var sharedPreferences: SharedPreferences? = null
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
            if (granted.entries.all { it.value }) {
                if (granted.get(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == true) presenter.startService() else check()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Bạn không thể sử dụng chức năng nếu không cấp quyền",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    fun check() {
        resultLauncher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("SetTextI18n", "RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentParameterBinding.bind(view)
        sharedPreferences = requireActivity().getSharedPreferences("state", Service.MODE_PRIVATE)
        presenter = ParameterPresenter(this, this)
        presenter.updateUIState()
        presenter.getDistance()
        presenter.getAverageSpeed()
        presenter.getMaxSpeed()
        SharedData.color.observe(viewLifecycleOwner) {
            with(binding){
                txtDistance.setTextColor(ColorUtils.checkColor(it))
                txtAvgSpeed.setTextColor(ColorUtils.checkColor(it))
                txtMaxSpeed.setTextColor(ColorUtils.checkColor(it))
                txtStartTime.setTextColor(ColorUtils.checkColor(it))
                btnStart.backgroundTintList= ColorStateList.valueOf(ColorUtils.checkColor(it))
                btnStop.backgroundTintList= ColorStateList.valueOf(ColorUtils.checkColor(it))
                imgPause.imageTintList= ColorStateList.valueOf(ColorUtils.checkColor(it))
                imgReset.imageTintList= ColorStateList.valueOf(ColorUtils.checkColor(it))
                imgResume.imageTintList= ColorStateList.valueOf(ColorUtils.checkColor(it))
            }
        }



        if (!presenter.isMyServiceRunning(MyService::class.java)) presenter.setState(
            MyLocationConstants.STOP
        );

        handleOrientationClickAll()
        SharedData.toUnit = "km/h"
        SharedData.toUnitDistance = "km"
    }

    private fun handleOrientationClickAll() {
        binding.btnStart?.setOnClickListener {
            if (!CheckPermission.hasLocationPermission(requireContext())) {
                resultLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    )
                )
            } else {
                presenter.startService()
            }

        }
        binding.btnStop?.setOnClickListener {
            presenter.stopService()
            (requireActivity() as MainActivity2).sendDataToSecondFragment()
        }
        binding.imgPause?.setOnClickListener {
            presenter.pauseService()
        }
        binding.imgResume?.setOnClickListener {
            presenter.resumeService()
        }
        binding.imgReset?.setOnClickListener {
            presenter.stopService()
        }
    }

    override fun onResume() {
        presenter.updateUIState()
        super.onResume()
    }


    override fun onPause() {
        super.onPause()

        check = false
        if (SharedData.checkService) {

        }
    }

    private fun setFont() {
        with(binding) {
            FontUtils.setFont(
                requireContext(), txtDistance, txtAvgSpeed, txtMaxSpeed, txtStartTime
            )
        }
    }






    override fun showMaxSpeed(string: String) {
        binding.txtMaxSpeed?.text = string
        setFont()
    }

    override fun showDistance(string: String) {

        binding.txtDistance?.text = string
        setFont()
    }

    override fun showAverageSpeed(string: String) {
        binding.txtAvgSpeed?.text = string
        setFont()
    }

    override fun hideStart() {
        binding.btnStart.visibility = View.INVISIBLE
    }

    override fun hideStop() {
        binding.btnStop.visibility = View.INVISIBLE

    }

    override fun hideReset() {
        binding.imgReset.visibility = View.INVISIBLE

    }

    override fun hideResume() {
        binding.imgResume.visibility = View.INVISIBLE

    }

    override fun hidePause() {
        binding.imgPause.visibility = View.INVISIBLE

    }

    override fun showStart() {
        binding.btnStart.visibility = View.VISIBLE

    }

    override fun showStop() {
        binding.btnStop.visibility = View.VISIBLE

    }

    override fun showReset() {
        binding.imgReset.visibility = View.VISIBLE

    }

    override fun showResume() {
        binding.imgResume.visibility = View.VISIBLE

    }

    override fun showPause() {
        binding.imgPause.visibility = View.VISIBLE

    }


}