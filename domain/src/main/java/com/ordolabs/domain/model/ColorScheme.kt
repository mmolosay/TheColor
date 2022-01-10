package com.ordolabs.domain.model

data class ColorScheme(
    val mode: SchemeMode?,
    val sampleCount: Int?,
    val colors: List<ColorDetails>?,
    val seed: ColorDetails?
) {

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