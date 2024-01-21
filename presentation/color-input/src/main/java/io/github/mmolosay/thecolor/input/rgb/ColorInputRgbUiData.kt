package io.github.mmolosay.thecolor.input.rgb

import io.github.mmolosay.thecolor.input.ColorInput
import io.github.mmolosay.thecolor.input.field.TextFieldUiData

data class ColorInputRgbUiData(
    val rTextField: TextFieldUiData,
    val gTextField: TextFieldUiData,
    val bTextField: TextFieldUiData,
)

fun ColorInputRgbUiData.assembleColorInput() =
    ColorInput.Rgb(
        r = rTextField.text.string,
        g = gTextField.text.string,
        b = bTextField.text.string,
    )