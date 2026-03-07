package io.github.mmolosay.thecolor.domain.color

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