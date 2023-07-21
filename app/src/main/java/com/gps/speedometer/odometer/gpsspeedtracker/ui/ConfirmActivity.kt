package com.gps.speedometer.odometer.gpsspeedtracker.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
        Handler(Looper.getMainLooper()).postDelayed({
            val mainActivity2 = SharedData.activity as MainActivity2
            mainActivity2.sendDataToSecondFragment()
            val notificationsFragment = mainActivity2.supportFragmentManager.findFragmentByTag("f2")
            if (notificationsFragment != null) (notificationsFragment as NotificationsFragment).onClearMap(
                false
            )
            showInterstitial(true){
                finish()
                val myDataBase = MyDataBase.getInstance(mainActivity2)
                val i = Intent(this@ConfirmActivity, ShowActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                i.putExtra("movementData", myDataBase.movementDao().getLastMovementData())
                startActivity(i)
            }
        }, 5000)
    }
}