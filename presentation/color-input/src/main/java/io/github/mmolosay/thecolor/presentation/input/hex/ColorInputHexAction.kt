package io.github.mmolosay.thecolor.presentation.input.hex

import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldAction

sealed interface ColorInputHexAction {

    data object SubmitInput : ColorInputHexAction

    data class TextField(val wrapped: TextFieldAction) : ColorInputHexAction
}