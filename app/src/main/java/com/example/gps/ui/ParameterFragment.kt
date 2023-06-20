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
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.gp.NotificationsFragment
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

class ParameterFragment : Fragment(R.layout.fragment_parameter),
    ParameterContracts.View {
    private lateinit var binding: FragmentParameterBinding
    private lateinit var presenter: ParameterPresenter
    private var check = false
    private var sharedPreferences: SharedPreferences? = null

    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
            if (granted.entries.all { it.value }) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (granted[Manifest.permission.ACCESS_BACKGROUND_LOCATION] == true) {
                        presenter.startService()
                    } else {
                        checkBackgroundLocationPermission()
                    }
                }else{
                    presenter.startService()
                }

            } else {
                Toast.makeText(
                    requireContext(),
                    "Bạn không thể sử dụng chức năng nếu không cấp quyền",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    private fun checkBackgroundLocationPermission() {
        permissionsLauncher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))

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
        presenter.timeStart()
        SharedData.color.observe(viewLifecycleOwner) {
            with(binding) {
                if (it != 0) {
                    val colorStateList = ColorStateList.valueOf(ColorUtils.checkColor(it))
                    txtDistance.setTextColor(colorStateList)
                    txtAvgSpeed.setTextColor(colorStateList)
                    txtMaxSpeed.setTextColor(colorStateList)
                    txtStartTime.setTextColor(colorStateList)
                    btnStart.backgroundTintList = colorStateList
                    btnStop.backgroundTintList = colorStateList
                    imgPause.imageTintList = colorStateList
                    imgReset.imageTintList = colorStateList
                    imgResume.imageTintList = colorStateList
                }
            }
        }



        if (!presenter.isMyServiceRunning(MyService::class.java)) {
            presenter.setState(
                MyLocationConstants.STOP
            );
        }
        handleOrientationClickAll()

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun handleOrientationClickAll() {
        binding.btnStart.setOnClickListener {
            if (!CheckPermission.hasLocationPermission(requireContext())) {
                permissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    )
                )
            } else {

                presenter.startService()
            }

        }
        binding.btnStop.setOnClickListener {
            presenter.stopService()
            (requireActivity() as MainActivity2).sendDataToSecondFragment()
            val notificationsFragment =
                (requireActivity() as MainActivity2).supportFragmentManager.findFragmentByTag("f2")
            if (notificationsFragment != null) (notificationsFragment as NotificationsFragment).onClearMap(
                false
            )

        }
        binding.imgPause.setOnClickListener {
            presenter.pauseService()
        }
        binding.imgResume.setOnClickListener {
            presenter.resumeService()
        }
        binding.imgReset.setOnClickListener {
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
    }

    private fun setFont() {
        with(binding) {
            FontUtils.setFont(
                requireContext(), txtDistance, txtAvgSpeed, txtMaxSpeed, txtStartTime
            )
        }
    }


    override fun onShowMaxSpeed(string: String) {
        binding.txtMaxSpeed.text = string
        setFont()
    }

    override fun onShowDistance(string: String) {

        binding.txtDistance.text = string
        setFont()
    }

    override fun onShowAverageSpeed(string: String) {
        binding.txtAvgSpeed.text = string
        setFont()
    }

    override fun onHideStart() {
        binding.btnStart.visibility = View.INVISIBLE
    }

    override fun onHideStop() {
        binding.btnStop.visibility = View.INVISIBLE

    }

    override fun onHideReset() {
        binding.imgReset.visibility = View.INVISIBLE

    }

    override fun onHideResume() {
        binding.imgResume.visibility = View.INVISIBLE

    }

    override fun onHidePause() {
        binding.imgPause.visibility = View.INVISIBLE

    }

    override fun onShowStart() {
        binding.btnStart.visibility = View.VISIBLE

    }

    override fun onShowStop() {
        binding.btnStop.visibility = View.VISIBLE

    }

    override fun onShowReset() {
        binding.imgReset.visibility = SharedData.onShowResetButton.value!!

    }
    override fun onShowReset(int: Int) {
        binding.imgReset.isEnabled = int == View.VISIBLE
        binding.imgReset.visibility = int
    }
    override fun onTimeStart(s: String) {
        binding.txtStartTime.text = s
    }

    override fun onShowResume() {
        binding.imgResume.visibility = View.VISIBLE

    }


    override fun onShowPause() {
        binding.imgPause.visibility = View.VISIBLE

    }
}