package io.github.mmolosay.thecolor.presentation.input.textfield

import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text
import kotlinx.coroutines.Job

sealed interface TextFieldAction {

    data class SetText(
        val text: Text,
    ) : TextFieldAction

    object ClearTextFeature {
        data object Invoke : TextFieldAction
    }
}

typealias ExecuteTextFieldAction = (TextFieldAction) -> Job