package io.github.mmolosay.thecolor.presentation.input.rgb

import androidx.compose.runtime.Immutable
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldFacade

@Immutable
data class ColorInputRgbFacade(
    val rTextField: TextFieldFacade,
    val gTextField: TextFieldFacade,
    val bTextField: TextFieldFacade,
    val execute: (ColorInputRgbAction) -> Unit,
    val inputSubmissionResult: ColorInputSubmissionResult?,
    val isSmartBackspaceEnabled: Boolean,
)