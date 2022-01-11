package com.ordolabs.thecolor.model.color

import android.os.Parcelable
import com.ordolabs.thecolor.util.struct.Color
import com.ordolabs.thecolor.util.struct.fromHex
import com.ordolabs.thecolor.util.struct.fromRgb
import com.ordolabs.thecolor.util.struct.toColorRgb
import kotlinx.parcelize.Parcelize

// TODO: get rid of *Presentation suffix of classes, which are not converted from domain.

/**
 * Presentation of __valid__ color of immaterial color space.
 */
// TODO: move all Color functionality into this and get rid of Color?
@Parcelize
data class ColorPresentation(
    val color: Color
) : IColorSpacePresentation {

    companion object
}

fun ColorPresentation.Companion.from(color: ColorHexPresentation) =
    ColorPresentation(
        color = Color.fromHex(color.value)
            ?: error("ColorPresentation must contain valid color")
    )

fun ColorPresentation.Companion.from(color: ColorRgbPresentation) =
    ColorPresentation(
        color = Color.fromRgb(color.r, color.g, color.b)
            ?: error("ColorPresentation must contain valid color")
    )

fun ColorPresentation.toColorHexPresentation() =
    ColorHexPresentation(
        value = this.color.hexSignless
    )

fun ColorPresentation.toColorRgbPresentation(): ColorRgbPresentation {
    val rgb = color.toColorRgb()
    return ColorRgbPresentation(
        r = rgb.r,
        g = rgb.g,
        b = rgb.b
    )
}

/**
 * Presentation of HEX color. May contain non-valid color or nothing at all.
 *
 * @param value __signless__ HEX color `String`, e.g. "16A8C0".
 */
@Parcelize
data class ColorHexPresentation(
    val value: String?
) : IColorSpacePresentation

/**
 * Presentation of RGB color. May contain non-valid color or nothing at all.
 *
 * @param r R component in range (0-255) including both start and end.
 * @param g G component in range (0-255) including both start and end.
 * @param b B component in range (0-255) including both start and end.
 */
@Parcelize
data class ColorRgbPresentation(
    val r: Int?,
    val g: Int?,
    val b: Int?
) : IColorSpacePresentation

/**
 * Empty interface, that indicates that class, inheriting it,
 * is a presentation of color of some color space.
 */
sealed interface IColorSpacePresentation : Parcelable