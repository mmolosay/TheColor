package io.github.mmolosay.thecolor.presentation.input.rgb

import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldAction

sealed interface ColorInputRgbAction {

    data object SubmitInput : ColorInputRgbAction

    data class TextField(
        val wrapped: TextFieldAction,
        val component: RgbComponent,
    ) : ColorInputRgbAction
}

enum class RgbComponent {
    R, G, B;
}