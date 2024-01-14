package io.github.mmolosay.presentation.model.color

import kotlinx.parcelize.Parcelize

/**
 * __Valid__ color, displayed on UI as a current one to be previewed.
 */
@Parcelize
class ColorPreview(
    override val hex: String,
    val isUserInput: Boolean
) : Color(hex) {

    constructor(color: Color, isUserInput: Boolean) : this(color.hex, isUserInput)
}