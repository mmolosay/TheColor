package io.github.mmolosay.thecolor.presentation.home.input

import io.github.mmolosay.thecolor.presentation.home.input.ColorInputFieldUiData.TrailingButton
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputFieldUiData.ViewData
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputFieldUiData.ViewModelData

/**
 * All data that is required to correctly display this UI component.
 * Designed in a way so it's convenient for UI to consume: related values are grouped into subclasses.
 */
data class ColorInputFieldUiData(
    val text: String,
    val onTextChange: (String) -> Unit,
    val processText: (String) -> String,
    val label: String,
    val placeholder: String,
    val prefix: String,
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
     * Created by `ViewModel`.
     */
    data class ViewModelData(
        val text: String,
        val onTextChange: (String) -> Unit,
        val processText: (String) -> String,
        val showTrailingButton: Boolean,
        val onTrailingButtonClick: () -> Unit,
    )

    /**
     * Part of to-be [ColorInputFieldUiData].
     * Created by `View`, since string resources are tied to platform-specific
     * components (like `Context`), which should be avoided in `ViewModel`s.
     */
    data class ViewData(
        val label: String,
        val placeholder: String,
        val prefix: String,
        val trailingIconContentDesc: String,
    )
}

operator fun ViewModelData.plus(viewData: ViewData) =
    uiData(viewModelData = this, viewData = viewData)

private fun uiData(
    viewModelData: ViewModelData,
    viewData: ViewData,
) =
    ColorInputFieldUiData(
        text = viewModelData.text,
        onTextChange = viewModelData.onTextChange,
        processText = viewModelData.processText,
        label = viewData.label,
        placeholder = viewData.placeholder,
        prefix = viewData.prefix,
        trailingButton = trailingButton(viewModelData, viewData),
    )

private fun trailingButton(
    viewModelData: ViewModelData,
    viewData: ViewData,
): TrailingButton =
    when (viewModelData.showTrailingButton) {
        false -> TrailingButton.Hidden
        true -> TrailingButton.Visible(
            onClick = viewModelData.onTrailingButtonClick,
            iconContentDesc = viewData.trailingIconContentDesc,
        )
    }