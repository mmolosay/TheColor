package io.github.mmolosay.thecolor.presentation.input.impl.hex

import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.impl.model.ColorSubmissionResult

/**
 * Platform-agnostic data provided by ViewModel to 'HEX Color Input' View.
 */
data class ColorInputHexData(
    val textField: TextFieldData,
    val submitColor: () -> Unit,
)