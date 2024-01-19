package io.github.mmolosay.thecolor.input.hex

import io.github.mmolosay.thecolor.presentation.color.ColorInput
import io.github.mmolosay.thecolor.input.field.TextFieldUiData

data class ColorInputHexUiData(
    val textField: TextFieldUiData,
)

fun ColorInputHexUiData.assembleColorInput() =
    ColorInput.Hex(string = textField.text.string)