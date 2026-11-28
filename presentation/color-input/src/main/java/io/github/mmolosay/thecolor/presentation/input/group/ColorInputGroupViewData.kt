package io.github.mmolosay.thecolor.presentation.input.group

import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

data class ColorInputGroupViewData(
    val selectedType: DomainColorInputType,
    val orderedTypes: List<DomainColorInputType>,
)