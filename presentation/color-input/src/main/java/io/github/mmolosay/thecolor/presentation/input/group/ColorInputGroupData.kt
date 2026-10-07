package io.github.mmolosay.thecolor.presentation.input.group

import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexData
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvData
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbData
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

/**
 * Platform-agnostic data provided by ViewModel to 'Color Input Group' View.
 */
data class ColorInputGroupData(
    val hex: ColorInputHexData,
    val rgb: ColorInputRgbData,
    val hsv: ColorInputHsvData,
    val selectedInputType: DomainColorInputType, // it's OK to use some domain models (like enums) in presentation layer
    val orderedInputTypes: List<DomainColorInputType>,
)