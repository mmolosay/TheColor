package io.github.mmolosay.thecolor.presentation.input.hex

import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldAction

sealed interface ColorInputHexAction {

    data class TextField(
        val action: TextFieldAction,
    ) : ColorInputHexAction

    data object SubmitInput : ColorInputHexAction

    data object AckInputSubmissionResult : ColorInputHexAction
}