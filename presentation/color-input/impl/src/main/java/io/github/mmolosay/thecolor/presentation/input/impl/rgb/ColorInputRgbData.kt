package io.github.mmolosay.thecolor.presentation.input.impl.rgb

import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.impl.model.ColorSubmissionResult

/**
 * Platform-agnostic data provided by ViewModel to 'RGB Color Input' View.
 */
data class ColorInputRgbData(
    val rTextField: TextFieldData,
    val gTextField: TextFieldData,
    val bTextField: TextFieldData,
    val submitColor: () -> Unit,
    val isSmartBackspaceEnabled: Boolean,
)