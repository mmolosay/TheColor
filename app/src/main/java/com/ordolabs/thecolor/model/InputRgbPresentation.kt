package com.ordolabs.thecolor.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Presentation of RGB color, entered by user.
 * May contain non-valid color or nothing at all.
 *
 * @param r R component in range (0-255) including both start and end.
 * @param g G component in range (0-255) including both start and end.
 * @param b B component in range (0-255) including both start and end.
 */
@Parcelize
data class InputRgbPresentation(
    val r: Int?,
    val g: Int?,
    val b: Int?
) : Parcelable