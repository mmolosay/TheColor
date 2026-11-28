package io.github.mmolosay.thecolor.presentation.input.rgb

import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmission
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldState
import io.github.mmolosay.thecolor.utils.Lens

data class ColorInputRgbState(
    val r: TextFieldState,
    val g: TextFieldState,
    val b: TextFieldState,
    val submission: ColorInputSubmission?,
    val isSmartBackspaceEnabled: Boolean,
)

object ColorInputRgbStateLenses {
    val r = Lens<ColorInputRgbState, TextFieldState>(
        get = { s -> s.r },
        set = { s, v -> s.copy(r = v) },
    )
    val g = Lens<ColorInputRgbState, TextFieldState>(
        get = { s -> s.g },
        set = { s, v -> s.copy(g = v) },
    )
    val b = Lens<ColorInputRgbState, TextFieldState>(
        get = { s -> s.b },
        set = { s, v -> s.copy(b = v) },
    )
}

sealed interface ColorInputRgbOutcome {
    data object Submitted : ColorInputRgbOutcome
}