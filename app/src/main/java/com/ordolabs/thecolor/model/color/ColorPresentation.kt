package com.ordolabs.thecolor.model.color

import com.ordolabs.thecolor.util.struct.Color
import com.ordolabs.thecolor.util.struct.fromHex
import com.ordolabs.thecolor.util.struct.fromRgb
import com.ordolabs.thecolor.util.struct.toColorRgb
import kotlinx.parcelize.Parcelize

/**
 * Presentation of __valid__ color of immaterial color space.
 */
// TODO: move all Color functionality into this and get rid of Color?
@Parcelize
data class ColorPresentation(
    val color: Color
) : IAbstractColor {

    companion object
}

fun ColorPresentation.Companion.from(color: ColorHex) =
    ColorPresentation(
        color = Color.fromHex(color.value)
            ?: error("ColorPresentation must contain valid color")
    )

fun ColorPresentation.Companion.from(color: ColorRgb) =
    ColorPresentation(
        color = Color.fromRgb(color.r, color.g, color.b)
            ?: error("ColorPresentation must contain valid color")
    )

fun ColorPresentation.toColorHexPresentation() =
    ColorHex(
        value = this.color.hexSignless
    )

fun ColorPresentation.toColorRgbPresentation(): ColorRgb {
    val rgb = color.toColorRgb()
    return ColorRgb(
        r = rgb.r,
        g = rgb.g,
        b = rgb.b
    )
}