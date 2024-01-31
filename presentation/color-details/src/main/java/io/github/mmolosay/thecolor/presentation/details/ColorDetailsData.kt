package io.github.mmolosay.thecolor.presentation.details

/**
 * Platform-agnostic data provided by ViewModel to color details View.
 */
data class ColorDetailsData(
    val color: ColorInt,
    val colorName: String,
    val useLightContentColors: Boolean,
    val hex: Hex,
    val rgb: Rgb,
    val hsl: Hsl,
    val hsv: Hsv,
    val cmyk: Cmyk,
    val exactMatch: ExactMatch,
    val onViewColorSchemeClick: () -> Unit,
) {

    @JvmInline
    value class ColorInt(val hex: Int)

    data class Hex(
        val value: String,
    )

    data class Rgb(
        val r: String,
        val g: String,
        val b: String,
    )

    data class Hsl(
        val h: String,
        val s: String,
        val l: String,
    )

    data class Hsv(
        val h: String,
        val s: String,
        val v: String,
    )

    data class Cmyk(
        val c: String,
        val m: String,
        val y: String,
        val k: String,
    )

    sealed interface ExactMatch {
        data object Yes : ExactMatch
        data class No(
            val exactValue: String,
            val exactColor: ColorInt,
            val onExactClick: () -> Unit,
            val deviation: String,
        ) : ExactMatch
    }
}