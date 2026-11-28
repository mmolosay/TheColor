package io.github.mmolosay.thecolor.domain.color

import javax.inject.Inject

/**
 * Compares colors of (possibly) different color spaces.
 */
class ColorComparator @Inject constructor(
    private val colorConverter: ColorConverter,
) {

    fun compare(c1: Color, c2: Color): Boolean {
        val hex1 = with(colorConverter) { c1.toHex() }
        val hex2 = with(colorConverter) { c2.toHex() }
        return (hex1 == hex2)
    }
}

context(colorComparator: ColorComparator)
infix fun Color?.isSameAs(other: Color?): Boolean {
    val c1 = this; val c2 = other
    if (c1 == null || c2 == null) return (c1 == c2)
    return colorComparator.compare(c1, c2)
}