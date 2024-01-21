package io.github.mmolosay.thecolor.input

import javax.inject.Inject

class ColorInputFactory @Inject constructor() {

    fun emptyHex(): ColorInput.Hex =
        ColorInput.Hex(string = "")

    fun emptyRgb(): ColorInput.Rgb =
        ColorInput.Rgb(r = "", g = "", b = "")
}