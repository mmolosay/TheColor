package io.github.mmolosay.thecolor.presentation.settings

import android.content.Context

/**
 * Framework-oriented.
 * Created by View, since string resources are tied to platform-specific
 * components (like Context), which should be avoided in ViewModels.
 */
// TODO: rename into "FeatureUiStrings" across the app
data class SettingsUiStrings(
    val topBarTitle: String,
    val topBarGoBackIconDesc: String,
    val itemPreferredColorInputTitle: String,
    val itemPreferredColorInputDesc: String,
    val itemPreferredColorInputValueHex: String,
    val itemPreferredColorInputValueRgb: String,
)

fun SettingsUiStrings(context: Context) =
    SettingsUiStrings(
        topBarTitle = context.getString(R.string.settings_top_bar_title),
        topBarGoBackIconDesc = context.getString(R.string.settings_top_bar_go_back_icon_desc),
        itemPreferredColorInputTitle = context.getString(R.string.settings_item_preferred_color_input_title),
        itemPreferredColorInputDesc = context.getString(R.string.settings_item_preferred_color_input_desc),
        itemPreferredColorInputValueHex = context.getString(R.string.settings_item_preferred_color_input_value_hex),
        itemPreferredColorInputValueRgb = context.getString(R.string.settings_item_preferred_color_input_value_rgb),
    )