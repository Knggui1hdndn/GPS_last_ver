package com.example.gps.ui

import android.app.Service
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.gps.MyLocationConstants
import com.example.gps.utils.FontUtils
import com.example.gps.R
import com.example.gps.SharedData
import com.example.gps.dao.MyDataBase
import com.example.gps.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private var binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentDashboardBinding.bind(view)
        var allDistance = requireActivity().getSharedPreferences("state", Service.MODE_PRIVATE)
            .getInt(MyLocationConstants.DISTANCE, 0)
        with(binding) {
            FontUtils.setFont(requireContext(), this!!.txtSpeed)
            SharedData.currentSpeedLiveData.observe(viewLifecycleOwner) {
                if(it==0F)  txtSpeed.text ="000"
                when (String.format("%.0f", it).length) {
                    1 -> {
                        txtSpeed.text = "00"+String.format("%.0f", it)
                    }
                    2 -> {
                        txtSpeed.text = "0"+String.format("%.0f", it)
                    }

                    else->txtSpeed.text = String.format("%.0f", it)
                }

            }
            SharedData.distanceLiveData.observe(viewLifecycleOwner) {
                txtDistance1.text = (allDistance + it).toInt().toString()
            }
            txtDistance1.text = allDistance.toString()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}