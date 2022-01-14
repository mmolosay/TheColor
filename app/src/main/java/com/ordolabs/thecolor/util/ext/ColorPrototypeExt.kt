package com.ordolabs.thecolor.util.ext

import com.ordolabs.thecolor.model.color.ColorPrototype

// TODO: may be defined in ColorPrototype?

fun ColorPrototype.Hex.Companion.empty() =
    ColorPrototype.Hex(null)

fun ColorPrototype.Rgb.Companion.empty() =
    ColorPrototype.Rgb(null, null, null)