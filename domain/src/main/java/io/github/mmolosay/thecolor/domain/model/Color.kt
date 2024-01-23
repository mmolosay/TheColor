package io.github.mmolosay.thecolor.domain.model

/**
 * Represents a valid color.
 */
sealed interface Color {

    /**
     * Color in no particular color space.
     * Serves as an adapter between different color spaces.
     */
    data class Abstract internal constructor(internal val int: Int) : Color

    data class Hex(
        val value: Int,
    ) : Color

    data class Rgb(
        val r: Int,
        val g: Int,
        val b: Int,
    ) : Color
}