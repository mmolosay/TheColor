package io.github.mmolosay.thecolor.presentation.acknowledgements

import android.content.Context

/**
 * Strings that are pre-defined in UI and don't come from ViewModel.
 *
 * This object is created by View, since string resources are tied to platform-specific
 * components (like Context), which should be avoided in ViewModels.
 */
data class AcknowledgementsUiStrings(
    val topBarTitle: String,
    val topBarGoBackIconDesc: String,
)

fun AcknowledgementsUiStrings(context: Context) =
    AcknowledgementsUiStrings(
        topBarTitle = context.getString(R.string.acknowledgements_top_bar_title),
        topBarGoBackIconDesc = context.getString(R.string.acknowledgements_top_bar_go_back_icon_desc),
    )