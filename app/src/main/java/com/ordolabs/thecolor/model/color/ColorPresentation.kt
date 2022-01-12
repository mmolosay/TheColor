package com.ordolabs.thecolor.model.color

import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import com.ordolabs.thecolor.util.ColorUtil
import com.ordolabs.thecolor.util.ColorUtil.colorHexSignless
import com.ordolabs.thecolor.util.ColorUtil.toColorHexFullForm
import kotlinx.parcelize.Parcelize

/**
 * Presentation of __valid__ abstract color of some color space.
 *
 * @param hex HEX color string __with__ number sign, e.g. "#16A8C0".
 */
// TODO: move all Color functionality into this and get rid of Color?
@Parcelize
data class ColorPresentation(
    val hex: String
) : IAbstractColor {

    companion object
}

fun ColorPresentation.Companion.from(color: ColorHex): ColorPresentation? {
    val value = color.value ?: return null
    val hex = value.toColorHexFullForm() ?: return null
    return ColorPresentation(hex)
}

fun ColorPresentation.Companion.from(color: ColorRgb): ColorPresentation? {
    val (r, g, b) = color
    if (r == null || g == null || b == null) return null
    val hex = ColorUtil.rgbToHex(r, g, b)
    return ColorPresentation(hex)
}

fun ColorPresentation.toColorHexPresentation() =
    ColorHex(
        value = this.hex.colorHexSignless()!!
    )

fun ColorPresentation.toColorRgbPresentation(): ColorRgb {
    val rgb = ColorUtil.hexToRgb(this.hex)
    return ColorRgb(
        r = rgb.r,
        g = rgb.g,
        b = rgb.b
    )
}

@ColorInt
fun ColorPresentation.toColorInt(): Int {
    return android.graphics.Color.parseColor(this.hex)
}

fun ColorPresentation.isDark(
    @IntRange(from = 0, to = 100) threshold: Int = 60
): Boolean =
    ColorUtil.isDark(this.hex, threshold)