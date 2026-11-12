package io.github.mmolosay.thecolor.presentation.input.editor

import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvData
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbData

data class ColorEditorData(
    val rgb: ColorInputRgbData,
    val hsv: ColorInputHsvData,
)
