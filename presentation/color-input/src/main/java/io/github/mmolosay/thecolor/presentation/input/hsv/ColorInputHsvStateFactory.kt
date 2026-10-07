package io.github.mmolosay.thecolor.presentation.input.hsv

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import javax.inject.Inject

class ColorInputHsvStateFactory @Inject constructor(
    private val colorConverter: ColorConverter,
) {
    fun create(color: Color?): ColorInputHsvState =
        ColorInputHsvState(
            displayColor = with(colorConverter) { color?.toHsv() },
            color = color,
        )
}