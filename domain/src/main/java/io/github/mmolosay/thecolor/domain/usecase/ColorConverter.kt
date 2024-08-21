package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.Color
import javax.inject.Inject

/**
 * Converts between different instances of [Color].
 */
class ColorConverter @Inject constructor() {

    fun Color.toHex(): Color.Hex =
        when (this) {
            is Color.Hex -> this
            is Color.Rgb -> this.toHex()
        }

    fun Color.toRgb(): Color.Rgb =
        when (this) {
            is Color.Hex -> this.toRgb()
            is Color.Rgb -> this
        }

    fun Color.Rgb.toHex(): Color.Hex {
        val r = this.r shl 16
        val g = this.g shl 8
        val b = this.b
        val int = r or g or b
        return Color.Hex(int)
    }

    fun Color.Hex.toRgb(): Color.Rgb {
        val r = (this.value shr 16) and 0xFF
        val g = (this.value shr 8) and 0xFF
        val b = this.value and 0xFF
        return Color.Rgb(r, g, b)
    }
}