package io.github.mmolosay.thecolor.presentation.devoptions.ui

import android.content.Context
import io.github.mmolosay.thecolor.presentation.devoptions.R

/**
 * Strings that are pre-defined in UI and don't come from ViewModel.
 *
 * This object is created by View, since string resources are tied to platform-specific
 * components (like Context), which should be avoided in ViewModels.
 */
data class DevOptionsUiStrings(
    val topBarTitle: String,
    val topBarGoBackIconDesc: String,
    val itemPredictableRandomColorsTitle: String,
    val itemPredictableRandomColorsDesc: String,
    val itemPredictableRandomColorsValueRandom: String,
    val itemPredictableRandomColorsValueCyclingRgbShort: String,
    val itemPredictableRandomColorsValueCyclingRgbVerbose: String,
    val itemPredictableRandomColorsValueCyclingLightDarkShort: String,
    val itemPredictableRandomColorsValueCyclingLightDarkVerbose: String,
)

fun DevOptionsUiStrings(context: Context) =
    DevOptionsUiStrings(
        topBarTitle = context.getString(R.string.dev_options_top_bar_title),
        topBarGoBackIconDesc = context.getString(R.string.dev_options_top_bar_go_back_icon_desc),
        itemPredictableRandomColorsTitle = context.getString(R.string.dev_options_item_predictable_random_colors_title),
        itemPredictableRandomColorsDesc = context.getString(R.string.dev_options_item_predictable_random_colors_desc),
        itemPredictableRandomColorsValueRandom = context.getString(R.string.dev_options_item_predictable_random_colors_value_random),
        itemPredictableRandomColorsValueCyclingRgbShort = context.getString(R.string.dev_options_item_predictable_random_colors_value_cycling_rgb_short),
        itemPredictableRandomColorsValueCyclingRgbVerbose = context.getString(R.string.dev_options_item_predictable_random_colors_value_cycling_rgb_verbose),
        itemPredictableRandomColorsValueCyclingLightDarkShort = context.getString(R.string.dev_options_item_predictable_random_colors_value_cycling_light_dark_short),
        itemPredictableRandomColorsValueCyclingLightDarkVerbose = context.getString(R.string.dev_options_item_predictable_random_colors_value_cycling_light_dark_verbose),
    )