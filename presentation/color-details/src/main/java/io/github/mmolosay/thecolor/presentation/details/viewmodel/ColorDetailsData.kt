package io.github.mmolosay.thecolor.presentation.details.viewmodel

import io.github.mmolosay.thecolor.presentation.common.colorint.ColorInt

/**
 * Platform-agnostic data provided by ViewModel to 'Color Details' View.
 */
data class ColorDetailsData(
    val colorName: String,
    val hex: Hex,
    val rgb: Rgb,
    val hsl: Hsl,
    val hsv: Hsv,
    val cmyk: Cmyk,
    val exactMatch: ExactMatch,
    val colorRoleData: ColorRoleData,
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
            val deviation: String,
        ) : ExactMatch
    }

    sealed interface ColorRoleData {

        data class Seed(
            val exactColor: ColorInt,
        ) : ColorRoleData

        data class Exact(
            val seedColor: ColorInt,
        ) : ColorRoleData
    }
}