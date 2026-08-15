package io.github.mmolosay.thecolor.presentation.home.viewmodel

import io.github.mmolosay.thecolor.presentation.common.colorint.ColorInt
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel
import io.github.mmolosay.thecolor.utils.SideEffect as UtilsSideEffect

/**
 * Platform-agnostic data provided by ViewModel to 'Home' View.
 */
data class HomeData(
    val canProceed: Boolean,
    val proceedResult: ProceedResult?,
    val colorSchemeSelectedSwatchData: ColorSchemeSelectedSwatchData?,
    val sideEffects: List<SideEffect>,
) {

    /** Result of executing a 'proceed' action. */
    sealed interface ProceedResult {

        data object InvalidSubmittedColor : ProceedResult

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
    )

    sealed interface SideEffect : UtilsSideEffect {

        data class GoToSettings(
            override val id: UtilsSideEffect.Id,
        ) : SideEffect
    }
}