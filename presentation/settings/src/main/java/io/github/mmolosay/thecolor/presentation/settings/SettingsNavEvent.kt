package io.github.mmolosay.thecolor.presentation.settings

import io.github.mmolosay.thecolor.presentation.api.ConsumableNavEvent

/**
 * A family of navigation events that may occur in settings View.
 */
sealed interface SettingsNavEvent :
    io.github.mmolosay.thecolor.presentation.api.ConsumableNavEvent {

    data class GoToHome(
        override val onConsumed: () -> Unit,
    ) : SettingsNavEvent
}