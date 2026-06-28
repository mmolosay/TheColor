package io.github.mmolosay.thecolor.presentation.input.hsv

import androidx.compose.runtime.Immutable
import io.github.mmolosay.thecolor.domain.color.Color

@Immutable
interface ColorInputHsvFacade {
    val color: Color.Hsv?
    fun setColor(color: Color.Hsv)
}