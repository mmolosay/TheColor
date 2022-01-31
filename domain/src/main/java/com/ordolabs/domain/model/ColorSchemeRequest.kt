package com.ordolabs.domain.model

data class ColorSchemeRequest(
    val seedHex: String,
    val modeOrdinal: Int,
    val sampleCount: Int
)