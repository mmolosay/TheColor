package io.github.mmolosay.thecolor.presentation.settings

import android.content.Context

/**
 * Strings that are pre-defined in UI and don't come from ViewModel.
 *
 * This object is created by View, since string resources are tied to platform-specific
 * components (like Context), which should be avoided in ViewModels.
 */
data class SettingsUiStrings(
    val topBarTitle: String,
    val topBarGoBackIconDesc: String,
    val itemPreferredColorInputTypeTitle: String,
    val itemPreferredColorInputTypeDesc: String,
    val itemPreferredColorInputTypeValueHex: String,
    val itemPreferredColorInputTypeValueRgb: String,
    val itemAppUiColorSchemeTitle: String,
    val itemAppUiColorSchemeDesc: String,
    val itemAppUiColorSchemeValueLight: String,
    val itemAppUiColorSchemeValueDark: String,
    val itemAppUiColorSchemeValueDayNightShort: String,
    val itemAppUiColorSchemeValueDayNightVerbose: String,
)

fun SettingsUiStrings(context: Context) =
    SettingsUiStrings(
        topBarTitle = context.getString(R.string.settings_top_bar_title),
        topBarGoBackIconDesc = context.getString(R.string.settings_top_bar_go_back_icon_desc),
        itemPreferredColorInputTypeTitle = context.getString(R.string.settings_item_preferred_color_input_type_title),
        itemPreferredColorInputTypeDesc = context.getString(R.string.settings_item_preferred_color_input_type_desc),
        itemPreferredColorInputTypeValueHex = context.getString(R.string.settings_item_preferred_color_input_type_value_hex),
        itemPreferredColorInputTypeValueRgb = context.getString(R.string.settings_item_preferred_color_input_value_type_rgb),
        itemAppUiColorSchemeTitle = context.getString(R.string.settings_item_app_ui_color_scheme_title),
        itemAppUiColorSchemeDesc = context.getString(R.string.settings_item_app_ui_color_scheme_desc),
        itemAppUiColorSchemeValueLight = context.getString(R.string.settings_item_app_ui_color_scheme_value_light),
        itemAppUiColorSchemeValueDark = context.getString(R.string.settings_item_app_ui_color_scheme_value_dark),
        itemAppUiColorSchemeValueDayNightShort = context.getString(R.string.settings_item_app_ui_color_scheme_value_day_night_short),
        itemAppUiColorSchemeValueDayNightVerbose = context.getString(R.string.settings_item_app_ui_color_scheme_value_day_night_verbose),
    )