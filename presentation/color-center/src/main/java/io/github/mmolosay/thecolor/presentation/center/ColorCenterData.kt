package io.github.mmolosay.thecolor.presentation.center

/**
 * Platform-agnostic data provided by ViewModel to color center View.
 *
 * @param changePage an action to be invoked by View to change the current page to some different page.
 */
data class ColorCenterData(
    val changePage: (destPage: Int) -> Unit,
    val changePageEvent: ChangePageEvent?,
) {

    // https://medium.com/androiddevelopers/viewmodel-one-off-event-antipatterns-16a1da869b95
    // https://developer.android.com/topic/architecture/ui-layer/events#consuming-trigger-updates
    data class ChangePageEvent(
        val destPage: Int,
        val onConsumed: () -> Unit,
    )
}