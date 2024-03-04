package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.presentation.ColorInt

/**
 * Platform-agnostic data provided by ViewModel to Home screen View.
 */
data class HomeData(
    val canProceed: CanProceed,
    val colorUsedToProceed: ColorFromColorInput?,
) {

    sealed interface CanProceed {
        data object No : CanProceed
        data class Yes(val action: () -> Unit) : CanProceed
    }

    data class ColorFromColorInput(
        val color: ColorInt,
        val isDark: Boolean,
    )
}