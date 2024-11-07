package io.github.mmolosay.thecolor.presentation.input.impl.field

import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData.Text

/**
 * Framework-oriented data required for text field View to be presented by Compose.
 */
data class TextFieldUiData(
    val text: Text,
    val onTextChange: (Text) -> Unit,
    val filterUserInput: (String) -> Text,
    val label: String,
    val placeholder: String,
    val prefix: String?,
    val trailingButton: TrailingButton,
) {

    sealed interface TrailingButton {
        data object Hidden : TrailingButton
        data class Visible(
            val onClick: () -> Unit,
            val iconContentDesc: String,
        ) : TrailingButton
    }

}