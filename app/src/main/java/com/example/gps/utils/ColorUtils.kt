package com.example.gps.utils

import android.graphics.Color
import com.example.gps.ColorConstants

class ColorUtils {
    companion object {
        fun checkColor(position: Int): Int {
            var color=0
            when (position) {
                1 -> color=Color.parseColor(ColorConstants.COLOR_1)
                2 -> color=Color.parseColor(ColorConstants.COLOR_2)
                3 -> color=Color.parseColor(ColorConstants.COLOR_3)
                4 -> color=Color.parseColor(ColorConstants.COLOR_4)
                5 -> color=Color.parseColor(ColorConstants.COLOR_5)
                6 -> color=Color.parseColor(ColorConstants.COLOR_6)
                7 -> color=Color.parseColor(ColorConstants.COLOR_7)
            }
            return color
        }
    }
}