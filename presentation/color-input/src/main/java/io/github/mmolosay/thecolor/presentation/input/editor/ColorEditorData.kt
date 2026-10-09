package io.github.mmolosay.thecolor.presentation.input.editor

import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexData
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvData
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbData

data class ColorEditorData(
    val hex: ColorInputHexData,
    val rgb: ColorInputRgbData,
    val hsv: ColorInputHsvData,
)
