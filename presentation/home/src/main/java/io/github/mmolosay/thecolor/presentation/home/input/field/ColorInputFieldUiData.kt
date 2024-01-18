package io.github.mmolosay.thecolor.presentation.home.input.field

/**
 * @param onTextChange a callback to be invoked when displayed text is changed.
 * It should receive a result of [filterUserInput] or other text that is presentation-ready.
 *
 * @param filterUserInput filters text from user input and returns processed text to be displayed.
 */
data class ColorInputFieldUiData(
    val text: String, // TODO: custom type?
    val onTextChange: (String) -> Unit,
    val filterUserInput: (String) -> String,
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
     * Part of to-be [ColorInputFieldUiData].
     * Created by `View`, since string resources are tied to platform-specific
     * components (like `Context`), which should be avoided in `ViewModel`s.
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