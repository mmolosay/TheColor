package io.github.mmolosay.thecolor.presentation.input.group

import kotlinx.coroutines.Job
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

data class ColorInputGroupFacade(
    val selectedInputType: DomainColorInputType,
    val orderedInputTypes: List<DomainColorInputType>,
    val execute: (ColorInputGroupAction) -> Job,
)