package io.github.mmolosay.thecolor.presentation.home.input.rgb

import io.github.mmolosay.thecolor.presentation.color.ColorPrototype
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData

data class ColorInputRgbUiData(
    val rInputField: ColorInputFieldUiData,
    val gInputField: ColorInputFieldUiData,
    val bInputField: ColorInputFieldUiData,
)

fun ColorInputRgbUiData.assembleColorPrototype() =
    ColorPrototype.Rgb(
        r = rInputField.text.toIntOrNull(),
        g = gInputField.text.toIntOrNull(),
        b = bInputField.text.toIntOrNull(),
    )