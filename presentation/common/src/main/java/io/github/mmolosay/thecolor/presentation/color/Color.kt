package io.github.mmolosay.thecolor.presentation.color

import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import io.github.mmolosay.thecolor.presentation.util.ColorUtil
import io.github.mmolosay.thecolor.presentation.util.ColorUtil.colorHexSignless
import io.github.mmolosay.thecolor.presentation.util.ColorUtil.toColorHexFullForm
import kotlinx.parcelize.IgnoredOnParcel
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

    @IgnoredOnParcel
    val hexSignless: String by lazy { hex.colorHexSignless()!! }

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
        is ColorPrototype -> this.from(color)
        else -> error("compilator bug requires this unreachable branch")
    }

fun Color.Companion.from(color: ColorPrototype): Color? =
    when (color) {
        is ColorPrototype.Hex -> Color.from(color)
        is ColorPrototype.Rgb -> Color.from(color)
    }

fun Color.Companion.from(color: ColorPrototype.Hex): Color? {
    val value = color.value ?: return null
    val hex = value.toColorHexFullForm() ?: return null
    return Color(hex)
}

fun Color.Companion.from(color: ColorPrototype.Rgb): Color? {
    val (r, g, b) = color
    if (r == null || g == null || b == null) return null
    val hex = ColorUtil.rgbToHex(r, g, b)
    return Color(hex)
}

fun Color.toHex() =
    ColorPrototype.Hex(
        value = this.hex.colorHexSignless()!!
    )

fun Color.toRgb(): ColorPrototype.Rgb {
    val rgb = ColorUtil.hexToRgb(this.hex)
    return ColorPrototype.Rgb(
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