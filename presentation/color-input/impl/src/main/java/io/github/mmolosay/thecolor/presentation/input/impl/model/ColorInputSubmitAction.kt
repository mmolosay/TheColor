package io.github.mmolosay.thecolor.presentation.input.impl.model

/**
 * An action executed when user submits a color (or whatever they input).
 *
 * Provided to the 'Color Input' feature from the outside (by the caller / parent).
 */
fun interface ColorInputSubmitAction {

    /**
     * @return `true` if the submitted [colorInput] was accepted and user will pause their
     * interaction with 'Color Input' for some time. Otherwise `false`.
     */
    operator fun invoke(
        colorInput: ColorInput,
        validationResult: ColorInputValidationResult,
    ): Boolean
}