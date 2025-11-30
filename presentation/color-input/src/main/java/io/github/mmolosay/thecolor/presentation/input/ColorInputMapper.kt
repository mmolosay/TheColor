package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.model.ColorPrototype
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.isInShortForm
import javax.inject.Inject
import io.github.mmolosay.thecolor.domain.model.Color as DomainColor

/**
 * Maps [ColorInput] to and from domain models.
 */
class ColorInputMapper @Inject constructor() {

    fun ColorInput.toPrototype(): ColorPrototype =
        when (this) {
            is ColorInput.Hex -> this.toPrototype()
            is ColorInput.Rgb -> this.toPrototype()
        }

    fun ColorInput.Hex.toPrototype(): ColorPrototype.Hex {
        val finalString = string.takeUnless { isInShortForm } ?: string.doubleEveryChar()
        return ColorPrototype.Hex(
            value = finalString.toIntOrNull(radix = 16),
        )
    }


    fun ColorInput.Rgb.toPrototype(): ColorPrototype.Rgb =
        ColorPrototype.Rgb(
            r = this.r.toIntOrNull(),
            g = this.g.toIntOrNull(),
            b = this.b.toIntOrNull(),
        )

    fun DomainColor.Hex.toColorInput(): ColorInput.Hex {
        val string = this.value
            .toString(radix = 16)
            .uppercase()
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

    private fun String.doubleEveryChar(): String {
        val result = StringBuilder()
        for (char in this) {
            repeat(2) { result.append(char) }
        }
        return result.toString()
    }
}