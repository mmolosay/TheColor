package io.github.mmolosay.thecolor.presentation.input.rgb

import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldAction

sealed interface ColorInputRgbAction {

    data class TextField(
        val action: TextFieldAction,
        val component: RgbComponent,
    ) : ColorInputRgbAction {

        enum class RgbComponent {
            R, G, B;
        }
    }

    data object SubmitInput : ColorInputRgbAction

    data object AckInputSubmissionResult : ColorInputRgbAction
}