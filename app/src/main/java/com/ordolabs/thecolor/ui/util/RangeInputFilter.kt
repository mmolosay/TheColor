package com.ordolabs.thecolor.ui.util

import android.text.InputFilter
import android.text.Spanned

class RangeInputFilter(min: Int, max: Int) : InputFilter {

    private var range = min..max

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        try {
            // Remove the string out of destination that is to be replaced
            var newVal = dest.toString().substring(0, dstart) +
                dest.toString().substring(dend, dest.toString().length)
            // Add the new string in
            newVal = newVal.substring(0, dstart) + source.toString() + newVal.substring(
                dstart,
                newVal.length
            )
            val input = newVal.toInt()
            if (input in range) return null
        } catch (nfe: NumberFormatException) {
            // do nothing
        }
        return ""
    }
}