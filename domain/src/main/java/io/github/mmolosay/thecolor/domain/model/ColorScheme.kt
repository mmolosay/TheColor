package io.github.mmolosay.thecolor.domain.model

data class ColorScheme(
    val swatchDetails: List<ColorDetails>,
) {

    enum class Mode {
        Monochrome,
        MonochromeDark,
        MonochromeLight,
        Analogic,
        Complement,
        AnalogicComplement,
        Triad,
        Quad,
    }
}