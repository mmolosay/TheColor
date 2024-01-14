package io.github.mmolosay.presentation.util

import androidx.annotation.IntRange
import com.github.ajalt.colormath.RGB

// TODO: ColorUtil should be a singleton class, which instance
//  would be injected by DI handler in places of usage.
/**
 * Contains all color converting and asserting logic.
 */
object ColorUtil {

    private const val HEX_NUMBER_SIGN = '#'

    private val hexFormatRegex = Regex("^#?(?:[0-9a-fA-F]{3}){1,2}\$")

    // region Convertion

    fun hexToRgb(hex: String) =
        RGB(hex)

    fun rgbToHex(r: Int, g: Int, b: Int) =
        rgbToHex(RGB(r, g, b))

    // endregion

    fun isDark(
        hex: String,
        @IntRange(from = 0, to = 100) threshold: Int
    ): Boolean {
        val hsl = RGB(hex).toHSL()
        return (hsl.l <= threshold)
    }

    // region Extensions

    fun String.colorHexSigned(): String? {
        if (!this.isColorHex()) return null
        if (this.isColorHexSigned()!!) return this
        return "$HEX_NUMBER_SIGN$this"
    }

    fun String.colorHexSignless(): String? {
        if (!this.isColorHex()) return null
        if (this.isColorHexSignless()!!) return this
        return this.substring(1)
    }

    fun String.isColorHex(): Boolean =
        this.matches(hexFormatRegex)

    fun String.isColorHexSigned(): Boolean? {
        if (!this.isColorHex()) return null
        return (this.first() == HEX_NUMBER_SIGN)
    }

    fun String.isColorHexSignless(): Boolean? =
        this.isColorHexSigned()?.let { !it }

    fun String.toColorHexFullForm(): String? {
        if (!this.isColorHex()) return null
        return rgbToHex(RGB(this))
    }

    // endregion

    private fun rgbToHex(color: RGB): String =
        color.toHex(withNumberSign = true).uppercase()
}