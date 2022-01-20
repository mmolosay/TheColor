package com.ordolabs.thecolor.model.color

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Wraps valid [ColorPrototype] of type [C] and specifies,
 * whether it should be forced to be populated in `View` or not.
 */
@Parcelize
data class ColorInput<C : ColorPrototype>(
    val color: C,
    val forcePopulate: Boolean
) : Parcelable