package io.github.mmolosay.thecolor.domain.color.prototype

import io.github.mmolosay.thecolor.domain.color.Color
import javax.inject.Inject

/**
 * Checks whether provided [ColorPrototype] is a valid color or not.
 */
// kotlin contracts for parameter properties are not supported at the moment
class ColorPrototypeValidator @Inject constructor() {

    fun ColorPrototype.isValid(): Boolean =
        when (this) {
            is ColorPrototype.Hex -> this.isValid()
            is ColorPrototype.Rgb -> this.isValid()
        }

    fun ColorPrototype.Hex.isValid(): Boolean {
        value ?: return false
        return (value in Color.Hex.Range)
    }

    fun ColorPrototype.Rgb.isValid(): Boolean {
        if ((r == null || g == null || b == null)) return false
        return listOf(r, g, b).all { it in Color.Rgb.ComponentRange }
    }
}