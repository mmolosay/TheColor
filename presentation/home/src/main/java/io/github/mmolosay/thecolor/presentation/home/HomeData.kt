package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.presentation.api.ColorInt

/**
 * Platform-agnostic data provided by ViewModel to Home screen View.
 */
data class HomeData(
    val canProceed: CanProceed,
    val proceedResult: ProceedResult?,
    val goToSettings: () -> Unit,
) {

    sealed interface CanProceed {
        data object No : CanProceed
        data class Yes(val action: () -> Unit) : CanProceed
    }

    /** Result of executing a 'proceed' action. */
    sealed interface ProceedResult {

        data object Failure : ProceedResult

        data class Success(
            val colorData: ColorData,
        ) : ProceedResult {

            /** A data of color that was used to proceed */
            data class ColorData(
                val color: ColorInt,
                val isDark: Boolean,
            )
        }
    }
}