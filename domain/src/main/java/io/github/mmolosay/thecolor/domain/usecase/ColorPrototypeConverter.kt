package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorPrototype
import javax.inject.Inject

/**
 * Converts provided [ColorPrototype] to [Color] or `null`, if prototype doesn't represent a valid color.
 */
class ColorPrototypeConverter @Inject constructor(
    private val colorPrototypeValidator: ColorPrototypeValidator,
) {

    fun ColorPrototype.toColorOrNull(): Color? =
        when (this) {
            is ColorPrototype.Hex -> this.toColorOrNull()
            is ColorPrototype.Rgb -> this.toColorOrNull()
        }

    fun ColorPrototype.Hex.toColorOrNull(): Color.Hex? {
        val valid = with(colorPrototypeValidator) { isValid() }
        if (valid.not()) return null
        return Color.Hex(value = this.value!!) // TODO: use contracts
    }

    fun ColorPrototype.Rgb.toColorOrNull(): Color.Rgb? {
        val valid = with(colorPrototypeValidator) { isValid() }
        if (valid.not()) return null
        return Color.Rgb(r = this.r!!, g = this.g!!, b = this.b!!) // TODO: use contracts
    }
}