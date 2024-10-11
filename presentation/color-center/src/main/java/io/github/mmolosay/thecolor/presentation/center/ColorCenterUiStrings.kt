package io.github.mmolosay.thecolor.presentation.center

import android.content.Context

/**
 * Strings that are pre-defined in UI and don't come from ViewModel.
 *
 * This object is created by View, since string resources are tied to platform-specific
 * components (like Context), which should be avoided in ViewModels.
 */
data class ColorCenterUiStrings(
    val detailsPageChangePageButtonText: String,
    val schemePageChangePageButtonText: String,
)

fun ColorCenterUiStrings(context: Context) =
    ColorCenterUiStrings(
        detailsPageChangePageButtonText = context.getString(R.string.color_center_details_page_change_button_text),
        schemePageChangePageButtonText = context.getString(R.string.color_center_scheme_page_change_button_text),
    )