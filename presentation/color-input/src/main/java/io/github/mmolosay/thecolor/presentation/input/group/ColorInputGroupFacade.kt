package io.github.mmolosay.thecolor.presentation.input.group

import androidx.compose.runtime.Immutable
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexFacade
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvFacade
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbFacade
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

@Immutable
data class ColorInputGroupFacade(
    val hex: ColorInputHexFacade,
    val rgb: ColorInputRgbFacade,
    val hsv: ColorInputHsvFacade,
    val execute: (ColorInputGroupAction) -> Unit,
    val selectedInputType: DomainColorInputType,
    val orderedInputTypes: List<DomainColorInputType>,
)