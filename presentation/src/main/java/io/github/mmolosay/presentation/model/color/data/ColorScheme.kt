package io.github.mmolosay.presentation.model.color.data

import android.os.Parcelable
import androidx.annotation.StringRes
import io.github.mmolosay.presentation.R
import io.github.mmolosay.presentation.model.color.Color
import kotlinx.parcelize.Parcelize

@Parcelize
data class ColorScheme(
    val mode: Mode?,
    val samples: List<Sample>?,
    val seed: ColorDetails?
) : Parcelable {

    @Parcelize
    data class Sample(
        val color: Color,
        val details: ColorDetails
    ) : Parcelable {

        constructor(hex: String, details: ColorDetails) : this(
            color = Color(hex),
            details = details
        )
    }

    enum class Mode(@StringRes val labelRes: Int) {
        MONOCHROME(R.string.color_scheme_mode_monochrome),
        MONOCHROME_DARK(R.string.color_scheme_mode_monochrome_dark),
        MONOCHROME_LIGHT(R.string.color_scheme_mode_monochrome_light),
        ANALOGIC(R.string.color_scheme_mode_analogic) {
            override fun getPartitions(): List<Partition> =
                listOf(
                    2 into 1,
                    3 into 1,
                    4 into 2,
                    6 into 3,
                    8 into 2,
                    9 into 3,
                    12 into 4
                )
        },
        COMPLEMENT(R.string.color_scheme_mode_complement) {
            override fun getPartitions(): List<Partition> =
                listOf(
                    2 into 2,
                    4 into 2,
                    6 into 2,
                    8 into 2,
                    10 into 2,
                    12 into 2
                )
        },
        ANALOGIC_COMPLEMENT(R.string.color_scheme_mode_analogic_complement) {
            override fun getPartitions(): List<Partition> =
                listOf(
                    2 into 1,
                    3 into 1,
                    4 into 2,
                    6 into 3,
                    8 into 2,
                    9 into 3,
                    12 into 4
                )
        },
        TRIAD(R.string.color_scheme_mode_triad) {
            override fun getPartitions(): List<Partition> =
                listOf(
                    3 into 3,
                    6 into 3,
                    9 into 3,
                    12 into 3,
                    15 into 3
                )
        },
        QUAD(R.string.color_scheme_mode_quad) {
            override fun getPartitions(): List<Partition> =
                listOf(
                    4 into 4,
                    8 into 4,
                    12 into 4,
                    16 into 4
                )
        };

        open fun getPartitions(): List<Partition> =
            List(10) { i ->
                (i + 2) into 1
            }

        data class Partition(
            val sampleCount: Int,
            val setCount: Int
        ) {
            val samplesPerSet = sampleCount / setCount
        }

        protected infix fun Int.into(setCount: Int): Partition {
            require(this % setCount == 0) { "can't split $this samples into $setCount groups" }
            return Partition(this, setCount)
        }

        companion object {
            val DEFAULT: Mode = MONOCHROME
        }
    }
}