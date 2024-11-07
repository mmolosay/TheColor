package io.github.mmolosay.thecolor.presentation.center

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * // TODO: change KDoc like this for all 'UiData' models across the app
 * Framework-oriented data required for Color Center UI to be presented by Compose.
 * It is a combination of [ColorCenterData] and [ColorCenterUiStrings].
 */
data class ColorCenterUiData(
    val detailsPage: Page,
    val schemePage: Page,
    val changePageEvent: ChangePageEvent?,
) {

    data class Page(
        // the main content of each page is provided as a @Composable lambda
        val changePageButton: ChangePageButton,
    ) {

        data class ChangePageButton(
            val text: String,
            val onClick: () -> Unit,
            val icon: ImageVector,
            val iconPlacement: IconPlacement,
        ) {
            enum class IconPlacement {
                Leading, Trailing,
            }
        }
    }

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
}