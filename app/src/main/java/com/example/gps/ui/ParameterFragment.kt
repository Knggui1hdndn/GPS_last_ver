package com.example.gps.ui


import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.gps.MyLocationConstants
import com.example.gps.R
import com.example.gps.SettingConstants
import com.example.gps.SharedData
import com.example.gps.dao.MyDataBase
import com.example.gps.databinding.FragmentParameterBinding
import com.example.gps.interfaces.MeasurementInterFace
import com.example.gps.model.MovementData
import com.example.gps.service.MyService
import com.example.gps.ui.setting.Setting
import com.example.gps.utils.ColorUtils
import com.example.gps.utils.FontUtils
import com.example.gps.utils.StringUtils
import com.example.gps.utils.TimeUtils

class ParameterFragment : Fragment(R.layout.fragment_parameter), MeasurementInterFace {
    private lateinit var binding: FragmentParameterBinding
    private var intColor: Int = 0
    private var check = false
    private var sharedPreferences: SharedPreferences? = null
    private lateinit var myDataBase: MyDataBase
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
            if (granted.entries.all { it.value }) {
                start()
                val fragment = checkFragmentNotification()
                if (fragment is NotificationsFragment) {
                    fragment.mapAsync()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Bạn không thể sử dụng chức năng nếu không cấp quyền",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    @SuppressLint("SetTextI18n", "RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentParameterBinding.bind(view)
        sharedPreferences = requireActivity().getSharedPreferences("state", Service.MODE_PRIVATE)
        myDataBase = MyDataBase.getInstance(requireContext())
        setBackGround()
        setTextDefault()
        setDataWhenComeBack()
        showOrHideView()
        setFont(binding)

        //set state is STOP when MyService not Running
        if (!isMyServiceRunning(MyService::class.java)) setState(MyLocationConstants.STOP);
        onDataChangeWithOrientationLandscape()
        onDataChangeWithOrientationPortrait()
        handleOrientationClickAll()

    }

    private fun handleOrientationClickAll() {
        binding.btnResume.setOnClickListener {
            setState(MyLocationConstants.RESUME)
            hideBtnResume()
            startService(MyLocationConstants.RESUME)
        }
        binding.btnPause.setOnClickListener {
            setState(MyLocationConstants.PAUSE)
            hideBtnPause()
            startService(MyLocationConstants.PAUSE)
        }
        binding.btnStop.setOnClickListener {
            val fragment = checkFragmentNotification()
            if (fragment is NotificationsFragment) {
                fragment.clear()

            }
            setState(MyLocationConstants.STOP)
            hideBtnStop()
            startService(MyLocationConstants.STOP)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onDataChangeWithOrientationPortrait() {
        with(binding) {
            SharedData.maxSpeedLiveData.observe(viewLifecycleOwner) {
                this.txtMaxSpeed.text = if (it <= 0) "0" + SharedData.toUnit else String.format(
                    "%.0f",
                    SharedData.convertSpeed(it)
                ) + SharedData.toUnit
                setFont(binding)
            }


            SharedData.distanceLiveData.observe(viewLifecycleOwner) {
                this.txtDistance.text =
                    String.format(
                        "%.2f",
                        SharedData.convertDistance(it)
                    ) + SharedData.toUnitDistance
                setFont(binding)
            }

            SharedData.averageSpeedLiveData.observe(viewLifecycleOwner) {
                this.txtAverageSpeed.text =
                    if (it <= 0) "0" + SharedData.toUnit else String.format(
                        "%.0f",
                        SharedData.convertSpeed(it)
                    ) + SharedData.toUnit
                setFont(binding)
            }

            this.btnStart.setOnClickListener {
                if (requireContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    requireContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
                    resultLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                } else {
                    start()
                }

            }
        }
    }

    private fun onDataChangeWithOrientationLandscape() {
        with(binding) {
            if (requireActivity().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                val fragment = checkFragmentNotification()
                if (fragment is NotificationsFragment) {
                    times!!.visibility = View.GONE
                }

                SharedData.time.observe(viewLifecycleOwner) {
                    times!!.text = TimeUtils.formatTime(it)
                }
                settings!!.setOnClickListener {
                    startActivity(Intent(requireActivity(), Setting::class.java))
                }
                stop!!.setOnClickListener {
                    val fragment = checkFragmentNotification()
                    if (fragment is NotificationsFragment) {
                        fragment.clear()
                    }
                    stopService()
                }
                imgRotateScreen1!!.setOnClickListener {
                    requireActivity().requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
                }
            }
        }
    }

    private fun checkFragmentNotification(): Fragment? {
        return (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main2) as NavHostFragment?)?.childFragmentManager?.fragments?.get(
            0
        )
    }

    private fun start() {
        (requireActivity() as MainActivity2).onStrengthGPSDataReceived(0, 0)
        insertMovementData()
        setState(MyLocationConstants.START)
        hideBtnStart()
        startService(MyLocationConstants.START)
    }


    @SuppressLint("SetTextI18n")
    private fun setTextDefault() {
        with(binding) {
            this.txtMaxSpeed.text = "0" + SharedData.toUnit
            this.txtDistance.text = "0" + SharedData.toUnitDistance
            this.txtAverageSpeed.text = "0" + SharedData.toUnit
        }
    }

    private fun insertMovementData() {
        myDataBase.movementDao().insertMovementData(
            MovementData(
                0,
                System.currentTimeMillis(),
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0
            )
        )
    }

    private fun stopService() {
        ((requireActivity() as MainActivity2).supportFragmentManager.fragments[0].childFragmentManager.findFragmentById(
            R.id.signal
        ) as FragmentSignal).onStrengthGPSDataReceived(0, 0)
        val status = requireContext().getSharedPreferences("state", Context.MODE_PRIVATE)
            .getString(MyLocationConstants.STATE, null)
        if (status != MyLocationConstants.STOP && status != null) {
            val intent = Intent(requireContext(), MyService::class.java)
            intent.action = MyLocationConstants.STOP
            requireActivity().startService(intent)
            SharedData.checkService = true
        }
    }

    fun toggleClockVisibilityLandscape(boolean: Boolean) {
        if (requireActivity().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.times?.visibility = if (boolean) View.VISIBLE else View.GONE
        }
    }

    private fun setBackGround() {
        intColor = requireActivity().getSharedPreferences(
            SettingConstants.SETTING,
            Context.MODE_PRIVATE
        ).getInt(SettingConstants.COLOR_DISPLAY, 2)
        with(binding) {
            FontUtils.setTextColor(intColor, this.txtMaxSpeed, txtAverageSpeed, txtDistance)

            btnStart.setBackgroundColor(ColorUtils.checkColor(intColor))
            btnPause.setBackgroundColor(ColorUtils.checkColor(intColor))
            btnResume.setBackgroundColor(ColorUtils.checkColor(intColor))
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager =
            requireActivity().getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun showOrHideView() {
        when (sharedPreferences?.getString(MyLocationConstants.STATE, null)) {
            MyLocationConstants.START -> {
                hideBtnStart()
            }

            MyLocationConstants.PAUSE -> {
                hideBtnPause()
            }

            MyLocationConstants.RESUME -> {
                hideBtnResume()
            }

            MyLocationConstants.STOP -> {
                hideBtnStop()
            }
        }
    }

    private fun hideBtnStart() {
        with(binding) {
            this.btnStart.visibility = View.GONE
            mframeLayout.visibility = View.VISIBLE
            btnStop.visibility = View.VISIBLE
        }
    }

    private fun hideBtnPause() {
        with(binding) {
            this.btnStart.visibility = View.GONE
            btnPause.visibility = View.GONE
            btnResume.visibility = View.VISIBLE
            mframeLayout.visibility = View.VISIBLE
            btnStop.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onStop() {
        super.onStop()
    }

    private fun hideBtnResume() {
        with(binding) {
            this.btnStart.visibility = View.GONE
            btnPause.visibility = View.VISIBLE
            btnResume.visibility = View.GONE
            mframeLayout.visibility = View.VISIBLE
            btnStop.visibility = View.VISIBLE
        }
    }

    fun hideBtnStop() {
        with(binding) {
            this.btnStart.visibility = View.VISIBLE
            mframeLayout.visibility = View.GONE
            btnStop.visibility = View.GONE
        }
    }


    private fun startService(action: String) {
        val intent = Intent(requireContext(), MyService::class.java)
        intent.action = action
        requireActivity().startService(intent)
    }

    private fun setState(state: String) {
        sharedPreferences!!.edit().putString(MyLocationConstants.STATE, state).apply()
    }


    override fun onPause() {
        super.onPause()
        check = false
        if (SharedData.checkService) {
            hideBtnStop();setState(MyLocationConstants.STOP);SharedData.checkService =
                false
        }
    }

    private fun setFont(binding: FragmentParameterBinding) {
        with(binding) {
            FontUtils.setFont(
                requireContext(), txtDistance, txtAverageSpeed, txtMaxSpeed
            )
        }
    }


    @SuppressLint("SetTextI18n")
    fun setDataWhenComeBack() {
        with(binding) {
            txtDistance.text =
                SharedData.convertDistance(SharedData.distanceLiveData.value!!).toInt()
                    .toString() + SharedData.toUnitDistance
            txtAverageSpeed.text = StringUtils.convert(SharedData.averageSpeedLiveData.value!!)
            txtMaxSpeed.text = StringUtils.convert(SharedData.maxSpeedLiveData.value!!)
            Log.d("okokkoo", SharedData.toUnitDistance + "sssssss" + SharedData.toUnit)
        }
        setFont(binding)

    }


    override fun onUnitChange() {
        setDataWhenComeBack()
    }

    override fun onColorChange(i: Int) {
        with(binding) {
            FontUtils.setTextColor(i, this.txtMaxSpeed, txtAverageSpeed, txtDistance)
            btnStart.setBackgroundColor(ColorUtils.checkColor(i))
            btnPause.setBackgroundColor(ColorUtils.checkColor(i))
            btnResume.setBackgroundColor(ColorUtils.checkColor(i))

        }
    }
}