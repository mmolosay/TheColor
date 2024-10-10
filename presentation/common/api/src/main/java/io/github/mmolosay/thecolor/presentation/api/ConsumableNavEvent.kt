package io.github.mmolosay.thecolor.presentation.api

/**
 * An interface for a navigation event that is executed by UI and should be reported
 * as consumed (executed, reduced) via [onConsumed].
 */
interface ConsumableNavEvent {
    val onConsumed: () -> Unit
}