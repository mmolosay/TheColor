package io.github.mmolosay.thecolor.presentation.color

import kotlinx.parcelize.Parcelize

/**
 * Immutable presentation of color prototype of some color space.
 * May be not valid color and contain `null`s.
 */
sealed class ColorPrototype : AbstractColor() {

    /**
     * Presentation of HEX color.
     *
     * @param value __signless__ HEX color `String`, e.g. "16A8C0".
     */
    @Parcelize
    data class Hex(
        val value: String?
    ) : ColorPrototype() {

        companion object
    }

    /**
     * Presentation of RGB color. May contain non-valid color or `null`s.
     *
     * @param r R component in range (0-255) including both start and end.
     * @param g G component in range (0-255) including both start and end.
     * @param b B component in range (0-255) including both start and end.
     */
    @Parcelize
    data class Rgb(
        val r: Int?,
        val g: Int?,
        val b: Int?
    ) : ColorPrototype() {

        override fun equals(other: Any?): Boolean {
            return super.equals(other)
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + (r ?: 0)
            result = 31 * result + (g ?: 0)
            result = 31 * result + (b ?: 0)
            return result
        }

        companion object
    }
}