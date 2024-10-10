package io.github.mmolosay.thecolor.presentation.input.impl.model

/*
 * This file contains factory functions for creating Color Input UI commands.
 * These functions operate with common models that are used in all types of Color Input Views.
 */

internal fun hideSoftwareKeyboardCommandOrNull(
    result: ColorSubmissionResult?,
): ColorInputUiCommand.HideSoftwareKeyboard? {
    result ?: return null
    // color input was rejected, thus user will probably want to correct it and needs keyboard
    if (result.wasAccepted.not()) return null
    // color input was accepted, thus user probably won't change it and doesn't need keyboard
    return ColorInputUiCommand.HideSoftwareKeyboard(onExecuted = result.discard)
}