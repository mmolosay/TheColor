package io.github.mmolosay.thecolor.presentation.details

import io.github.mmolosay.thecolor.presentation.api.ColorInt

/**
 * Platform-agnostic data provided by ViewModel to color details View.
 */
data class ColorDetailsData(
    val colorName: String,
    val hex: Hex,
    val rgb: Rgb,
    val hsl: Hsl,
    val hsv: Hsv,
    val cmyk: Cmyk,
    val exactMatch: ExactMatch,
    val initialColorData: InitialColorData?,
) {

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
            val goToExactColor: () -> Unit,
            val deviation: String,
        ) : ExactMatch
    }

    /**
     * When [ExactMatch.No.goToExactColor] is invoked and "exact" color is shown,
     * you can use [goToInitialColor] to restore Color Details state to initial color from
     * where the "exact" color was clicked.
     */
    data class InitialColorData(
        val initialColor: ColorInt,
        val goToInitialColor: () -> Unit,
    )
}