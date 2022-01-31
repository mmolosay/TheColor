package com.ordolabs.thecolor.model.color.data

import com.ordolabs.thecolor.model.color.Color

data class ColorSchemeRequest(
    val seed: Color,
    val config: Config
) {

    data class Config(
        val modeOrdinal: Int,
        val sampleCount: Int
    ) {
        companion object {
            const val SAMPLE_COUNT_DEFAULT = 8
            val sampleCounts = listOf(
                5, 6, 8, 10, 12, 15
            )
        }
    }
}