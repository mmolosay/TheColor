package io.github.mmolosay.thecolor.domain.model

/**
 * Represents a valid, opaque color.
 */
// TODO: add .toString() methods to derivatives for easier presentation in debugger
sealed interface Color {

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