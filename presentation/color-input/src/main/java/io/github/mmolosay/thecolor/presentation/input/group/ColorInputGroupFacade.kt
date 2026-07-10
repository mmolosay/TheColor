package io.github.mmolosay.thecolor.presentation.input.group

import androidx.compose.runtime.Immutable
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

@Immutable
data class ColorInputGroupFacade(
    val execute: (ColorInputGroupAction) -> Unit,
    val selectedInputType: DomainColorInputType,
    val orderedInputTypes: List<DomainColorInputType>,
)