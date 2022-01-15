package com.ordolabs.thecolor.model.color.data

import com.ordolabs.thecolor.model.color.Color

data class ColorSchemeInput(
    val seed: Color,
    val mode: ColorScheme.Mode,
    val partitionIndex: Int
) {

    companion object {
        const val SAMPLE_COUNT_MAX = 15
    }
}