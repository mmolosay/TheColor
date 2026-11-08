package io.github.mmolosay.thecolor.presentation.input.editor

import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexState
import io.github.mmolosay.thecolor.presentation.input.hex.toData
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvState
import io.github.mmolosay.thecolor.presentation.input.hsv.toData
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbState
import io.github.mmolosay.thecolor.presentation.input.rgb.toData
import io.github.mmolosay.thecolor.utils.Lens

data class ColorEditorState(
    val hex: ColorInputHexState,
    val rgb: ColorInputRgbState,
    val hsv: ColorInputHsvState,
    val colorState: ColorState,
)

object ColorEditorStateLenses {
    val hex = Lens<ColorEditorState, ColorInputHexState>(
        get = { s -> s.hex },
        set = { s, v -> s.copy(hex = v) },
    )
    val rgb = Lens<ColorEditorState, ColorInputRgbState>(
        get = { s -> s.rgb },
        set = { s, v -> s.copy(rgb = v) },
    )
    val hsv = Lens<ColorEditorState, ColorInputHsvState>(
        get = { s -> s.hsv },
        set = { s, v -> s.copy(hsv = v) },
    )
}

fun ColorEditorState.toData(): ColorEditorData =
    ColorEditorData(
        hex = this.hex.toData(),
        rgb = this.rgb.toData(),
        hsv = this.hsv.toData(),
    )