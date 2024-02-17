package io.github.mmolosay.thecolor.presentation.center

/**
 * Platform-agnostic data provided by ViewModel to color center View.
 *
 * @param changePage an action to be invoked by View to change the current page to some different page.
 * @param onPageChanged an action to be invoked by View when page was changed by user gesture
 * and now this new page index should be propagated to ViewModel to ensure data synchronisation.
 */
data class ColorCenterData(
    val page: Int,
    val changePage: ChangePageAction,
    val onPageChanged: OnPageChangedAction,
) {

    fun interface ChangePageAction : (Int) -> Unit {
        override operator fun invoke(destPage: Int)
    }

    fun interface OnPageChangedAction : (Int) -> Unit {
        override fun invoke(newPage: Int)
    }
}