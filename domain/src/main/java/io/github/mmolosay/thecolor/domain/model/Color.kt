package io.github.mmolosay.thecolor.domain.model

object Color {

    /**
     * Color in no particular color space.
     * Serves as an adapter between different color spaces.
     */
    interface Abstract

    data class Hex(
        val value: String,
    )

    data class Rgb(
        val r: Int,
        val g: Int,
        val b: Int,
    )
}