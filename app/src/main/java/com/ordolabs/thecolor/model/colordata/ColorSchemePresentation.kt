package com.ordolabs.thecolor.model.colordata

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ColorSchemePresentation(
    val mode: SchemeMode?,
    val colors: List<ColorDetailsPresentation>?,
    val seed: ColorDetailsPresentation?
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