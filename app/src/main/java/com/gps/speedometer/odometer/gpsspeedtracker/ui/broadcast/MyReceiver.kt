package com.gps.speedometer.odometer.gpsspeedtracker.ui.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityTransitionResult

class MyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            if (result != null) {
                for (event in result.transitionEvents) {
                    val activityType = event.activityType
                    val transitionType = event.transitionType
                  }
            }
        }
    }



}