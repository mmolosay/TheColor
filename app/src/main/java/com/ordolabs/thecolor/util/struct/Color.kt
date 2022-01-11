package com.ordolabs.thecolor.util.struct

import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import com.github.ajalt.colormath.RGB
import com.ordolabs.thecolor.util.ColorUtil.colorHexSignless
import com.ordolabs.thecolor.util.ColorUtil.isColorHexSigned
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * __Valid__ color of immaterial color space.
 * Actually, holds HEX color value __with number sign__.
 *
 * @param [hex] HEX color string __with__ number sign, e.g. "#16A8C0".
 */
@Parcelize
data class Color(
    val hex: String
) : Parcelable {

    init {
        require(hex.isColorHexSigned() == true) { "$hex is not signed HEX color" }
    }

    @IgnoredOnParcel
    val hexSignless: String by lazy {
        hex.colorHexSignless()!! // already checked that .isColorHex() in init block
    }

    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (other is String) {
            return (other == hex || other == hexSignless)
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

fun Color.Companion.fromHex(hex: String?): Color? {
    hex ?: return null
    val expanded = RGB(hex).toHex(withNumberSign = true).uppercase()
    return Color(hex = expanded)
}

fun Color.Companion.fromRgb(r: Int?, g: Int?, b: Int?): Color? {
    if (r == null || g == null || b == null) return null
    val rgb = RGB(r, g, b)
    val value = rgb.toHex(withNumberSign = true).uppercase()
    return Color(hex = value)
}

fun Color.toColorHex(): String {
    return this.hex
}

fun Color.toColorRgb(): RGB {
    return RGB(this.hex)
}

@ColorInt
fun Color.toColorInt(): Int {
    return android.graphics.Color.parseColor(this.hex)
}

fun Color.isDark(
    @IntRange(from = 0, to = 100) threshold: Int = 60
): Boolean {
    val hsl = RGB(this.hex).toHSL()
    return (hsl.l <= threshold)
}