package io.github.mmolosay.thecolor.input

import io.github.mmolosay.thecolor.domain.model.ColorPrototype
import io.github.mmolosay.thecolor.presentation.color.ColorInput
import javax.inject.Inject
import io.github.mmolosay.thecolor.domain.model.Color as DomainColor

/**
 * Maps [ColorInput] to and from domain models.
 */
// TODO: this component is in ":color-input" module, so should be 'ColorInput'
class ColorInputMapper @Inject constructor() {

    fun ColorInput.toPrototype(): ColorPrototype =
        when (this) {
            is ColorInput.Hex -> this.toPrototype()
            is ColorInput.Rgb -> this.toPrototype()
        }

    fun ColorInput.Hex.toPrototype(): ColorPrototype.Hex =
        ColorPrototype.Hex(
            value = this.string.toIntOrNull(radix = 16),
        )

    fun ColorInput.Rgb.toPrototype(): ColorPrototype.Rgb =
        ColorPrototype.Rgb(
            r = this.r.toIntOrNull(),
            g = this.g.toIntOrNull(),
            b = this.b.toIntOrNull(),
        )

    @OptIn(ExperimentalStdlibApi::class)
    fun DomainColor.Hex.toColorInput(): ColorInput.Hex {
        val string = this.value
            .toHexString(format = HexFormat.UpperCase)
            .trimStart { it == '0' }
            .padStart(6, '0')
        return ColorInput.Hex(string)
    }

    fun DomainColor.Rgb.toColorInput(): ColorInput.Rgb =
        ColorInput.Rgb(
            r = this.r.toString(),
            g = this.g.toString(),
            b = this.b.toString(),
        )
}