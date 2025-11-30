package io.github.mmolosay.thecolor.presentation.input.impl

import io.github.mmolosay.thecolor.presentation.input.impl.model.ColorInput
import javax.inject.Inject

class ColorInputFactory @Inject constructor() {

    fun emptyHex(): ColorInput.Hex =
        ColorInput.Hex(string = "")

    fun emptyRgb(): ColorInput.Rgb =
        ColorInput.Rgb(r = "", g = "", b = "")
}