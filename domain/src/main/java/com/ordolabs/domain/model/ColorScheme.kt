package com.ordolabs.domain.model

data class ColorScheme(
    val modeOrdinal: Int?,
    val sampleCount: Int?,
    val colors: List<ColorDetails>?,
    val seed: ColorDetails?,
)