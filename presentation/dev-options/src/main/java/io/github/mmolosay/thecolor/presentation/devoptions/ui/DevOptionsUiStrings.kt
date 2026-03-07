package io.github.mmolosay.thecolor.presentation.devoptions.ui

import android.content.Context
import android.text.Spanned
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
    val topBarRestartAppIconDesc: String,
    val topBarResetValuesToDefaultIconDesc: String,

    val restartAppDialogTitle: String,
    val restartAppDialogText: String,
    val restartAppDialogDismissButtonText: String,
    val restartAppDialogConfirmButtonText: String,

    val resetValuesToDefaultDialogTitle: String,
    val resetValuesToDefaultDialogText: String,
    val resetValuesToDefaultDialogDismissButtonText: String,
    val resetValuesToDefaultDialogConfirmButtonText: String,

    val itemPredictableRandomColorsTitle: String,
    val itemPredictableRandomColorsDesc: String,
    val itemPredictableRandomColorsValueRandom: String,
    val itemPredictableRandomColorsValueCyclingRgbShort: String,
    val itemPredictableRandomColorsValueCyclingRgbVerbose: String,
    val itemPredictableRandomColorsValueCyclingLightDarkShort: String,
    val itemPredictableRandomColorsValueCyclingLightDarkVerbose: String,

    val itemStrictModeTitle: String,
    val itemStrictModeDesc: Spanned,

    val itemHttpLoggingTitle: String,
    val itemHttpLoggingDesc: String,

    val itemBuildInfoTitle: String,
    val itemBuildInfoAppBuildTypeLabel: String,
    val itemBuildInfoAppVersionNameLabel: String,
    val itemBuildInfoAppVersionCodeLabel: String,
)

fun DevOptionsUiStrings(context: Context) =
    DevOptionsUiStrings(
        topBarTitle = context.getString(R.string.dev_options_top_bar_title),
        topBarGoBackIconDesc = context.getString(R.string.dev_options_top_bar_go_back_icon_desc),
        topBarRestartAppIconDesc = context.getString(R.string.dev_options_top_bar_restart_app_icon_desc),
        topBarResetValuesToDefaultIconDesc = context.getString(R.string.dev_options_top_bar_reset_values_to_default_icon_desc),

        restartAppDialogTitle = context.getString(R.string.dev_options_restart_app_dialog_title),
        restartAppDialogText = context.getString(R.string.dev_options_restart_app_dialog_text),
        restartAppDialogDismissButtonText = context.getString(R.string.dev_options_restart_app_dialog_dismiss_button_text),
        restartAppDialogConfirmButtonText = context.getString(R.string.dev_options_restart_app_dialog_confirm_button_text),

        resetValuesToDefaultDialogTitle = context.getString(R.string.dev_options_reset_values_to_default_dialog_title),
        resetValuesToDefaultDialogText = context.getString(R.string.dev_options_reset_values_to_default_dialog_text),
        resetValuesToDefaultDialogDismissButtonText = context.getString(R.string.dev_options_reset_values_to_default_dialog_dismiss_button_text),
        resetValuesToDefaultDialogConfirmButtonText = context.getString(R.string.dev_options_reset_values_to_default_dialog_confirm_button_text),

        itemPredictableRandomColorsTitle = context.getString(R.string.dev_options_item_predictable_random_colors_title),
        itemPredictableRandomColorsDesc = context.getString(R.string.dev_options_item_predictable_random_colors_desc),
        itemPredictableRandomColorsValueRandom = context.getString(R.string.dev_options_item_predictable_random_colors_value_random),
        itemPredictableRandomColorsValueCyclingRgbShort = context.getString(R.string.dev_options_item_predictable_random_colors_value_cycling_rgb_short),
        itemPredictableRandomColorsValueCyclingRgbVerbose = context.getString(R.string.dev_options_item_predictable_random_colors_value_cycling_rgb_verbose),
        itemPredictableRandomColorsValueCyclingLightDarkShort = context.getString(R.string.dev_options_item_predictable_random_colors_value_cycling_light_dark_short),
        itemPredictableRandomColorsValueCyclingLightDarkVerbose = context.getString(R.string.dev_options_item_predictable_random_colors_value_cycling_light_dark_verbose),

        itemStrictModeTitle = context.getString(R.string.dev_options_item_strict_mode_title),
        itemStrictModeDesc = context.getText(R.string.dev_options_item_strict_mode_desc) as Spanned,

        itemHttpLoggingTitle = context.getString(R.string.dev_options_item_http_logging_title),
        itemHttpLoggingDesc = context.getString(R.string.dev_options_item_http_logging_desc),

        itemBuildInfoTitle = context.getString(R.string.dev_options_item_build_info_title),
        itemBuildInfoAppBuildTypeLabel = context.getString(R.string.dev_options_item_build_info_app_build_type_label),
        itemBuildInfoAppVersionNameLabel = context.getString(R.string.dev_options_item_build_info_app_version_name_label),
        itemBuildInfoAppVersionCodeLabel = context.getString(R.string.dev_options_item_build_info_app_version_code_label),
    )