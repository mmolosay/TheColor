package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.presentation.api.ColorInt

/**
 * Platform-agnostic data provided by ViewModel to Home screen View.
 */
data class HomeData(
    val canProceed: CanProceed,
    val colorUsedToProceed: ColorData?,
    val goToSettings: () -> Unit,
) {

    sealed interface CanProceed {
        data object No : CanProceed
        data class Yes(val action: () -> Unit) : CanProceed
    }

    data class ColorData(
        val color: ColorInt,
        val isDark: Boolean,
    )
}