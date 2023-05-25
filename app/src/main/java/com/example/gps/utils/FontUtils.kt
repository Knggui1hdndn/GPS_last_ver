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
                val position = it.text.toString().indexOf("k")
                if (position >= 0) {
                    // Chỉ định kích thước mới cho kí tự
                    val newSize = 0.5f
// Tạo một RelativeSizeSpan với kích thước mới
                    val sizeSpan = RelativeSizeSpan(newSize)
Log.d("dfgvub", "$position")
// Áp dụng RelativeSizeSpan cho kí tự tại vị trí đã chỉ định
                    spannableString.setSpan(
                        sizeSpan,
                        position,
                        it.text.toString().length ,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

// Sử dụng spannableString trong TextView
                    it.text = spannableString
                }
            }

        }
    }
}