package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvState

object MockColorInputStates {

    fun ColorInputHsvState(color: Color.Hsv?): ColorInputHsvState =
        ColorInputHsvState(
            displayColor = color,
            color = color,
        )
}
