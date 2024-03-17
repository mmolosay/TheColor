package io.github.mmolosay.thecolor.domain.model

/**
 * Represents a valid, opaque color.
 */
sealed interface Color {

    /**
     * Color in no particular color space.
     * Serves as an adapter between different color spaces.
     */
    data class Abstract internal constructor(internal val int: Int) : Color

    /**
     * Solid color in RRGGBB format without alpha channel.
     * Example: `value = 0x1A803F`.
     */
    data class Hex(
        val value: Int,
    ) : Color

    data class Rgb(
        val r: Int,
        val g: Int,
        val b: Int,
    ) : Color
}