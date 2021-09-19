package com.ordolabs.thecolor.util

import androidx.annotation.ColorInt
import com.github.ajalt.colormath.RGB
import com.ordolabs.thecolor.model.ColorHexPresentation
import com.ordolabs.thecolor.model.ColorRgbPresentation
import android.graphics.Color as ColorAndroid

object ColorUtil {

    /**
     * "Abstract" color model. Actually, holds HEX color value __without number sign__.
     */
    data class Color(
        val hex: String
    ) {
        val hexWithNumberSign: String get() = '#' + this.hex

        override operator fun equals(other: Any?): Boolean {
            other ?: return false
            if (other is String) {
                return (other == hex || other == hexWithNumberSign)
            }
            if (other is Color) {
                return super.equals(other)
            }
            return false
        }

        override fun hashCode(): Int {
            return hex.hashCode()
        }

        companion object
    }

    fun Color.Companion.from(color: ColorHexPresentation): Color? {
        color.value ?: return null
        val expanded = when (color.value.length) {
            3 -> color.value.map { it.toString().repeat(2) }.joinToString(separator = "")
            6 -> color.value
            else -> return null
        }
        return Color(hex = expanded)
    }

    fun Color.Companion.from(color: ColorRgbPresentation): Color? {
        if (color.r == null || color.g == null || color.b == null) return null
        val rgb = RGB(color.r, color.g, color.b)
        val value = rgb.toHex(withNumberSign = false).uppercase()
        return Color(hex = value)
    }

    fun Color.toColorHex(): ColorHexPresentation {
        return ColorHexPresentation(value = this.hex)
    }

    fun Color.toColorRgb(): ColorRgbPresentation {
        val color = RGB(this.hex)
        return ColorRgbPresentation(
            r = color.r,
            g = color.g,
            b = color.b
        )
    }

    @ColorInt
    fun Color.toColorInt(): Int {
        return ColorAndroid.parseColor(this.hexWithNumberSign)
    }
}