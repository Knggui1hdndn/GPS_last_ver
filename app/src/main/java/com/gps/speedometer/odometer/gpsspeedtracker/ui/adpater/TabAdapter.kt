package com.gps.speedometer.odometer.gpsspeedtracker.ui.adpater

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gps.speedometer.odometer.gpsspeedtracker.ui.NotificationsFragment
import com.gps.speedometer.odometer.gpsspeedtracker.ui.DashboardFragment
import com.gps.speedometer.odometer.gpsspeedtracker.ui.HomeFragment

class TabAdapter(fm: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fm, lifecycle) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
       when(position){
           0 ->return HomeFragment()
           1 ->return DashboardFragment()
           2 ->return NotificationsFragment()
       }
        return HomeFragment()
    }

}