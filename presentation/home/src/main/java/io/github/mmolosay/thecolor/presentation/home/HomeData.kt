package io.github.mmolosay.thecolor.presentation.home

/**
 * Platform-agnostic data provided by ViewModel to Home screen View.
 */
data class HomeData(
    val canProceed: CanProceed,
) {

    sealed interface CanProceed {
        data object No : CanProceed
        data class Yes(val action: () -> Unit) : CanProceed
    }
}