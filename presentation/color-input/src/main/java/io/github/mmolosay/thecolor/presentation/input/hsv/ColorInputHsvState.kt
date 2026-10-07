package io.github.mmolosay.thecolor.presentation.input.hsv

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter

data class ColorInputHsvState(
    val displayColor: Color.Hsv?, // what the View presents
    val color: Color?, // the applied color
)

fun ColorInputHsvState.toData(): ColorInputHsvData =
    ColorInputHsvData(
        color = this.displayColor,
    )

context(
    converter: ColorConverter,
)
fun ColorInputHsvState.withColor(color: Color?): ColorInputHsvState {
    return this.copy(
        displayColor = with(converter) { color?.toHsv() },
        color = color,
    )
}