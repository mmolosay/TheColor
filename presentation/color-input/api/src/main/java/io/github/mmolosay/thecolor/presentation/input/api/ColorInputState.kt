package io.github.mmolosay.thecolor.presentation.input.api

import io.github.mmolosay.thecolor.domain.model.Color

/**
 * Depicts a result of validation of some [ColorInput].
 */
sealed interface ColorInputState {

    data class Valid(val color: Color) : ColorInputState

    data class Invalid(
        val isEmpty: Boolean,
        val isCompleteFromUserPerspective: Boolean,
    ) : ColorInputState
}