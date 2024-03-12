package io.github.mmolosay.thecolor.presentation.center

import android.content.Context
import io.github.mmolosay.thecolor.presentation.center.ColorCenterUiData.ViewData

/**
 * Framework-oriented data required for color center View to be presented by Compose.
 * It is a combination of [ColorCenterData] and [ViewData].
 */
data class ColorCenterUiData(
    val detailsPage: Page,
    val schemePage: Page,
    val changePageEvent: ChangePageEvent?,
) {

    data class Page(
        // the main content of each page is provided as a @Composable lambda
        val changePageButton: ChangePageButton,
    )

    data class ChangePageButton(
        val text: String,
        val onClick: () -> Unit,
    )

    /*
     * Same as ColorCenterData.ChangePageEvent. It's a coincidence and a case of false duplication.
     * Those two models speak in different languages, even though it has happened that the fields are the same.
     *
     * Imagine: if HorizontalPager() Composable was using String type for tracking pages (instead of Int),
     * and ViewModel would've still be using Int for this purpose, then reusing the same model (with either String or Int)
     * would have caused additional conversion logic (String <-> Int) in the wrong place.
     */
    data class ChangePageEvent(
        val destPage: Int,
        val onConsumed: () -> Unit,
    )

    /**
     * Part of to-be [ColorCenterUiData].
     * Framework-oriented.
     *
     * Created by View, since string resources are tied to platform-specific
     * components (like Context), which should be avoided in ViewModels.
     */
    data class ViewData(
        val detailsPageChangePageButtonText: String,
        val schemePageChangePageButtonText: String,
    )
}

fun ColorCenterViewData(context: Context) =
    ViewData(
        detailsPageChangePageButtonText = context.getString(R.string.color_center_details_page_change_button_text),
        schemePageChangePageButtonText = context.getString(R.string.color_center_scheme_page_change_button_text),
    )