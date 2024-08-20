package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.presentation.impl.ConsumableNavEvent

/**
 * A family of navigation events that may occur in home View.
 */
sealed interface HomeNavEvent : ConsumableNavEvent {

    data class GoToSettings(
        override val onConsumed: () -> Unit,
    ) : HomeNavEvent
}