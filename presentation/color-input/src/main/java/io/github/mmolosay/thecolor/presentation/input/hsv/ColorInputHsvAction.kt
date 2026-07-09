package io.github.mmolosay.thecolor.presentation.input.hsv

import io.github.mmolosay.thecolor.domain.color.Color

sealed interface ColorInputHsvAction {

    data class SetColor(
        val color: Color.Hsv,
    ) : ColorInputHsvAction
}