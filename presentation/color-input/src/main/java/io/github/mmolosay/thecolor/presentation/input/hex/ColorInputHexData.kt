package io.github.mmolosay.thecolor.presentation.input.hex

import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.utils.AckValue

/**
 * Platform-agnostic data provided by ViewModel to 'HEX Color Input' View.
 */
data class ColorInputHexData(
    val textField: TextFieldData,
    val submission: SubmissionData,
) {

    data class SubmissionData(
        val submitInput: () -> Unit,
        val result: AckValue<ColorInputSubmissionResult>?,
    )
}