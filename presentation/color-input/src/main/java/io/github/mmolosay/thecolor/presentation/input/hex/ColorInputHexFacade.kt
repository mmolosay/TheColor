package io.github.mmolosay.thecolor.presentation.input.hex

import androidx.compose.runtime.Immutable
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldFacade

@Immutable
data class ColorInputHexFacade(
    val textField: TextFieldFacade,
    val execute: (ColorInputHexAction) -> Unit,
    val inputSubmissionResult: ColorInputSubmissionResult?,
)