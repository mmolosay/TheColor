package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.Color
import javax.inject.Inject

/**
 * Converts between different instances of [Color].
 */
class ColorConverter @Inject constructor() {

    fun Color.toAbstract(): Color.Abstract =
        when (this) {
            is Color.Abstract -> this
            is Color.Hex -> this.toAbstract()
            is Color.Rgb -> this.toAbstract()
        }

    // TODO: add unit tests
    fun Color.toHex(): Color.Hex =
        when (this) {
            is Color.Abstract -> this.toHex()
            is Color.Hex -> this
            is Color.Rgb -> this.toAbstract().toHex()
        }

    // TODO: add unit tests
    fun Color.toRgb(): Color.Rgb =
        when (this) {
            is Color.Abstract -> this.toRgb()
            is Color.Hex -> this.toAbstract().toRgb()
            is Color.Rgb -> this
        }

    fun Color.Hex.toAbstract(): Color.Abstract {
        val int = this.value
        return Color.Abstract(int)
    }

    fun Color.Rgb.toAbstract(): Color.Abstract {
        val r = this.r shl 16
        val g = this.g shl 8
        val b = this.b
        val int = r or g or b
        return Color.Abstract(int)
    }

    fun Color.Abstract.toHex(): Color.Hex {
        return Color.Hex(value = this.int)
    }

    fun Color.Abstract.toRgb(): Color.Rgb {
        val r = (this.int shr 16) and 0xFF
        val g = (this.int shr 8) and 0xFF
        val b = this.int and 0xFF
        return Color.Rgb(r, g, b)
    }
}