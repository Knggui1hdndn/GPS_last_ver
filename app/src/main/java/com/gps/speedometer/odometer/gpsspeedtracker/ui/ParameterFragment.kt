package com.gps.speedometer.odometer.gpsspeedtracker.ui


import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.access.pro.callBack.OnShowInterstitialListener
import com.gps.speedometer.odometer.gpsspeedtracker.constants.MyLocationConstants
import com.gps.speedometer.odometer.gpsspeedtracker.R
import com.gps.speedometer.odometer.gpsspeedtracker.constants.SettingConstants
import com.gps.speedometer.odometer.gpsspeedtracker.dao.MyDataBase
import com.gps.speedometer.odometer.gpsspeedtracker.databinding.DialogPermissionDescriptionBinding
import com.gps.speedometer.odometer.gpsspeedtracker.`object`.SharedData
import com.gps.speedometer.odometer.gpsspeedtracker.databinding.FragmentParameterBinding
import com.gps.speedometer.odometer.gpsspeedtracker.interfaces.ParameterContracts
import com.gps.speedometer.odometer.gpsspeedtracker.presenter.ParameterPresenter
import com.gps.speedometer.odometer.gpsspeedtracker.service.MyService
import com.gps.speedometer.odometer.gpsspeedtracker.`object`.CheckPermission
import com.gps.speedometer.odometer.gpsspeedtracker.utils.ColorUtils
import com.gps.speedometer.odometer.gpsspeedtracker.utils.FontUtils

class ParameterFragment : Fragment(R.layout.fragment_parameter),
    ParameterContracts.View {
    private lateinit var binding: FragmentParameterBinding
    private lateinit var dialog: Dialog
    private lateinit var presenter: ParameterPresenter
    private var check = false
    private var sharedPreferences: SharedPreferences? = null
    private var shareShow: SharedPreferences? = null
    private lateinit var sharedPreferencesSetting: SharedPreferences

    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
            if (granted.entries.all { it.value }) {
                dialog.dismiss()
                presenter.startService()
                (activity as MainActivity2).showMess()
            }
        }


    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("SetTextI18n", "RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentParameterBinding.bind(view)
        sharedPreferences = requireActivity().getSharedPreferences("state", Service.MODE_PRIVATE)
        shareShow = requireActivity().getSharedPreferences("show", AppCompatActivity.MODE_PRIVATE)
        sharedPreferencesSetting = requireContext().getSharedPreferences(SettingConstants.SETTING, Context.MODE_PRIVATE)
        presenter = ParameterPresenter(this, this)
        presenter.updateUIState()
        presenter.getDistance()
        presenter.getAverageSpeed()
        presenter.getMaxSpeed()
        presenter.timeStart()
        SharedData.color.observe(viewLifecycleOwner) {
            with(binding) {
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

        if (!presenter.isMyServiceRunning(MyService::class.java)) {
            presenter.setState(
                MyLocationConstants.STOP
            );
        }
        handleOrientationClickAll()

    }

    fun stopService() {
        presenter.stopService()
     }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun handleOrientationClickAll() {
        binding.btnStart.setOnClickListener {
             if (!CheckPermission.hasLocationPermission(requireContext())) {
                showDialog()
            } else {
//                 val frag = (SharedData.activity as MainActivity2).supportFragmentManager.findFragmentByTag(
//                         "f" + (SharedData.activity as MainActivity2).viewPager.currentItem
//                     )?.childFragmentManager!!.findFragmentById(
//                         R.id.frag
//                     ) as ParameterFragment
                 presenter.startService()
                 (SharedData.activity as MainActivity2).showMess()
            }
        }
        binding.btnStop.setOnClickListener {
            stopService()
            requireContext().stopService(Intent(context, MyService::class.java))
            startActivity(Intent(requireContext(),ConfirmActivity::class.java))
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
    fun showDialog() {
        val dialogBinding: DialogPermissionDescriptionBinding =
            DialogPermissionDescriptionBinding.inflate(layoutInflater)
        dialog = Dialog(requireContext())
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(dialogBinding.root)
        dialogBinding.textView2.text = Html.fromHtml(getString(R.string.mess_dialog), HtmlCompat.FROM_HTML_MODE_LEGACY)
        dialog.setCancelable(false)
        dialog.show()

        dialogBinding.button.setOnClickListener {
            permissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
        }
        dialogBinding.txtLater.setOnClickListener {
            Toast.makeText(requireContext(),"Required permissions for GPS location",Toast.LENGTH_SHORT).show()
            dialog.dismiss()
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
        binding.imgReset.visibility =if (sharedPreferencesSetting.getBoolean(SettingConstants.SHOW_RESET_BUTTON,true)) View.VISIBLE else View.INVISIBLE

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