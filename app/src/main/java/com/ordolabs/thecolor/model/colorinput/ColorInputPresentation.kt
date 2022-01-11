package com.ordolabs.thecolor.model.colorinput

import android.os.Parcelable
import com.ordolabs.thecolor.model.color.ColorHexPresentation
import com.ordolabs.thecolor.model.color.ColorRgbPresentation
import com.ordolabs.thecolor.model.color.IColorSpacePresentation
import kotlinx.parcelize.Parcelize

/**
 * Presentation for [color] of type [C] <-- [IColorSpacePresentation],
 * whether obtained from user input or not.
 */
// TODO: never used as type or something; replace typealias-es with classes
@Parcelize
data class ColorInputPresentation<C : IColorSpacePresentation>(
    val color: C,
    val isUserInput: Boolean
) : Parcelable

typealias ColorInputHexPresentation = ColorInputPresentation<ColorHexPresentation>
typealias ColorInputRgbPresentation = ColorInputPresentation<ColorRgbPresentation>