package com.example.gps.ui


import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gps.MyLocationConstants
import com.example.gps.R
import com.example.gps.SettingConstants
import com.example.gps.SharedData
import com.example.gps.dao.MyDataBase
import com.example.gps.databinding.FragmentParameterBinding
import com.example.gps.service.MyService
import com.example.gps.utils.ColorUtils
import com.example.gps.utils.FontUtils

class ParameterFragment : Fragment(R.layout.fragment_parameter) {
    private lateinit var binding: FragmentParameterBinding
    private var intColor: Int = 0
    private var check = false
    private var sharedPreferences: SharedPreferences? = null
    private var unit: Int = 0
    private lateinit var myDataBase: MyDataBase
    var checkUnit = ""

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentParameterBinding.bind(view)
        sharedPreferences = requireActivity().getSharedPreferences("state", Service.MODE_PRIVATE)
        myDataBase = MyDataBase.getInstance(requireContext())
        checkUnit = SharedData.toUnit
        if (!isMyServiceRunning(MyService::class.java)) setState(MyLocationConstants.STOP)
        with(binding) {
            setBackGround()
            this.txtMaxSpeed.text = "0" + SharedData.toUnit
            this.txtDistance.text = "0" + if (SharedData.toUnit != "km/h") "mi" else "km"
            this.txtAverageSpeed.text = "0" + SharedData.toUnit
            SharedData.maxSpeedLiveData.observe(viewLifecycleOwner) {
                this.txtMaxSpeed.text = if (it <= 0) "0" + SharedData.toUnit else String.format(
                    "%.0f",
                    SharedData.convertSpeed(it)
                ) + SharedData.toUnit
                setFont(binding)
            }


            SharedData.distanceLiveData.observe(viewLifecycleOwner) {
                this.txtDistance.text =
                    if (SharedData.toUnit != "km/h") "${(it * 0.6214).toInt()}mi" else "${(it * 1.60934).toInt()}km"
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

            showOrHideView()
            setFont(binding)
            this.btnStart.setOnClickListener {
                setState(MyLocationConstants.START)
                hideBtnStart()
                startService(MyLocationConstants.START)
            }

            btnResume.setOnClickListener {
                setState(MyLocationConstants.RESUME)
                hideBtnResume()
                startService(MyLocationConstants.RESUME)
            }
            btnPause.setOnClickListener {
                setState(MyLocationConstants.PAUSE)
                hideBtnPause()
                startService(MyLocationConstants.PAUSE)
            }
            btnStop.setOnClickListener {
                setState(MyLocationConstants.STOP)
                hideBtnStop()
                startService(MyLocationConstants.STOP)


            }
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
            this!!.btnStart.visibility = View.GONE
            mframeLayout.visibility = View.VISIBLE
            btnStop.visibility = View.VISIBLE
        }
    }

    private fun hideBtnPause() {
        with(binding) {
            this!!.btnStart.visibility = View.GONE
            btnPause.visibility = View.GONE
            btnResume.visibility = View.VISIBLE
            mframeLayout.visibility = View.VISIBLE
            btnStop.visibility = View.VISIBLE
        }
    }

    private fun hideBtnResume() {
        with(binding) {
            this!!.btnStart.visibility = View.GONE
            btnPause.visibility = View.VISIBLE
            btnResume.visibility = View.GONE
            mframeLayout.visibility = View.VISIBLE
            btnStop.visibility = View.VISIBLE
        }
    }

    private fun hideBtnStop() {
        with(binding) {
            this!!.btnStart.visibility = View.VISIBLE
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

    override fun onResume() {
        super.onResume()
        setBackgroundWhenComeBack()
        setDataWhenComeBack()
    }

    private fun setDataWhenComeBack() {
        if (SharedData.toUnit != checkUnit) {
            with(binding) {
                numberSeparation(txtDistance)
                val distance = numberSeparation(txtDistance)
                this.txtDistance.text =
                    if (SharedData.toUnit != "km/h") "${(distance * 0.6214).toInt()}mi" else "${(distance * 1.60934).toInt()}km"
                var averageSpeed = numberSeparation(txtAverageSpeed)
                txtAverageSpeed.text =
                    if (averageSpeed != 0) SharedData.convertSpeed(averageSpeed.toFloat()).toInt()
                        .toString() + SharedData.toUnit else "0${SharedData.toUnit}"
                var speed = numberSeparation(txtMaxSpeed)
                this.txtMaxSpeed.text =
                    if (speed != 0) SharedData.convertSpeed(speed.toFloat()).toInt()
                        .toString() + SharedData.toUnit else "0${SharedData.toUnit}"
            }
            setFont(binding)
            checkUnit = SharedData.toUnit
        }
    }

    private fun setBackgroundWhenComeBack() {
        val i = requireActivity().getSharedPreferences(
            SettingConstants.SETTING,
            Context.MODE_PRIVATE
        ).getInt(SettingConstants.COLOR_DISPLAY, 2)
        if ((myDataBase.SpeedDao().getChecked() != null)) {
            unit = myDataBase.SpeedDao().getChecked().type
        }
        if (intColor != i) {
            with(binding) {
                FontUtils.setTextColor(i, this.txtMaxSpeed, txtAverageSpeed, txtDistance)
                btnStart.setBackgroundColor(ColorUtils.checkColor(i))
                btnPause.setBackgroundColor(ColorUtils.checkColor(i))
                btnResume.setBackgroundColor(ColorUtils.checkColor(i))
            }
        }
    }

    private fun numberSeparation(txt: TextView): Int {
        return txt.text.toString().filter { it.isDigit() }.toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(requireContext(), "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(requireContext(), "portrait", Toast.LENGTH_SHORT).show();
        }

    }
}