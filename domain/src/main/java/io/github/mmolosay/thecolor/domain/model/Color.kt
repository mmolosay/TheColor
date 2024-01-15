package io.github.mmolosay.thecolor.domain.model

object Color {

    data class Hex(
        val value: String,
    )

    data class Rgb(
        val r: Int,
        val g: Int,
        val b: Int,
    )
}