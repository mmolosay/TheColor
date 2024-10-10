package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.ColorPrototype
import javax.inject.Inject

/**
 * Checks whether provided [ColorPrototype] is a valid color or not.
 */
// TODO: kotlin contracts for parameter properties are not supported at the moment
class ColorPrototypeValidator @Inject constructor() {

    fun ColorPrototype.isValid(): Boolean =
        when (this) {
            is ColorPrototype.Hex -> this.isValid()
            is ColorPrototype.Rgb -> this.isValid()
        }

    fun ColorPrototype.Hex.isValid(): Boolean {
        value ?: return false
        fun inRange() = value in 0x000000..0xFFFFFF
        return inRange()
    }

    fun ColorPrototype.Rgb.isValid(): Boolean {
        if ((r == null || g == null || b == null)) return false
        fun Int.isInComponentRange() = this in 0..255
        return (r.isInComponentRange() && g.isInComponentRange() && b.isInComponentRange())
    }
}