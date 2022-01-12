package com.ordolabs.thecolor.model.color

import kotlinx.parcelize.Parcelize

/**
 * Presentation of HEX color. May contain non-valid color or `null`.
 *
 * @param value __signless__ HEX color `String`, e.g. "16A8C0".
 */
@Parcelize
data class ColorHex(
    val value: String?
) : IAbstractColor