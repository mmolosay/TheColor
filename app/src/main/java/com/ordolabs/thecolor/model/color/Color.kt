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
@Parcelize
open class Color(
    open val hex: String
) : AbstractColor() {

    override fun equals(other: Any?): Boolean {
        other ?: return false
        return when (other) {
            is Color -> (this.hex == other.hex)
            is AbstractColor -> super.equals(other)
            else -> false
        }
    }

    override fun hashCode(): Int {
        return hex.hashCode()
    }

    companion object
}

fun Color.Companion.from(color: AbstractColor): Color? =
    when (color) {
        is Color -> color
        is ColorHex -> Color.from(color)
        is ColorRgb -> Color.from(color)
    }

fun Color.Companion.from(color: ColorHex): Color? {
    val value = color.value ?: return null
    val hex = value.toColorHexFullForm() ?: return null
    return Color(hex)
}

fun Color.Companion.from(color: ColorRgb): Color? {
    val (r, g, b) = color
    if (r == null || g == null || b == null) return null
    val hex = ColorUtil.rgbToHex(r, g, b)
    return Color(hex)
}

fun Color.toColorHex() =
    ColorHex(
        value = this.hex.colorHexSignless()!!
    )

fun Color.toColorRgb(): ColorRgb {
    val rgb = ColorUtil.hexToRgb(this.hex)
    return ColorRgb(
        r = rgb.r,
        g = rgb.g,
        b = rgb.b
    )
}

@ColorInt
fun Color.toColorInt(): Int {
    return android.graphics.Color.parseColor(this.hex)
}

fun Color.isDark(
    @IntRange(from = 0, to = 100) threshold: Int = 60
): Boolean =
    ColorUtil.isDark(this.hex, threshold)