package io.github.mmolosay.thecolor.presentation.home.viewmodel

import io.github.mmolosay.thecolor.presentation.api.ColorInt
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsViewModel

/**
 * Platform-agnostic data provided by ViewModel to 'Home' View.
 */
data class HomeData(
    val canProceed: CanProceed,
    val proceedResult: ProceedResult?,
    val colorSchemeSelectedSwatchData: ColorSchemeSelectedSwatchData?,
    val goToSettings: () -> Unit,
) {

    sealed interface CanProceed {
        data object No : CanProceed
        data class Yes(val proceed: () -> Unit) : CanProceed
    }

    /** Result of executing a 'proceed' action. */
    sealed interface ProceedResult {

        data class InvalidSubmittedColor(
            val discard: () -> Unit, // aka onConsumed()
        ) : ProceedResult

        data class Success(
            val colorData: ColorData,
        ) : ProceedResult {

            /** A data of color that was used to proceed. */
            data class ColorData(
                val color: ColorInt,
                val isDark: Boolean,
            )
        }
    }

    /** Data for currently selected swatch on 'Color Scheme'. */
    data class ColorSchemeSelectedSwatchData(
        val colorDetailsViewModel: ColorDetailsViewModel,
        val discard: () -> Unit, // aka onConsumed()
    )
}