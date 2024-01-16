package io.github.mmolosay.thecolor.presentation.home.input

import io.github.mmolosay.thecolor.presentation.home.input.ColorInputHexUiData.TrailingButton
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputHexUiData.ViewData
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputHexUiData.ViewModelData

/**
 * All data that is required to correctly display this UI component.
 * Designed in a way so it's convenient for UI to consume: related values are grouped into subclasses.
 */
data class ColorInputHexUiData(
    val input: String,
    val onInputChange: (String) -> Unit,
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
     * Part of to-be [ColorInputHexUiData].
     * Created by `ViewModel`.
     */
    data class ViewModelData(
        val input: String,
        val onInputChange: (String) -> Unit,
        val showTrailingButton: Boolean,
        val onTrailingButtonClick: () -> Unit,
    )

    /**
     * Part of to-be [ColorInputHexUiData].
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
    ColorInputHexUiData(
        input = viewModelData.input,
        onInputChange = viewModelData.onInputChange,
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