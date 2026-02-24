package io.github.mmolosay.thecolor.presentation.settings.ui

import android.content.Context
import io.github.mmolosay.thecolor.presentation.settings.R

/**
 * Strings that are pre-defined in UI and don't come from ViewModel.
 *
 * This object is created by View, since string resources are tied to platform-specific
 * components (like Context), which should be avoided in ViewModels.
 */
data class SettingsUiStrings(
    val topBarTitle: String,
    val topBarGoBackIconDesc: String,
    val topBarResetPreferencesToDefaultIconDesc: String,

    val resetPreferencesToDefaultDialogTitle: String,
    val resetPreferencesToDefaultDialogText: String,
    val resetPreferencesToDefaultDialogDismissButtonText: String,
    val resetPreferencesToDefaultDialogConfirmButtonText: String,

    val itemPreferredColorInputTypeTitle: String,
    val itemPreferredColorInputTypeDesc: String,
    val itemPreferredColorInputTypeValueHex: String,
    val itemPreferredColorInputTypeValueRgb: String,
    val itemPreferredColorInputTypeValueHsv: String,

    val itemAppUiColorSchemeTitle: String,
    val itemAppUiColorSchemeDesc: String,
    val itemAppUiColorSchemeValueDayNightShort: String,
    val itemAppUiColorSchemeValueDayNightVerbose: String,
    val itemAppUiColorSchemeValueLight: String,
    val itemAppUiColorSchemeValueDark: String,
    val itemAppUiColorSchemeValueJungle: String,
    val itemAppUiColorSchemeValueMidnight: String,

    val itemDynamicUiColorsTitle: String,
    val itemDynamicUiColorsDesc: String,

    val itemResumeFromLastSearchedColorTitle: String,
    val itemResumeFromLastSearchedColorDesc: String,

    val itemSmartBackspaceTitle: String,
    val itemSmartBackspaceDesc: String,

    val itemSelectAllTextOnTextFieldFocusTitle: String,
    val itemSelectAllTextOnTextFieldFocusDesc: String,

    val itemAutoProceedWithRandomizedColorsTitle: String,
    val itemAutoProceedWithRandomizedColorsDesc: String,

    val itemDevOptionsTitle: String,
)

fun SettingsUiStrings(context: Context) =
    SettingsUiStrings(
        topBarTitle = context.getString(R.string.settings_top_bar_title),
        topBarGoBackIconDesc = context.getString(R.string.settings_top_bar_go_back_icon_desc),
        topBarResetPreferencesToDefaultIconDesc = context.getString(R.string.settings_top_bar_reset_preferences_to_default_icon_desc),

        resetPreferencesToDefaultDialogTitle = context.getString(R.string.settings_reset_preferences_to_default_dialog_title),
        resetPreferencesToDefaultDialogText = context.getString(R.string.settings_reset_preferences_to_default_dialog_text),
        resetPreferencesToDefaultDialogDismissButtonText = context.getString(R.string.settings_reset_preferences_to_default_dialog_dismiss_button_text),
        resetPreferencesToDefaultDialogConfirmButtonText = context.getString(R.string.settings_reset_preferences_to_default_dialog_confirm_button_text),

        itemPreferredColorInputTypeTitle = context.getString(R.string.settings_item_preferred_color_input_type_title),
        itemPreferredColorInputTypeDesc = context.getString(R.string.settings_item_preferred_color_input_type_desc),
        itemPreferredColorInputTypeValueHex = context.getString(R.string.settings_item_preferred_color_input_type_value_hex),
        itemPreferredColorInputTypeValueRgb = context.getString(R.string.settings_item_preferred_color_input_value_type_rgb),
        itemPreferredColorInputTypeValueHsv = context.getString(R.string.settings_item_preferred_color_input_value_type_hsv),

        itemAppUiColorSchemeTitle = context.getString(R.string.settings_item_app_ui_color_scheme_title),
        itemAppUiColorSchemeDesc = context.getString(R.string.settings_item_app_ui_color_scheme_desc),
        itemAppUiColorSchemeValueDayNightShort = context.getString(R.string.settings_item_app_ui_color_scheme_value_day_night_short),
        itemAppUiColorSchemeValueDayNightVerbose = context.getString(R.string.settings_item_app_ui_color_scheme_value_day_night_verbose),
        itemAppUiColorSchemeValueLight = context.getString(R.string.settings_item_app_ui_color_scheme_value_light),
        itemAppUiColorSchemeValueDark = context.getString(R.string.settings_item_app_ui_color_scheme_value_dark),
        itemAppUiColorSchemeValueJungle = context.getString(R.string.settings_item_app_ui_color_scheme_value_jungle),
        itemAppUiColorSchemeValueMidnight = context.getString(R.string.settings_item_app_ui_color_scheme_value_midnight),

        itemDynamicUiColorsTitle = context.getString(R.string.settings_item_dynamic_ui_colors_title),
        itemDynamicUiColorsDesc = context.getString(R.string.settings_item_dynamic_ui_colors_desc),

        itemResumeFromLastSearchedColorTitle = context.getString(R.string.settings_item_resume_from_last_searched_color_title),
        itemResumeFromLastSearchedColorDesc = context.getString(R.string.settings_item_resume_from_last_searched_color_desc),

        itemSmartBackspaceTitle = context.getString(R.string.settings_item_smart_backspace_title),
        itemSmartBackspaceDesc = context.getString(R.string.settings_item_smart_backspace_desc),

        itemSelectAllTextOnTextFieldFocusTitle = context.getString(R.string.settings_item_select_all_text_on_text_field_focus_title),
        itemSelectAllTextOnTextFieldFocusDesc = context.getString(R.string.settings_item_select_all_text_on_text_field_focus_desc),

        itemAutoProceedWithRandomizedColorsTitle = context.getString(R.string.settings_item_auto_proceed_with_randomized_colors_title),
        itemAutoProceedWithRandomizedColorsDesc = context.getString(R.string.settings_item_auto_proceed_with_randomized_colors_desc),

        itemDevOptionsTitle = context.getString(R.string.settings_item_dev_options_title),
    )