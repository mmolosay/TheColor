package io.github.mmolosay.thecolor.presentation.details

import android.content.Context

/**
 * Strings that are pre-defined in UI and don't come from ViewModel.
 *
 * This object is created by View, since string resources are tied to platform-specific
 * components (like Context), which should be avoided in ViewModels.
 */
data class ColorDetailsUiStrings(
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

fun ColorDetailsUiStrings(context: Context) =
    ColorDetailsUiStrings(
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