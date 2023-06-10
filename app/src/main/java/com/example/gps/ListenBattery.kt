package com.example.gps

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.example.gps.ui.MainActivity2

class ListenBattery : BroadcastReceiver() {
    companion object {
        const val ACTION_BATTERY_CHANGED = "BATTERY_CHANGED"
    }

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            if (intent.action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                (context as MainActivity2).onBatteryDataReceived((level * 100 / scale.toFloat()).toInt())
             }
        }
    }
}