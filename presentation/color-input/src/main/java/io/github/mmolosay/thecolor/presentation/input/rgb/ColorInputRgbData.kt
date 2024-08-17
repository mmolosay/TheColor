package io.github.mmolosay.thecolor.presentation.input.rgb

import io.github.mmolosay.thecolor.presentation.input.field.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput

/**
 * Platform-agnostic data provided by ViewModel to RGB color input View.
 */
data class ColorInputRgbData(
    val rTextField: TextFieldData,
    val gTextField: TextFieldData,
    val bTextField: TextFieldData,
    val submitColor: () -> Unit,
)