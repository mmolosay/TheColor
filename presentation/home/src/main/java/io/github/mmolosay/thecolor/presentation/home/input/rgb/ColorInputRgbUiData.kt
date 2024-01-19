package io.github.mmolosay.thecolor.presentation.home.input.rgb

import io.github.mmolosay.thecolor.presentation.color.ColorInput
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData

data class ColorInputRgbUiData(
    val rInputField: ColorInputFieldUiData,
    val gInputField: ColorInputFieldUiData,
    val bInputField: ColorInputFieldUiData,
)

fun ColorInputRgbUiData.areAllInputsEmpty() =
    listOf(rInputField, gInputField, bInputField)
        .all { it.text.string.isEmpty() }

fun ColorInputRgbUiData.assembleColorInput() =
    ColorInput.Rgb(
        r = rInputField.text.string,
        g = gInputField.text.string,
        b = bInputField.text.string,
    )