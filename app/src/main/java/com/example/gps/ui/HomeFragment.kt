package com.example.gps.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.gps.R
import com.example.gps.SharedData
import com.example.gps.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentHomeBinding.bind(view)
        var timePrevious=System.currentTimeMillis()
        with(binding) {
            SharedData.currentSpeedLiveData.observe(viewLifecycleOwner) {
                var speed1 = String.format("%.1f", it)
                speed1 = speed1.replace(",", ".");
                this!!.speed.speedTo(speed1.toFloat(), System.currentTimeMillis() - timePrevious)
                timePrevious=System.currentTimeMillis()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}