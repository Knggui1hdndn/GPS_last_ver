package com.example.gps.ui.adpater

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.gps.ui.DashboardFragment
import com.example.gps.ui.HomeFragment
import com.example.gps.ui.NotificationsFragment

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