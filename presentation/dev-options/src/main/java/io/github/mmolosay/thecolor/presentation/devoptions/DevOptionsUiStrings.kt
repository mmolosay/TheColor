package io.github.mmolosay.thecolor.presentation.devoptions

import android.content.Context

/**
 * Strings that are pre-defined in UI and don't come from ViewModel.
 *
 * This object is created by View, since string resources are tied to platform-specific
 * components (like Context), which should be avoided in ViewModels.
 */
data class DevOptionsUiStrings(
    val topBarTitle: String,
    val topBarGoBackIconDesc: String,
)

fun DevOptionsUiStrings(context: Context) =
    DevOptionsUiStrings(
        topBarTitle = context.getString(R.string.dev_options_top_bar_title),
        topBarGoBackIconDesc = context.getString(R.string.dev_options_top_bar_go_back_icon_desc),
    )