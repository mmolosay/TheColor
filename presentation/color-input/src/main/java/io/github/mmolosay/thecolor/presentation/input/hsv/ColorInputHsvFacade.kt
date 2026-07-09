package io.github.mmolosay.thecolor.presentation.input.hsv

import androidx.compose.runtime.Immutable
import io.github.mmolosay.thecolor.domain.color.Color

@Immutable
data class ColorInputHsvFacade(
    val execute: (ColorInputHsvAction) -> Unit,
    val color: Color.Hsv?,
)