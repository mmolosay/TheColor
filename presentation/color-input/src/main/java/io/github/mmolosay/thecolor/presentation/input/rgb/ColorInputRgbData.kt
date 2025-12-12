package io.github.mmolosay.thecolor.presentation.input.rgb

import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData

/**
 * Platform-agnostic data provided by ViewModel to 'RGB Color Input' View.
 */
data class ColorInputRgbData(
    val rTextField: TextFieldData,
    val gTextField: TextFieldData,
    val bTextField: TextFieldData,
    val submitInput: () -> Unit,
    val isSmartBackspaceEnabled: Boolean,
)