package io.github.mmolosay.thecolor.presentation.input.hsv

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorComparator
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.color.isSameAs

data class ColorInputHsvState(
    val displayColor: Color.Hsv?, // what the View presents
    val color: Color.Hsv?, // the applied color
)

fun ColorInputHsvState.toData(): ColorInputHsvData =
    ColorInputHsvData(
        color = this.displayColor.orDefault(),
    )

internal fun Color.Hsv?.orDefault(): Color.Hsv =
    this
        ?: Color.Hsv(
            hue = Color.Hsv.HueRange.start,
            saturation = Color.Hsv.SaturationRange.endInclusive,
            value = Color.Hsv.ValueRange.endInclusive,
        )

context(
    converter: ColorConverter,
    comparator: ColorComparator,
)
fun ColorInputHsvState.withColor(color: Color?): ColorInputHsvState {
    // the same color changes nothing, so a gray keeps its hue, which no HEX or RGB color holds
    if (color isSameAs this.color) return this
    val hsvColor = with(converter) { color?.toHsv() }
    return this.copy(
        displayColor = hsvColor,
        color = hsvColor,
    )
}