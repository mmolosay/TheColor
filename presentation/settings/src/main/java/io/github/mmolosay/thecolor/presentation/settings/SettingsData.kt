package io.github.mmolosay.thecolor.presentation.settings

import io.github.mmolosay.thecolor.utils.ConsumableNavEvent

/**
 * Platform-agnostic data provided by ViewModel to settings View.
 */
data class SettingsData(
    val goToHome: () -> Unit,
    val navEvent: NavEvent?,
) {

    sealed interface NavEvent : ConsumableNavEvent {

        data class GoToHome(
            override val onConsumed: () -> Unit,
        ) : NavEvent
    }
}