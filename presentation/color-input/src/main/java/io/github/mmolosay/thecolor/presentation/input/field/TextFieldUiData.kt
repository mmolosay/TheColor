package io.github.mmolosay.thecolor.presentation.input.field

import io.github.mmolosay.thecolor.presentation.input.field.TextFieldData.Text

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

    /**
     * Part of to-be [TextFieldUiData].
     * Framework-oriented.
     * Created by View, since string resources are tied to platform-specific
     * components (like Context), which should be avoided in ViewModels.
     */
    data class ViewData(
        val label: String,
        val placeholder: String,
        val prefix: String?,
        val trailingIcon: TrailingIcon,
    ) {

        sealed interface TrailingIcon {
            data object None : TrailingIcon
            data class Exists(val contentDesc: String) : TrailingIcon
        }
    }
}