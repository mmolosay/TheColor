package io.github.mmolosay.thecolor.presentation.input.group

import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

data class ColorInputGroupState(
    val preferredType: DomainColorInputType?,
    val selectedType: DomainColorInputType?,
)