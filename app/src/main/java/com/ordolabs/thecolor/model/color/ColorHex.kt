package com.ordolabs.thecolor.model.color

import kotlinx.parcelize.Parcelize

/**
 * Presentation of HEX color. May contain non-valid color or `null`.
 *
 * @param value __signless__ HEX color `String`, e.g. "16A8C0".
 */
// TODO: rename ColorHex and other Color* classes into just * (Hex), introduce class ColorPrototype
//  and make them a part of it (ColorPrototype.Hex)
@Parcelize
data class ColorHex(
    val value: String?
) : AbstractColor() {

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (value?.hashCode() ?: 0)
        return result
    }

    companion object
}

fun ColorHex.Companion.empty() =
    ColorHex(null)