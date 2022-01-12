package com.ordolabs.thecolor.model.color

import com.ordolabs.thecolor.model.colorinput.ColorInputHex
import com.ordolabs.thecolor.model.colorinput.ColorInputRgb
import kotlinx.parcelize.Parcelize

/**
 * __Valid__ color of immaterial color space, whether obtained from user input or not.
 */
@Parcelize
class ColorPreview(
    override val hex: String,
    val isUserInput: Boolean
) : Color(hex) {

    constructor(color: Color, isUserInput: Boolean) : this(color.hex, isUserInput)
}

fun ColorPreview.toColorInputHexPresentation() =
    ColorInputHex(
        color = this.toColorHexPresentation(),
        isUserInput = this.isUserInput
    )

fun ColorPreview.toColorInputRgbPresentation() =
    ColorInputRgb(
        color = this.toColorRgbPresentation(),
        isUserInput = this.isUserInput
    )