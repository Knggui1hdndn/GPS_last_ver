package com.example.gps.utils

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.widget.TextView

class FontUtils() {
    companion object {
        fun setFont(context: Context, vararg textView: TextView) {
            textView.forEach {
                it.typeface = Typeface.createFromAsset(context.assets, "font_lcd.ttf")
                val spannableString = SpannableString(it.text.toString())
                var position = it.text.toString().indexOf("k")
                if (position < 0) {
                    position = it.text.toString().indexOf("m")

                }
                if (position < 0) {
                    position = it.text.toString().indexOf("N")

                }
                if (position >= 0) {
                    val newSize = 0.5f
                    val sizeSpan = RelativeSizeSpan(newSize)
                    Log.d("dfgvub", "$position")
                    spannableString.setSpan(
                        sizeSpan,
                        position,
                        it.text.toString().length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    it.text = spannableString
                }
            }
        }

        fun setTextColor(colorPosition: Int, vararg txt: TextView) {
            txt.forEach {
                it.setTextColor(ColorUtils.checkColor(colorPosition))
            }
        }
    }
}