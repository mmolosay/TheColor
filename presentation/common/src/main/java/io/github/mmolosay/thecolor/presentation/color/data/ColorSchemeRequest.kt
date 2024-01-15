package io.github.mmolosay.thecolor.presentation.color.data

import android.os.Parcelable
import io.github.mmolosay.thecolor.presentation.color.Color
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