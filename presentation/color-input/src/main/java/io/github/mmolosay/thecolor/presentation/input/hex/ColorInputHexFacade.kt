package io.github.mmolosay.thecolor.presentation.input.hex

import androidx.compose.runtime.Immutable
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldFacade
import io.github.mmolosay.thecolor.utils.AckValue

@Immutable
interface ColorInputHexFacade {
    val textField: TextFieldFacade

    fun submitInput()
    val inputSubmissionResult: AckValue<ColorInputSubmissionResult>?
}