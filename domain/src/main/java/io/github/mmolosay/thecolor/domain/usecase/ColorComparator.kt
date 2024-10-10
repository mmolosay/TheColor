package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.Color
import javax.inject.Inject

/**
 * Compares colors of (possibly) different color spaces.
 */
class ColorComparator @Inject constructor(
    private val colorConverter: ColorConverter,
) {

    infix fun Color.isSameAs(other: Color): Boolean {
        val thisAsHex = with(colorConverter) { toHex() }
        val otherAsHex = with(colorConverter) { other.toHex() }
        return (thisAsHex == otherAsHex)
    }
}