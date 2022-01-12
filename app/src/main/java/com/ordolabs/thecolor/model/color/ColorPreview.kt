package com.ordolabs.thecolor.model.color

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