package io.github.mmolosay.thecolor.presentation.home.input

data class ColorInputFieldUiData(
    val text: String,
    val onTextChange: (String) -> Unit,
    val processText: (String) -> String,
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