package com.ordolabs.thecolor.model.colordata

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ColorScheme(
    val mode: SchemeMode?,
    val colors: List<ColorDetails>?,
    val seed: ColorDetails?
) : Parcelable {

    enum class SchemeMode {
        MONOCHROME,
        MONOCHROME_DARK,
        MONOCHROME_LIGHT,
        ANALOGIC,
        COMPLEMENT,
        ANALOGIC_COMPLEMENT,
        TRIAD,
        QUAD;

        override fun toString(): String {
            return this.name.lowercase().replace(oldChar = '_', newChar = ' ')
        }
    }
}