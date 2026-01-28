package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorConstants
import io.github.mmolosay.thecolor.utils.truncateDecimalPlaces
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Converts between different instances of [Color].
 */
class ColorConverter @Inject constructor() {

    fun Color.toHex(): Color.Hex =
        when (this) {
            is Color.Hex -> this
            is Color.Rgb -> this.toHex()
            is Color.Hsv -> this.toRgb().toHex()
        }

    fun Color.toRgb(): Color.Rgb =
        when (this) {
            is Color.Hex -> this.toRgb()
            is Color.Rgb -> this
            is Color.Hsv -> this.toRgb()
        }

    // TODO: unit test me
    fun Color.toHsv(): Color.Hsv =
        when (this) {
            is Color.Hex -> this.toRgb().toHsv()
            is Color.Rgb -> this.toHsv()
            is Color.Hsv -> this
        }

    /*
     * For any conversion from color space X to color space Y we convert X to RGB and then RGB to Y.
     * In other words, for every color space, (X -> RGB) and (RGB -> X) should be defined and sufficient.
     */

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

    fun Color.Hsv.toRgb(): Color.Rgb {
        val chroma = this.saturation * this.value
        val huePrime = this.hue / 60f // [0, 6)
        val x = chroma * (1f - abs((huePrime % 2f) - 1f)) // interpolated secondary component
        val m = this.value - chroma // base brightness offset
        val cR: Float; val cG: Float; val cB: Float // RGB components in [0, C] chroma range
        when {
            huePrime < 1f -> { cR = chroma; cG = x; cB = 0f } // 0° ≤ H < 60°
            huePrime < 2f -> { cR = x; cG = chroma; cB = 0f } // 60° ≤ H < 120°
            huePrime < 3f -> { cR = 0f; cG = chroma; cB = x } // 120° ≤ H < 180°
            huePrime < 4f -> { cR = 0f; cG = x; cB = chroma } // 180° ≤ H < 240°
            huePrime < 5f -> { cR = x; cG = 0f; cB = chroma } // 240° ≤ H < 300°
            huePrime < 6f -> { cR = chroma; cG = 0f; cB = x } // 300° ≤ H < 360°
            else -> error("unreachable")
        }
        fun normalize(chromaComponent: Float): Float =
            chromaComponent + m
        val nR = normalize(cR)
        val nG = normalize(cG)
        val nB = normalize(cB)
        fun quantize(normalizedComponent: Float): Int =
            (normalizedComponent * ColorConstants.RgbColorComponentIntRange.last).roundToInt()
        val r = quantize(nR)
        val g = quantize(nG)
        val b = quantize(nB)
        return Color.Rgb(r, g, b)
    }

    fun Color.Rgb.toHsv(): Color.Hsv {
        fun normalize(rgbComponent: Int): Float {
            val rgbComponentMaxValue = ColorConstants.RgbColorComponentIntRange.last
            return (rgbComponent.toFloat() / rgbComponentMaxValue)
        }
        val nR = normalize(this.r)
        val nG = normalize(this.g)
        val nB = normalize(this.b)
        val max = maxOf(nR, nG, nB)
        val min = minOf(nR, nG, nB)
        val delta = max - min

        /*
         * HEX/RGB is discrete (0–255 per channel).
         * HSV decimals beyond ~2 for Hue and ~3–4 for Saturation/Value do not affect the RGB round-trip.
         * Extra precision is harmless but unnecessary.
         */

        val hue = run {
            val rawHue = when {
                delta == 0f -> 0f
                max == nR -> 60 * (((nG - nB) / delta) % 6f)
                max == nG -> 60 * (((nB - nR) / delta) + 2)
                max == nB -> 60 * (((nR - nG) / delta) + 4)
                else -> error("unreachable")
            }
            val hue = if (rawHue < 0f) rawHue + 360f else rawHue
            val rounded = hue.truncateDecimalPlaces(keep = 2)
            return@run rounded
        }
        val saturation = run {
            val saturation = if (max == 0f) 0f else delta / max
            val rounded = saturation.truncateDecimalPlaces(keep = 4)
            return@run rounded
        }
        val value = run {
            val value = max
            val rounded = value.truncateDecimalPlaces(keep = 4)
            return@run rounded
        }
        return Color.Hsv(hue, saturation, value)
    }
}