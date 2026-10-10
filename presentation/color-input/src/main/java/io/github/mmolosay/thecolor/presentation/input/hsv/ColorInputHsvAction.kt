package io.github.mmolosay.thecolor.presentation.input.hsv

import io.github.mmolosay.thecolor.presentation.common.ExecuteAction

sealed interface ColorInputHsvAction {

    data class SetHue(
        val hue: Float,
    ) : ColorInputHsvAction

    data class SetSaturationAndValue(
        val saturation: Float,
        val value: Float,
    ) : ColorInputHsvAction
}

typealias ExecuteColorInputHsvAction = ExecuteAction<ColorInputHsvAction>