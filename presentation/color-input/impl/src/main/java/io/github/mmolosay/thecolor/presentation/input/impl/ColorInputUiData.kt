package io.github.mmolosay.thecolor.presentation.input.impl

import io.github.mmolosay.thecolor.domain.model.ColorInputType as DomainColorInputType

/**
 * Framework-oriented data required for color input View to be presented by Compose.
 */
data class ColorInputUiData(
    val selectedInputType: DomainColorInputType, // it's OK to use some domain models (like enums) in presentation layer
    val orderedInputTypes: List<DomainColorInputType>,
    val onInputTypeChange: (DomainColorInputType) -> Unit,
    val hexLabel: String,
    val rgbLabel: String,
)