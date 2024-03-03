package io.github.mmolosay.thecolor.presentation.home

import android.content.Context

/**
 * Framework-oriented data required for Home screen to be presented by Compose.
 * It's a derivative from [HomeData].
 */
data class HomeUiData(
    val headline: String,
    val proceedButton: ProceedButton,
) {

    data class ProceedButton(
        val onClick: () -> Unit,
        val enabled: Boolean,
        val text: String,
    )

    /**
     * Part of to-be [HomeUiData].
     * Framework-oriented.
     * Created by View, since string resources are tied to platform-specific
     * components (like Context), which should be avoided in ViewModels.
     */
    data class ViewData(
        val headline: String,
        val proceedButtonText: String,
    )
}

fun HomeViewData(context: Context) =
    HomeUiData.ViewData(
        headline = context.getString(R.string.home_headline),
        proceedButtonText = context.getString(R.string.home_proceed_btn),
    )