package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.Color
import javax.inject.Inject

/**
 * Compares colors of (possibly) different color spaces.
 */
class ColorComparator @Inject constructor(
    private val colorConverter: ColorConverter,
) {

    // syntactic sugar
    infix fun Color.isSameAs(other: Color): Boolean =
        compare(c1 = this, c2 = other)

    fun compare(c1: Color, c2: Color): Boolean {
        val hex1 = with(colorConverter) { c1.toHex() }
        val hex2 = with(colorConverter) { c2.toHex() }
        return (hex1 == hex2)
    }
}