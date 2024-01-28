package io.github.mmolosay.thecolor.presentation.home.details

import android.content.Context
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.github.mmolosay.thecolor.presentation.home.R
import io.github.mmolosay.thecolor.presentation.home.details.ColorDetailsUiData.ContentColors
import io.github.mmolosay.thecolor.presentation.home.details.ColorDetailsUiData.ViewData
import androidx.compose.material.ripple.RippleTheme as BaseRippleTheme

/**
 * Framework-oriented data required for color details View to be presented by Compose.
 * It is a combination of [ColorDetailsData], [ViewData] and [ContentColors].
 */
data class ColorDetailsUiData(
    val background: Color,
    val headline: Headline,
    val translations: ColorTranslations,
    val divider: Divider,
    val specs: List<ColorSpec>,
    val viewColorSchemeButtonText: ViewColorSchemeButton,
) {

    data class Headline(
        val text: String,
        val color: Color,
    )

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
            val labelColor: Color,
            val value: String,
            val valueColor: Color,
        )

        data class Rgb(
            val label: String,
            val labelColor: Color,
            val r: String,
            val g: String,
            val b: String,
            val valueColor: Color,
        )

        data class Hsl(
            val label: String,
            val labelColor: Color,
            val h: String,
            val s: String,
            val l: String,
            val valueColor: Color,
        )

        data class Hsv(
            val label: String,
            val labelColor: Color,
            val h: String,
            val s: String,
            val v: String,
            val valueColor: Color,
        )

        data class Cmyk(
            val label: String,
            val labelColor: Color,
            val c: String,
            val m: String,
            val y: String,
            val k: String,
            val valueColor: Color,
        )
    }

    data class Divider(
        val color: Color,
    )

    sealed interface ColorSpec {

        data class Name(
            val label: String,
            val labelColor: Color,
            val value: String,
            val valueColor: Color,
        ) : ColorSpec

        data class ExactMatch(
            val label: String,
            val labelColor: Color,
            val value: String,
            val valueColor: Color,
        ) : ColorSpec

        data class ExactValue(
            val label: String,
            val labelColor: Color,
            val iconColor: Color,
            val value: String,
            val valueColor: Color,
            val exactColor: Color,
            val onClick: () -> Unit,
        ) : ColorSpec

        data class Deviation(
            val label: String,
            val labelColor: Color,
            val value: String,
            val valueColor: Color,
        ) : ColorSpec
    }

    data class ViewColorSchemeButton(
        val text: String,
        val contentColor: Color,
        val onClick: () -> Unit,
    )

    /**
     * Part of to-be [ColorDetailsUiData].
     * Framework-oriented.
     *
     * Created by View, since string resources are tied to platform-specific
     * components (like Context), which should be avoided in ViewModels.
     *
     * It is provided to View, meaning that you are able to pass different instances of [ViewData]
     * in testing purposes to verify that everything looks good (e.g. in Compose Preview).
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
        val exactValueLabel: String,
        val deviationLabel: String,
        val viewColorSchemeButtonText: String,
    )

    /**
     * Part of to-be [ColorDetailsUiData].
     * Framework-oriented.
     *
     * Created by View, but isn't part of [ViewData].
     * Unlike [ViewData], [ContentColors] is not intended to be providable.
     * It is created inside View and is not injectable.
     * The reason for this is because you don't usually want to test your View with different
     * [ContentColors]. They are constant, defined by design and private to View.
     */
    data class ContentColors(
        val rippleTheme: RippleTheme,
        val headline: Color,
        val translation: ColorTranslation,
        val divider: Color,
        val specs: ColorSpecs,
        val viewColorSchemeButtonContentColor: Color,
    ) {
        data class ColorTranslation(
            val label: Color,
            val value: Color,
        )

        data class ColorSpecs(
            val label: Color,
            val value: Color,
        )

        class RippleTheme(
            private val contentColor: Color,
            private val lightTheme: Boolean,
        ) : BaseRippleTheme {

            @Composable
            override fun defaultColor(): Color =
                BaseRippleTheme.defaultRippleColor(
                    contentColor = contentColor,
                    lightTheme = lightTheme,
                )

            @Composable
            override fun rippleAlpha(): RippleAlpha =
                BaseRippleTheme.defaultRippleAlpha(
                    contentColor = contentColor,
                    lightTheme = lightTheme,
                )
        }
    }
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
        exactValueLabel = context.getString(R.string.color_details_exact_value_label),
        deviationLabel = context.getString(R.string.color_details_deviation_label),
        viewColorSchemeButtonText = context.getString(R.string.color_details_view_color_scheme_button_text),
    )

fun ColorDetailsContentColors(useLight: Boolean): ContentColors =
    if (useLight) LightContentColors() else DarkContentColors()

private fun LightContentColors() =
    ContentColors(
        rippleTheme = ContentColors.RippleTheme(
            contentColor = Color(0xDE_FFFFFF),
            lightTheme = true,
        ),
        headline = Color(0xDE_FFFFFF),
        translation = ContentColors.ColorTranslation(
            label = Color(0x99_FFFFFF),
            value = Color(0xFF_FFFFFF),
        ),
        divider = Color(0x61_FFFFFF),
        specs = ContentColors.ColorSpecs(
            label = Color(0x99_FFFFFF),
            value = Color(0xDE_FFFFFF),
        ),
        viewColorSchemeButtonContentColor = Color.White,
    )

private fun DarkContentColors() =
    ContentColors(
        rippleTheme = ContentColors.RippleTheme(
            contentColor = Color(0xDE_000000),
            lightTheme = true,
        ),
        headline = Color(0xDE_000000),
        translation = ContentColors.ColorTranslation(
            label = Color(0x99_000000),
            value = Color(0xFF_1C1B1F),
        ),
        divider = Color(0x61_000000),
        specs = ContentColors.ColorSpecs(
            label = Color(0x99_000000),
            value = Color(0xDE_000000),
        ),
        viewColorSchemeButtonContentColor = Color.Black,
    )