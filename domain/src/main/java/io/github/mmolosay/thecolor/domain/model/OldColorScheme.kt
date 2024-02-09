package io.github.mmolosay.thecolor.domain.model

data class OldColorScheme(
    val modeOrdinal: Int,
    val sampleCount: Int,
    val colors: List<ColorDetails>,
    val seed: ColorDetails,
)