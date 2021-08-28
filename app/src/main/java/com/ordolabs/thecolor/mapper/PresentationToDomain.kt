package com.ordolabs.thecolor.mapper

import com.ordolabs.domain.model.ColorHex
import com.ordolabs.domain.model.ColorRgb
import com.ordolabs.thecolor.model.ColorHexPresentation
import com.ordolabs.thecolor.model.ColorRgbPresentation

fun ColorHexPresentation.toDomain(): ColorHex {
    val valueString = this.value.let {
        if (it.startsWith('#')) it.substring(1) else it
    }
    return ColorHex(
        value = valueString
    )
}

fun ColorRgbPresentation.toDomain(): ColorRgb {
    return ColorRgb(
        r = this.r,
        g = this.g,
        b = this.b
    )
}