package com.ordolabs.thecolor.model.color

import kotlinx.parcelize.Parcelize

/**
 * Presentation of RGB color. May contain non-valid color or `null`s.
 *
 * @param r R component in range (0-255) including both start and end.
 * @param g G component in range (0-255) including both start and end.
 * @param b B component in range (0-255) including both start and end.
 */
@Parcelize
data class ColorRgb(
    val r: Int?,
    val g: Int?,
    val b: Int?
) : IAbstractColor