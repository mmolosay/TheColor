package io.github.mmolosay.thecolor.presentation.input.group

import androidx.compose.runtime.Immutable
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexFacade
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbFacade
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

@Immutable
interface ColorInputGroupFacade {
    val hex: ColorInputHexFacade
    val rgb: ColorInputRgbFacade
    // TODO: add HSV here after migration

    val orderedInputTypes: List<DomainColorInputType>
    val selectedInputType: DomainColorInputType
    fun changeInputType(type: DomainColorInputType)
}