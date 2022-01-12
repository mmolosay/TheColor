package com.ordolabs.thecolor.model.colorinput

import android.os.Parcelable
import com.ordolabs.thecolor.model.color.ColorHex
import com.ordolabs.thecolor.model.color.ColorRgb
import com.ordolabs.thecolor.model.color.IAbstractColor
import kotlinx.parcelize.Parcelize

/**
 * Presentation for [color] of type [IAbstractColor],
 * whether obtained from user input or not.
 */
// TODO: never used as type or something; replace typealias-es with classes
@Parcelize
data class ColorInput<C : IAbstractColor>(
    val color: C,
    val isUserInput: Boolean // TODO: should not be passed with color; fragments should be responsible for it themselves
) : Parcelable

typealias ColorInputHex = ColorInput<ColorHex>
typealias ColorInputRgb = ColorInput<ColorRgb>