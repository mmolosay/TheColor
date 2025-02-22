package io.github.mmolosay.thecolor.presentation.home.ui

import android.content.Context
import io.github.mmolosay.thecolor.presentation.home.R

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
    val randomizeButtonIconContentDesc: String,
    val invalidSubmittedColorMessage: String,
)

fun HomeUiStrings(context: Context) =
    HomeUiStrings(
        settingsIconContentDesc = context.getString(R.string.home_settings_icon_content_desc),
        headline = context.getString(R.string.home_headline),
        proceedButtonText = context.getString(R.string.home_proceed_btn),
        randomizeButtonIconContentDesc = context.getString(R.string.home_randomize_color_button_icon_content_desc),
        invalidSubmittedColorMessage = context.getString(R.string.home_invalid_color_message),
    )