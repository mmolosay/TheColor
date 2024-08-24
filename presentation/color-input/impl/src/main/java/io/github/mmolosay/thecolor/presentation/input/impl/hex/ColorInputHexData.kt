package io.github.mmolosay.thecolor.presentation.input.impl.hex

import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData

/**
 * Platform-agnostic data provided by ViewModel to HEX color input View.
 */
data class ColorInputHexData(
    val textField: TextFieldData,
    val submitColor: () -> Unit,
    val colorSubmissionResult: ColorSubmissionResult?,
) {

    /**
     * Result of invoking [submitColor()][submitColor] action.
     * Use [discard()][discard] to notify ViewModel that UI has conveyed this event to user and no longer needed.
     */
    data class ColorSubmissionResult(
        val wasAccepted: Boolean,
        val discard: () -> Unit,
    )
}