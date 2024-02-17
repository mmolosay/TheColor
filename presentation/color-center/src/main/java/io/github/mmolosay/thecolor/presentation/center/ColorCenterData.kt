package io.github.mmolosay.thecolor.presentation.center

/**
 * Platform-agnostic data provided by ViewModel to color center View.
 */
data class ColorCenterData(
    val pageIndex: Int,
    val changePage: ChangePageAction,
) {

    fun interface ChangePageAction {
        operator fun invoke(destPageIndex: Int)
    }
}