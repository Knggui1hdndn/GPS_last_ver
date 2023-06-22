package com.gps.speedometer.odometer.gpsspeedtracker.utils

import android.graphics.Color
import androidx.appcompat.app.AppCompatDelegate
import com.gps.speedometer.odometer.gpsspeedtracker.constants.ColorConstants

class ColorUtils {
    companion object {
        fun checkColor(position: Int): Int {
            var color = 0
            if (position == 0) {
                if (isThemeDark()) return Color.parseColor(ColorConstants.COLOR_1_1) else return Color.parseColor(
                    ColorConstants.COLOR_1
                )
            }
            when (position) {
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