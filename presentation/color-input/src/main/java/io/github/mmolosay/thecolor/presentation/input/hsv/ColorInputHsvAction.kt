package io.github.mmolosay.thecolor.presentation.input.hsv

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.presentation.common.ExecuteAction

sealed interface ColorInputHsvAction {

    data class SetColor(
        val color: Color.Hsv,
    ) : ColorInputHsvAction
}

typealias ExecuteColorInputHsvAction = ExecuteAction<ColorInputHsvAction>