package com.example.gps.ui


import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
import com.example.gps.utils.TimeUtils
import com.example.gps.utils.UnitUtils

class ParameterFragment : Fragment(R.layout.fragment_parameter) ,MeasurementInterFace{
    private lateinit var binding: FragmentParameterBinding
    private var intColor: Int = 0
    private var check = false
    private var sharedPreferences: SharedPreferences? = null
     private lateinit var myDataBase: MyDataBase
    var checkUnit = ""

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentParameterBinding.bind(view)
        sharedPreferences = requireActivity().getSharedPreferences("state", Service.MODE_PRIVATE)
        myDataBase = MyDataBase.getInstance(requireContext())
        checkUnit = getCurrentUnit()
        setBackGround()
        setTextDefault()
        onUnitChange()
        //set state is STOP when MyService not Running
        if (!isMyServiceRunning(MyService::class.java)) setState(MyLocationConstants.STOP)
        with(binding) {

            if (requireActivity().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                SharedData.time.observe(viewLifecycleOwner) {
                    times!!.text = TimeUtils.formatTime(it)
                }
                settings!!.setOnClickListener {
                    startActivity(Intent(requireActivity(), Setting::class.java))
                }
                stop!!.setOnClickListener {
                  stopService()
                }
                imgRotateScreen1!!.setOnClickListener {
                    requireActivity().requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
                }
            }



            SharedData.maxSpeedLiveData.observe(viewLifecycleOwner) {
                this.txtMaxSpeed.text = if (it  <= 0) "0" + SharedData.toUnit else String.format(
                    "%.0f",
                     it
                ) + SharedData.toUnit
                setFont(binding)
            }


            SharedData.distanceLiveData.observe(viewLifecycleOwner) {
                this.txtDistance.text =if (SharedData.toUnit != "km/h") "${(it  * 0.6214).toInt()}mi" else "${(it  * 1.60934).toInt()}km"
                setFont(binding)
            }

            SharedData.averageSpeedLiveData.observe(viewLifecycleOwner) {
                this.txtAverageSpeed.text =
                    if (it  <= 0) "0" + SharedData.toUnit else String.format(
                        "%.0f",
                        SharedData.convertSpeed(it )
                    ) + SharedData.toUnit
                setFont(binding)
            }
            setDataWhenComeBack()
            showOrHideView()
            setFont(binding)
            this.btnStart.setOnClickListener {
                insertMovementData()
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

    @SuppressLint("SetTextI18n")
    private fun setTextDefault() {
        with(binding){
            this.txtMaxSpeed.text = "0" + SharedData.toUnit
            this.txtDistance.text = "0" + if (SharedData.toUnit != "km/h") "mi" else "km"
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
                0F,
                0F,
                0F,
                0F
            )
        )
    }

    private fun stopService() {
        val status = requireContext().getSharedPreferences("state", Context.MODE_PRIVATE)
            .getString(MyLocationConstants.STATE, null)
        if (status != MyLocationConstants.STOP && status != null) {
            val intent = Intent(requireContext(), MyService::class.java)
            intent.action = MyLocationConstants.STOP
            requireActivity().startService(intent)
            SharedData.checkService = true
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
        val manager = requireActivity().getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
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

    private fun hideBtnResume() {
        with(binding) {
            this.btnStart.visibility = View.GONE
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


    private fun setDataWhenComeBack() {
            with(binding) {
                 numberSeparation(txtDistance)
                val distance = numberSeparation(txtDistance)
                SharedData.distanceLiveData.value =  SharedData.convertDistance(distance.toFloat()) .toFloat()
                val averageSpeed = numberSeparation(txtAverageSpeed)
                SharedData.averageSpeedLiveData.value  = SharedData.convertSpeed(averageSpeed.toFloat()).toFloat()
                val speed = numberSeparation(txtMaxSpeed)
                SharedData.maxSpeedLiveData.value =SharedData.convertSpeed(speed .toFloat()).toFloat()
            }
            setFont(binding)

    }



    fun getCurrentUnit():String{
        val myDataBase=MyDataBase.getInstance(requireContext()).SpeedDao()
     return UnitUtils.getUnit(myDataBase.getChecked().type)
    }


    private fun numberSeparation(txt: TextView): Int {
        return txt.text.toString().filter { it.isDigit() }.toInt()
    }



    override fun onUnitChange() {
        setDataWhenComeBack()
    }

    override fun onColorChange(i:Int) {
        with(binding) {
            FontUtils.setTextColor(i, this.txtMaxSpeed, txtAverageSpeed, txtDistance)
            btnStart.setBackgroundColor(ColorUtils.checkColor(i))
            btnPause.setBackgroundColor(ColorUtils.checkColor(i))
            btnResume.setBackgroundColor(ColorUtils.checkColor(i))

        }
    }
}