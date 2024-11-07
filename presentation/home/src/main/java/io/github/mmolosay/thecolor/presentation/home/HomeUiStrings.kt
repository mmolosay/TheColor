package io.github.mmolosay.thecolor.presentation.home

import android.content.Context

/**
 * Strings that are pre-defined in UI and don't come from ViewModel.
 *
 * This object is created by View, since string resources are tied to platform-specific
 * components (like Context), which should be avoided in ViewModels.
 */
data class HomeUiStrings(
    val settingsIconContentDesc: String,
    val headline: String,
    val proceedButtonText: String,
    val invalidSubmittedColorMessage: String,
)

fun HomeUiStrings(context: Context) =
    HomeUiStrings(
        settingsIconContentDesc = context.getString(R.string.home_settings_icon_content_desc),
        headline = context.getString(R.string.home_headline),
        proceedButtonText = context.getString(R.string.home_proceed_btn),
        invalidSubmittedColorMessage = context.getString(R.string.home_invalid_color_message),
    )