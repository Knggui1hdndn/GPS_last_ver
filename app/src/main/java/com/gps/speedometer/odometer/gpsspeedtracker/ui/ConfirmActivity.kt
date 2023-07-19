package com.gps.speedometer.odometer.gpsspeedtracker.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.access.pro.callBack.OnShowInterstitialListener
import com.gps.speedometer.odometer.gpsspeedtracker.R
import com.gps.speedometer.odometer.gpsspeedtracker.biiling.BaseActivity
import com.gps.speedometer.odometer.gpsspeedtracker.constants.SettingConstants
import com.gps.speedometer.odometer.gpsspeedtracker.dao.MyDataBase
import com.gps.speedometer.odometer.gpsspeedtracker.databinding.ActivityConfirmBinding
import com.gps.speedometer.odometer.gpsspeedtracker.model.Vehicle
import com.gps.speedometer.odometer.gpsspeedtracker.`object`.SharedData
import com.gps.speedometer.odometer.gpsspeedtracker.presenter.ParameterPresenter
import com.gps.speedometer.odometer.gpsspeedtracker.utils.VehicleUtils

class ConfirmActivity : BaseActivity() {
    var _binding: ActivityConfirmBinding? = null
    val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityConfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val myDataBase = MyDataBase.getInstance(this).vehicleDao()
        binding.txtConfirm.text = "Phương tiện:${VehicleUtils.getVehicle(myDataBase.getVehicleChecked().type)}" +
                    "\nTốc độ tối đa:${myDataBase.getVehicleChecked().limitWarning}" +
                    "\nĐơn vị đo:${SharedData.toUnit}\n"
        binding.btnOK.setOnClickListener {
            showInterstitial(true) {
                finish()
                val frag =
                    (SharedData.activity as MainActivity2).supportFragmentManager.findFragmentByTag(
                        "f" + (SharedData.activity as MainActivity2).viewPager.currentItem
                    )?.childFragmentManager!!.findFragmentById(
                        R.id.frag
                    ) as ParameterFragment
                frag.startService()
                (SharedData.activity as MainActivity2).showMess()
            }
        }
    }
}