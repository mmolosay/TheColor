package io.github.mmolosay.thecolor.input.hex

import io.github.mmolosay.thecolor.presentation.color.ColorInput
import io.github.mmolosay.thecolor.input.field.ColorInputFieldUiData

data class ColorInputHexUiData(
    val inputField: ColorInputFieldUiData,
)

fun ColorInputHexUiData.assembleColorInput() =
    ColorInput.Hex(string = inputField.text.string)