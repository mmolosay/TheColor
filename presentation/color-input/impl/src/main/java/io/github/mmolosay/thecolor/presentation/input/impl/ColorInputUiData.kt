package io.github.mmolosay.thecolor.presentation.input.impl

import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputData.ViewType

/**
 * Framework-oriented data required for color input View to be presented by Compose.
 */
data class ColorInputUiData(
    val selectedViewType: ViewType,
    val orderedViewTypes: List<ViewType>,
    val onInputTypeChange: (ViewType) -> Unit,
    val hexLabel: String,
    val rgbLabel: String,
)