package io.github.mmolosay.thecolor.presentation.input.hex

import io.github.mmolosay.thecolor.presentation.input.field.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput

/**
 * Platform-agnostic data provided by ViewModel to HEX color input View.
 */
data class ColorInputHexData(
    val textField: TextFieldData,
)

fun ColorInputHexData.assembleColorInput() =
    ColorInput.Hex(string = textField.text.string)