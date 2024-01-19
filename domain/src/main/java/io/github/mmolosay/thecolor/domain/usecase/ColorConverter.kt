package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.Color
import javax.inject.Inject

// TODO: use third-party library for converting
class ColorConverter @Inject constructor() {

    fun Color.toAbstract(): Color.Abstract =
        when (this) {
            is Color.Abstract -> this
            is Color.Hex -> this.toAbstract()
            is Color.Rgb -> this.toAbstract()
        }

    fun Color.Hex.toAbstract(): Color.Abstract {
        val int = this.value
        return AbstractColorImpl(int)
    }

    fun Color.Rgb.toAbstract(): Color.Abstract {
        val r = this.r shl 16
        val g = this.g shl 8
        val b = this.b
        val int = r or g or b
        return AbstractColorImpl(int)
    }

    fun Color.Abstract.toHex(): Color.Hex {
        val abstract = this as AbstractColorImpl
        return Color.Hex(value = abstract.int)
    }

    fun Color.Abstract.toRgb(): Color.Rgb {
        val abstract = this as AbstractColorImpl
        val r = (abstract.int shr 16) and 0xFF
        val g = (abstract.int shr 8) and 0xFF
        val b = abstract.int and 0xFF
        return Color.Rgb(r, g, b)
    }

    @JvmInline
    private value class AbstractColorImpl(val int: Int) : Color.Abstract
}