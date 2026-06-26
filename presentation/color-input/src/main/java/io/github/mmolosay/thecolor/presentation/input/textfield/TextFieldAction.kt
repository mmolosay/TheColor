package io.github.mmolosay.thecolor.presentation.input.textfield

sealed interface TextFieldAction {

    data class SetText(
        val newText: TextFieldData.Text,
    ) : TextFieldAction

    data object ClearText : TextFieldAction
}