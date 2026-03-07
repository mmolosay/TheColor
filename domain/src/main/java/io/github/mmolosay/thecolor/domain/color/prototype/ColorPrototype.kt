package io.github.mmolosay.thecolor.domain.color.prototype

/**
 * A prototype of to-be [io.github.mmolosay.thecolor.domain.color.Color].
 * May represent not valid color, contain `null`s and values out of range.
 */
sealed interface ColorPrototype {

    data class Hex(
        val value: Int?,
    ) : ColorPrototype

    data class Rgb(
        val r: Int?,
        val g: Int?,
        val b: Int?,
    ) : ColorPrototype
}