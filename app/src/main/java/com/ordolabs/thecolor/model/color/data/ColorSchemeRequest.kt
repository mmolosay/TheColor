package com.ordolabs.thecolor.model.color.data

import android.os.Parcelable
import com.ordolabs.thecolor.model.color.Color
import kotlinx.parcelize.Parcelize

data class ColorSchemeRequest(
    val seed: Color,
    val config: Config
) {

    @Parcelize
    data class Config(
        val modeOrdinal: Int,
        val sampleCount: Int
    ) : Parcelable {
        companion object {
            const val SAMPLE_COUNT_DEFAULT = 8
            val sampleCounts = listOf(
                5, 6, 8, 10, 12, 15
            )
        }
    }
}