package io.github.mmolosay.thecolor.presentation.input.impl

import io.github.mmolosay.thecolor.domain.model.ColorInputType as DomainColorInputType

/**
 * Platform-agnostic data provided by ViewModel to 'Color Input' View.
 */
data class ColorInputData(
    val selectedInputType: DomainColorInputType, // it's OK to use some domain models (like enums) in presentation layer
    val orderedInputTypes: List<DomainColorInputType>,
    val onInputTypeChange: (DomainColorInputType) -> Unit,
)