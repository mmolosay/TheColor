package com.ordolabs.thecolor.mapper

import com.ordolabs.domain.model.ColorHex
import com.ordolabs.domain.model.ColorRgb
import com.ordolabs.thecolor.model.ColorHexPresentation
import com.ordolabs.thecolor.model.ColorRgbPresentation

fun ColorHexPresentation.toDomain(): ColorHex? {
    this.value ?: return null
    val string = this.value.let {
        if (it.startsWith('#')) it.substring(1) else it
    }
    if (string.length != 3 && string.length != 6) return null
    return ColorHex(
        value = string
    )
}

fun ColorRgbPresentation.toDomain(): ColorRgb? {
    if (this.r == null || this.g == null || this.b == null) return null
    return ColorRgb(
        r = this.r,
        g = this.g,
        b = this.b
    )
}