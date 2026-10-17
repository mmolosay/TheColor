package io.github.mmolosay.thecolor.demo.grams

import io.github.mmolosay.thecolor.demo.input.Submission
import io.github.mmolosay.thecolor.demo.textfield.TextFieldState

data class GramsInputState(
    val textField: TextFieldState,
    val submission: Submission?,
)

sealed interface GramsInputOutcome {
    data object Submitted : GramsInputOutcome // the parent reads the amount from the state
}
