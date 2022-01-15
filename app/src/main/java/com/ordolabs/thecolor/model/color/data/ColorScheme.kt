package com.ordolabs.thecolor.model.color.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ColorScheme(
    val mode: Mode?,
    val colors: List<ColorDetails>?,
    val seed: ColorDetails?
) : Parcelable {

    enum class Mode {
        MONOCHROME,
        MONOCHROME_DARK,
        MONOCHROME_LIGHT,
        ANALOGIC {
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
        COMPLEMENT {
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
        ANALOGIC_COMPLEMENT {
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
        TRIAD {
            override fun getPartitions(): List<Partition> =
                listOf(
                    3 into 3,
                    6 into 3,
                    9 into 3,
                    12 into 3,
                    15 into 3
                )
        },
        QUAD {
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
    }
}