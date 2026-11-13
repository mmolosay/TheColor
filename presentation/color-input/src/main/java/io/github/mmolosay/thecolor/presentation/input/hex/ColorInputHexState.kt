package io.github.mmolosay.thecolor.presentation.input.hex

import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmission
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldState
import io.github.mmolosay.thecolor.utils.Lens

data class ColorInputHexState(
    val textField: TextFieldState,
    val submission: ColorInputSubmission?,
)

object ColorInputHexStateLenses {
    val textField = Lens<ColorInputHexState, TextFieldState>(
        get = { s -> s.textField },
        set = { s, v -> s.copy(textField = v) },
    )
}

sealed interface ColorInputHexOutcome {
    data object Submitted : ColorInputHexOutcome
}