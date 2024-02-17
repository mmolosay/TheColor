package io.github.mmolosay.thecolor.presentation.center

/**
 * Platform-agnostic data provided by ViewModel to color center View.
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