package io.github.mmolosay.thecolor.presentation.input.hex

import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData

/**
 * Platform-agnostic data provided by ViewModel to 'HEX Color Input' View.
 */
data class ColorInputHexData(
    val textField: TextFieldData,
    val submitInput: () -> Unit,
)