package com.ordolabs.thecolor.model.color.data

import com.ordolabs.thecolor.model.color.Color

data class ColorSchemeRequest(
    val seed: Color,
    val modeOrdinal: Int,
    val sampleCount: Int
) {

    companion object {
        const val SAMPLE_COUNT_MAX = 15
    }
}