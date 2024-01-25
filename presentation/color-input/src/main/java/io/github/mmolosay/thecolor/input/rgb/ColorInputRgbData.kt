package io.github.mmolosay.thecolor.input.rgb

import io.github.mmolosay.thecolor.input.field.TextFieldData
import io.github.mmolosay.thecolor.input.model.ColorInput

/**
 * Platform-agnostic data provided by ViewModel to RGB color input View.
 */
data class ColorInputRgbData(
    val rTextField: TextFieldData,
    val gTextField: TextFieldData,
    val bTextField: TextFieldData,
)

fun ColorInputRgbData.assembleColorInput() =
    ColorInput.Rgb(
        r = rTextField.text.string,
        g = gTextField.text.string,
        b = bTextField.text.string,
    )