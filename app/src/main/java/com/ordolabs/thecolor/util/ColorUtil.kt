package com.ordolabs.thecolor.util

import com.github.ajalt.colormath.RGB
import com.ordolabs.thecolor.model.ColorHexPresentation
import com.ordolabs.thecolor.model.ColorRgbPresentation

object ColorUtil {

    data class Color(
        val hex: String
    ) {
        companion object
    }

    fun Color.Companion.from(color: ColorHexPresentation): Color {
        val completed = if (color.value.length == 3) {
            color.value.map { it.toString().repeat(2) }.joinToString(separator = "")
        } else {
            color.value + "0".repeat(6 - color.value.length)
        }
        return Color(hex = completed)
    }

    fun Color.Companion.from(color: ColorRgbPresentation): Color {
        val rgb = RGB(color.r, color.g, color.b)
        return Color(hex = rgb.toHex(withNumberSign = false))
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