package io.github.mmolosay.thecolor.demo.presentation.feature.grams

import io.github.mmolosay.thecolor.demo.presentation.feature.input.Submission
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldState
import io.github.mmolosay.thecolor.utils.Lens

data class GramsInputState(
    val textField: TextFieldState,
    val submission: Submission?,
)

object GramsInputStateLenses {

    val textField = Lens<GramsInputState, TextFieldState>(
        get = { state -> state.textField },
        set = { state, textField -> state.copy(textField = textField) },
    )
}

sealed interface GramsInputOutcome {
    data object Submitted : GramsInputOutcome // the parent reads the amount from the state
}
