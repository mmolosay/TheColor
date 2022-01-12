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
) : AbstractColor() {

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (r ?: 0)
        result = 31 * result + (g ?: 0)
        result = 31 * result + (b ?: 0)
        return result
    }

    companion object
}

fun ColorRgb.Companion.empty() =
    ColorRgb(null, null, null)