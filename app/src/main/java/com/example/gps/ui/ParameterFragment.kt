package com.example.gps.ui


import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
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
    private var difference: Double = 1.0
    private var unit: Int = 0
    private var unitDistance: Int = 0
    private var unitCurrent: String = ""
    private lateinit var myDataBase: MyDataBase
    private var u = 0.0
    private var v = 0.0
    private var vMax = 0.0

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentParameterBinding.bind(view)
        sharedPreferences = requireActivity().getSharedPreferences("state", Service.MODE_PRIVATE)
        myDataBase = MyDataBase.getInstance(requireContext())
        unit = myDataBase.SpeedDao().getChecked().type


        if (!isMyServiceRunning(MyService::class.java)) setState(MyLocationConstants.STOP)
        with(binding) {
            setBackGround()

            SharedData.unitSpeed.observe(viewLifecycleOwner) {
                txtkm1.text = it.toString()
                txtkm3.text = it.toString()
                if (it != "km") txtkm2.text = "mi" else txtkm2.text = "km"
                convertKmTo(it)
                convertToKm()

            }
            SharedData.maxSpeedLiveData.observe(viewLifecycleOwner) {
                vMax = it.toDouble()
                this.txtMaxSpeed.text =
                    if (it <= 0) "0" else String.format(
                        "%.0f",
                        it
                    )
                setFont(binding)
            }
            SharedData.distanceLiveData.observe(viewLifecycleOwner) {
                this.txtDistance.text = String.format("%.2f", it)
                setFont(binding)

            }
            SharedData.averageSpeedLiveData.observe(viewLifecycleOwner) {
                v = it.toDouble()
                this.txtAverageSpeed.text =
                    if (it <= 0) "0" else String.format(
                        "%.0f",
                        it
                    ) + ""
                setFont(binding)

            }

            showOrHideView()
            setFont(binding!!)
            this!!.btnStart.setOnClickListener {
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

    private fun convertToKm() {
        when (unitCurrent) {
            "km" -> {
                v = v
            }

            "knot" -> {
                v *= 1.852
            }

            "mph" -> {
                v *= 1.609344
            }
        }
    }

    private fun convertKmTo(it: String):Int {
        with(binding) {
            when (it) {
                "km" -> {
                    txtAverageSpeed.text = v.toString()
                    txtMaxSpeed.text = vMax.toString()
                }

                "knot" -> {

                    txtAverageSpeed.text =
                        (v / 0.539957).toInt().toString()
                    txtMaxSpeed.text =
                        (vMax / 0.539957).toInt().toString()

                }

                "mph" -> {
                    txtAverageSpeed.text =
                        (v / 0.621).toInt().toString()
                    txtMaxSpeed.text = (vMax / 0.621).toInt().toString()

                }
            }
        }

    }


    private fun setBackGround() {
        intColor = requireActivity().getSharedPreferences(
            SettingConstants.SETTING,
            Context.MODE_PRIVATE
        ).getInt(SettingConstants.COLOR_DISPLAY, 2)
        with(binding) {
            FontUtils.setTextColor(intColor, this!!.txtMaxSpeed, txtAverageSpeed, txtDistance)
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

        val i = requireActivity().getSharedPreferences(
            SettingConstants.SETTING,
            Context.MODE_PRIVATE
        ).getInt(SettingConstants.COLOR_DISPLAY, 2)
        unit = myDataBase.SpeedDao().getChecked().type
        if (intColor != i) {
            with(binding) {
                FontUtils.setTextColor(i, this.txtMaxSpeed, txtAverageSpeed, txtDistance)
                btnStart.setBackgroundColor(ColorUtils.checkColor(i))
                btnPause.setBackgroundColor(ColorUtils.checkColor(i))
                btnResume.setBackgroundColor(ColorUtils.checkColor(i))
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
    }

}