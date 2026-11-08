package io.github.mmolosay.thecolor.demo.presentation.feature.ounces

import io.github.mmolosay.thecolor.demo.domain.DecimalSeparator
import io.github.mmolosay.thecolor.demo.presentation.feature.input.Submission
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldState
import io.github.mmolosay.thecolor.utils.Lens

data class OuncesInputState(
    val textField: TextFieldState,
    val submission: Submission?,
    val separator: DecimalSeparator, // the setting; the text holds a point either way
)

object OuncesInputStateLenses {

    val textField = Lens<OuncesInputState, TextFieldState>(
        get = { state -> state.textField },
        set = { state, textField -> state.copy(textField = textField) },
    )
}

sealed interface OuncesInputOutcome {
    data object Submitted : OuncesInputOutcome // the parent reads the amount from the state
}
