package io.github.mmolosay.thecolor.presentation.input.hsv

import io.github.mmolosay.thecolor.domain.color.Color
import kotlinx.coroutines.Job

sealed interface ColorInputHsvAction {

    data class SetColor(
        val color: Color.Hsv,
    ) : ColorInputHsvAction
}

typealias ExecuteColorInputHsvAction = (ColorInputHsvAction) -> Job