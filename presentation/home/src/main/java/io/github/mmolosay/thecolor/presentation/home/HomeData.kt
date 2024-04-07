package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.presentation.ColorInt
import io.github.mmolosay.thecolor.presentation.home.HomeData.Models
import io.github.mmolosay.thecolor.utils.ConsumableNavEvent

/**
 * Platform-agnostic data provided by ViewModel to Home screen View.
 * Assembled from [Models] and private methods of ViewModel.
 *
 * Such separation is required, because [Models] and actions (lambdas) originate from different places.
 * [Models] are created by mapping domain models to presentation counterparts.
 * Actions belong to ViewModel and should only be exposed to View as a part of [HomeData].
 */
data class HomeData(
    val canProceed: CanProceed,
    val colorUsedToProceed: ColorData?,
    val goToSettings: () -> Unit,
    val navEvent: NavEvent?,
) {

    sealed interface CanProceed {
        data object No : CanProceed
        data class Yes(val action: () -> Unit) : CanProceed
    }

    data class ColorData(
        val color: ColorInt,
        val isDark: Boolean,
    )

    sealed interface NavEvent : ConsumableNavEvent {
        data class GoToSettings(
            override val onConsumed: () -> Unit,
        ) : NavEvent
    }

    // TODO: move to HomeViewModel
    data class Models(
        val canProceed: Boolean,
        val colorUsedToProceed: ColorData?,
        val navEvent: NavEvent?,
    ) {
        sealed interface NavEvent {
            data object GoToSettings : NavEvent
        }
    }
}