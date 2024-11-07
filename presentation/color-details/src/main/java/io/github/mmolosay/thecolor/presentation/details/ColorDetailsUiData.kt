package io.github.mmolosay.thecolor.presentation.details

import androidx.compose.ui.graphics.Color

/**
 * Framework-oriented data required for color details View to be presented by Compose.
 * It is a combination of [ColorDetailsData] and [ColorDetailsUiStrings].
 */
data class ColorDetailsUiData(
    val headline: String,
    val translations: ColorTranslations,
    val specs: List<ColorSpec>,
) {

    data class ColorTranslations(
        val hex: ColorTranslation.Hex,
        val rgb: ColorTranslation.Rgb,
        val hsl: ColorTranslation.Hsl,
        val hsv: ColorTranslation.Hsv,
        val cmyk: ColorTranslation.Cmyk,
    )

    object ColorTranslation {

        data class Hex(
            val label: String,
            val value: String,
        )

        data class Rgb(
            val label: String,
            val r: String,
            val g: String,
            val b: String,
        )

        data class Hsl(
            val label: String,
            val h: String,
            val s: String,
            val l: String,
        )

        data class Hsv(
            val label: String,
            val h: String,
            val s: String,
            val v: String,
        )

        data class Cmyk(
            val label: String,
            val c: String,
            val m: String,
            val y: String,
            val k: String,
        )
    }

    sealed interface ColorSpec {

        data class Name(
            val label: String,
            val value: String,
        ) : ColorSpec

        data class ExactMatch(
            val label: String,
            val value: String,
            val goBackToInitialColorButton: GoBackToInitialColorButton?,
        ) : ColorSpec {

            data class GoBackToInitialColorButton(
                val text: String,
                val initialColor: Color,
                val onClick: () -> Unit,
            )
        }

        data class ExactValue(
            val label: String,
            val value: String,
            val exactColor: Color,
            val onClick: () -> Unit,
        ) : ColorSpec

        data class Deviation(
            val label: String,
            val value: String,
        ) : ColorSpec
    }
}