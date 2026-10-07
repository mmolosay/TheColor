package io.github.mmolosay.thecolor.presentation.input.group

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.presentation.input.ColorInputMapper
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexState
import io.github.mmolosay.thecolor.presentation.input.hex.withColor
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvState
import io.github.mmolosay.thecolor.presentation.input.hsv.withColor
import io.github.mmolosay.thecolor.presentation.input.model.ColorState
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbState
import io.github.mmolosay.thecolor.presentation.input.rgb.withColor
import io.github.mmolosay.thecolor.utils.Lens
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

data class ColorInputGroupState(
    val selectedInputType: DomainColorInputType,
    val orderedInputTypes: List<DomainColorInputType>,
    val colorState: ColorState,
    val hex: ColorInputHexState,
    val rgb: ColorInputRgbState,
    val hsv: ColorInputHsvState,
)

object ColorInputGroupStateLenses {
    val hex = Lens<ColorInputGroupState, ColorInputHexState>(
        get = { s -> s.hex },
        set = { s, v -> s.copy(hex = v) },
    )
    val rgb = Lens<ColorInputGroupState, ColorInputRgbState>(
        get = { s -> s.rgb },
        set = { s, v -> s.copy(rgb = v) },
    )
    val hsv = Lens<ColorInputGroupState, ColorInputHsvState>(
        get = { s -> s.hsv },
        set = { s, v -> s.copy(hsv = v) },
    )
}

fun ColorInputGroupState.toData(): ColorInputGroupData =
    ColorInputGroupData(
        selectedInputType = this.selectedInputType,
        orderedInputTypes = this.orderedInputTypes,
    )

context(
    converter: ColorConverter,
    inputMapper: ColorInputMapper,
)
fun ColorInputGroupState.withColor(
    color: Color?,
    source: DomainColorInputType?,
): ColorInputGroupState {
    return this.copy(
        colorState = ColorState(
            color = color,
            source = source,
            revision = this.colorState.revision.next(),
        ),
        hex = this.hex.withColor(color),
        rgb = this.rgb.withColor(color),
        hsv = this.hsv.withColor(color),
    )
}