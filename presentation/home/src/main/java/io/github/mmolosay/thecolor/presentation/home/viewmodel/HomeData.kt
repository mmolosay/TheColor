package io.github.mmolosay.thecolor.presentation.home.viewmodel

import io.github.mmolosay.thecolor.presentation.common.colorint.ColorInt
import kotlinx.collections.immutable.ImmutableList
import io.github.mmolosay.thecolor.utils.SideEffect as UtilsSideEffect

/**
 * Platform-agnostic data provided by ViewModel to 'Home' View.
 */
data class HomeData(
    val canProceed: Boolean,
    val proceedResult: ProceedResult?,
    val sideEffects: ImmutableList<SideEffect>,
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

    sealed interface SideEffect : UtilsSideEffect {

        data class GoToSettings(
            override val id: UtilsSideEffect.Id,
        ) : SideEffect
    }
}