package com.ordolabs.thecolor.mapper

import com.ordolabs.domain.model.ColorHex
import com.ordolabs.domain.model.ColorRgb
import com.ordolabs.thecolor.model.color.Color
import com.ordolabs.thecolor.model.color.ColorPrototype
import com.ordolabs.thecolor.model.color.from

fun ColorPrototype.Hex.toDomain(): ColorHex? {
    Color.from(this) ?: return null
    return ColorHex(
        value = this.value!! // checked in converting to valid color above
    )
}

fun ColorPrototype.Rgb.toDomain(): ColorRgb? {
    Color.from(this) ?: return null
    return ColorRgb(
        r = this.r!!, // checked in converting to valid color above
        g = this.g!!, // checked in converting to valid color above
        b = this.b!! // checked in converting to valid color above
    )
}