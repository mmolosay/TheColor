package io.github.mmolosay.thecolor.presentation.input.rgb

import androidx.compose.runtime.Immutable
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldFacade
import io.github.mmolosay.thecolor.utils.AckValue

@Immutable
interface ColorInputRgbFacade {
    val rTextField: TextFieldFacade
    val gTextField: TextFieldFacade
    val bTextField: TextFieldFacade

    val isSmartBackspaceEnabled: Boolean

    fun submitInput()
    val inputSubmissionResult: AckValue<ColorInputSubmissionResult>?
}