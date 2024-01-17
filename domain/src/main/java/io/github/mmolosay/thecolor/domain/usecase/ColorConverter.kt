package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.Color
import javax.inject.Inject

// TODO: use third-party library for converting
class ColorConverter @Inject constructor() {

    fun Color.Hex.toAbstract(): Color.Abstract {
        val int = this.value.toInt(radix = 16)
        return AbstractColorImpl(int)
    }

    fun Color.Rgb.toAbstract(): Color.Abstract {
        val r = this.r shl 16
        val g = this.g shl 8
        val b = this.b
        val int = r or g or b
        return AbstractColorImpl(int)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun Color.Abstract.toHex(): Color.Hex {
        val abstract = this as AbstractColorImpl
        val string = abstract.int
            .toHexString(HexFormat.UpperCase)
            .removePrefix("00")
        return Color.Hex(value = string)
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