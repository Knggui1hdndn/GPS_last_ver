package com.example.gps.utils

import android.graphics.Color
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.example.gps.constants.ColorConstants

class ColorUtils {
    companion object {
        fun checkColor(position: Int): Int {
            Log.d("sssssssssss", position.toString())
            var color = 0
            when (position) {
                8 -> color = Color.parseColor(ColorConstants.COLOR_1_1)

                1 -> color = Color.parseColor(Color.BLACK.toString())
                2 -> color = Color.parseColor(ColorConstants.COLOR_2)
                3 -> color = Color.parseColor(ColorConstants.COLOR_3)
                4 -> color = Color.parseColor(ColorConstants.COLOR_4)
                5 -> color = Color.parseColor(ColorConstants.COLOR_5)
                6 -> color = Color.parseColor(ColorConstants.COLOR_6)
                7 -> color = Color.parseColor(ColorConstants.COLOR_7)
            }
            return color
        }

        fun isThemeDark(): Boolean {
            return AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        }
    }
}