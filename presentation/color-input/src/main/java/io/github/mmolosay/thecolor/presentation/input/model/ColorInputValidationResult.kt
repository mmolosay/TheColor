package io.github.mmolosay.thecolor.presentation.input.model

import io.github.mmolosay.thecolor.domain.color.Color

/**
 * Depicts a result of validation / parsing of some [ColorInput].
 */
sealed interface ColorInputValidationResult {

    data class Valid(val color: Color) : ColorInputValidationResult

    data class Invalid(
        val isEmpty: Boolean,
        val isCompleteFromUserPerspective: Boolean,
    ) : ColorInputValidationResult
}

fun ColorInputValidationResult.getColorOrNull(): Color? =
    if (this is ColorInputValidationResult.Valid) this.color else null