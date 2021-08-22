package com.ordolabs.thecolor.util

import com.github.ajalt.colormath.RGB
import com.ordolabs.thecolor.model.ColorHexPresentation
import com.ordolabs.thecolor.model.ColorRgbPresentation

object ColorUtil {

    /**
     * "Abstract" color model. Actually, holds HEX color value __without number sign__.
     */
    data class Color(
        val hex: String
    ) {
        companion object
    }

    fun Color.Companion.from(color: ColorHexPresentation): Color {
        val expanded = if (color.value.length == 3) {
            color.value.map { it.toString().repeat(2) }.joinToString(separator = "")
        } else {
            color.value + "0".repeat(6 - color.value.length)
        }
        return Color(hex = expanded)
    }

    fun Color.Companion.from(color: ColorRgbPresentation): Color {
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
}