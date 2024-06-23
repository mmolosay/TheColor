package io.github.mmolosay.thecolor.presentation.details

import android.content.Context
import androidx.compose.ui.graphics.Color
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsUiData.ViewData

/**
 * Framework-oriented data required for color details View to be presented by Compose.
 * It is a combination of [ColorDetailsData] and [ViewData].
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

    /**
     * Part of to-be [ColorDetailsUiData].
     * Framework-oriented.
     *
     * Created by View, since string resources are tied to platform-specific
     * components (like Context), which should be avoided in ViewModels.
     */
    data class ViewData(
        val hexLabel: String,
        val rgbLabel: String,
        val hslLabel: String,
        val hsvLabel: String,
        val cmykLabel: String,
        val nameLabel: String,
        val exactMatchLabel: String,
        val exactMatchYes: String,
        val exactMatchNo: String,
        val goBackToInitialColorButtonText: String,
        val exactValueLabel: String,
        val deviationLabel: String,
        val viewColorSchemeButtonText: String,
    )
}

fun ColorDetailsViewData(context: Context) =
    ViewData(
        hexLabel = context.getString(R.string.color_details_hex_label),
        rgbLabel = context.getString(R.string.color_details_rgb_label),
        hslLabel = context.getString(R.string.color_details_hsl_label),
        hsvLabel = context.getString(R.string.color_details_hsv_label),
        cmykLabel = context.getString(R.string.color_details_cmyk_label),
        nameLabel = context.getString(R.string.color_details_name_label),
        exactMatchLabel = context.getString(R.string.color_details_exact_match_label),
        exactMatchYes = context.getString(R.string.color_details_exact_match_yes),
        exactMatchNo = context.getString(R.string.color_details_exact_match_no),
        goBackToInitialColorButtonText = context.getString(R.string.color_details_go_back_to_initial_color_button_text),
        exactValueLabel = context.getString(R.string.color_details_exact_value_label),
        deviationLabel = context.getString(R.string.color_details_deviation_label),
        viewColorSchemeButtonText = context.getString(R.string.color_details_view_color_scheme_button_text),
    )