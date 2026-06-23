package io.github.mmolosay.thecolor.presentation.input.rgb

import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.utils.ActionWithResult

/**
 * Platform-agnostic data provided by ViewModel to 'RGB Color Input' View.
 */
data class ColorInputRgbData(
    val rTextField: TextFieldData,
    val gTextField: TextFieldData,
    val bTextField: TextFieldData,
    val submitInput: ActionWithResult<ColorInputSubmissionResult>,
    val isSmartBackspaceEnabled: Boolean,
)