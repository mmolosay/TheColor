package io.github.mmolosay.thecolor.presentation.settings

import io.github.mmolosay.thecolor.presentation.impl.ConsumableNavEvent

/**
 * A family of navigation events that may occur in settings View.
 */
sealed interface SettingsNavEvent : ConsumableNavEvent {

    data class GoToHome(
        override val onConsumed: () -> Unit,
    ) : SettingsNavEvent
}