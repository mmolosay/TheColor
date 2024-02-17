package io.github.mmolosay.thecolor.presentation.center

import android.content.Context
import io.github.mmolosay.thecolor.presentation.center.ColorCenterUiData.ViewData

/**
 * Framework-oriented data required for color center View to be presented by Compose.
 * It is a combination of [ColorCenterData] and [ViewData].
 */
data class ColorCenterUiData(
    val pageIndex: Int,
    val detailsPage: Page,
    val schemePage: Page,
) {

    data class Page(
        // the main content of each page is provided as a @Composable lambda
        val changePageButton: ChangePageButton,
    )

    data class ChangePageButton(
        val text: String,
        val onClick: () -> Unit,
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