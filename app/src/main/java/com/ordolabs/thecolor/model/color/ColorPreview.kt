package com.ordolabs.thecolor.model.color

import android.os.Parcelable
import com.ordolabs.thecolor.model.colorinput.ColorInputHex
import com.ordolabs.thecolor.model.colorinput.ColorInputRgb
import kotlinx.parcelize.Parcelize

/**
 * __Valid__ color of immaterial color space, whether obtained from user input or not.
 */
// TODO: not val color, but inherit ColorPresentation
@Parcelize
data class ColorPreview(
    val color: ColorPresentation,
    val isUserInput: Boolean
) : Parcelable

fun ColorPreview.toColorInputHexPresentation() =
    ColorInputHex(
        color = this.color.toColorHexPresentation(),
        isUserInput = this.isUserInput
    )

fun ColorPreview.toColorInputRgbPresentation() =
    ColorInputRgb(
        color = this.color.toColorRgbPresentation(),
        isUserInput = this.isUserInput
    )