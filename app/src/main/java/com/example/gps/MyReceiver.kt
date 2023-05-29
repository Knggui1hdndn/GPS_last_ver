package com.example.gps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.ActivityTransitionEvent
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity

class MyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context,"Activity:  ",Toast.LENGTH_SHORT).show()

        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            if (result != null) {
                for (event in result.transitionEvents) {
                    val activityType = event.activityType
                    val transitionType = event.transitionType
Toast.makeText(context,"Activity: $activityType, Transition: $transitionType",Toast.LENGTH_SHORT).show()
                    // Xử lý thông tin về hoạt động và sự chuyển đổi
                    Log.d("kjklfda", (activityType+transitionType).toString())
                 }
            }
        }
    }



}