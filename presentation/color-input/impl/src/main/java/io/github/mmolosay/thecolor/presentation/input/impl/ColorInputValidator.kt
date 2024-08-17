package io.github.mmolosay.thecolor.presentation.input.impl

import io.github.mmolosay.thecolor.domain.usecase.ColorFactory
import io.github.mmolosay.thecolor.presentation.input.api.ColorInput
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputState
import io.github.mmolosay.thecolor.presentation.input.api.isInFullForm
import io.github.mmolosay.thecolor.presentation.input.api.isInShortForm
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Validates given [ColorInput] and returns the result as [ColorInputState].
 */
// TODO: add unit tests
@Singleton
class ColorInputValidator @Inject constructor(
    private val colorInputMapper: ColorInputMapper,
    private val colorFactory: ColorFactory,
) {

    fun ColorInput.validate(): ColorInputState {
        val isEmpty = this.isEmpty()
        val isCompleteFromUserPerspective = this.isCompleteFromUserPerspective()
        fun invalidState() = ColorInputState.Invalid(isEmpty, isCompleteFromUserPerspective)
        if (isEmpty || !isCompleteFromUserPerspective) return invalidState()

        val prototype = with(colorInputMapper) { toPrototype() }
        val color = colorFactory.from(prototype) ?: return invalidState()
        return ColorInputState.Valid(color)
    }
}

private fun ColorInput.isEmpty(): Boolean =
    when (this) {
        is ColorInput.Hex -> string.isEmpty()
        is ColorInput.Rgb -> r.isEmpty() && g.isEmpty() && b.isEmpty()
    }

/**
 * Imagine user enters "1" into hex color input View.
 * From data validation perspective, it is a valid, finished color: 1 == 0x1 == 0x000001 == RGB(0, 0, 1).
 * But from user perspective it is not a completed color. They do not consider "invisible" leading zeros.
 * User intends to enter "1______", and in their mind this "1" is not at the end, but at the beginning of future color.
 */
private fun ColorInput.isCompleteFromUserPerspective(): Boolean =
    when (this) {
        is ColorInput.Hex -> isInShortForm || isInFullForm
        is ColorInput.Rgb -> r.isNotEmpty() && g.isNotEmpty() && b.isNotEmpty()
    }