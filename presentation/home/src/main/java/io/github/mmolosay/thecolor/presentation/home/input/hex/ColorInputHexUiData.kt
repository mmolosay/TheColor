package io.github.mmolosay.thecolor.presentation.home.input.hex

import io.github.mmolosay.thecolor.presentation.color.ColorPrototype
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData

data class ColorInputHexUiData(
    val inputField: ColorInputFieldUiData,
)

fun ColorInputHexUiData.assembleColorPrototype() =
    ColorPrototype.Hex(value = inputField.text)