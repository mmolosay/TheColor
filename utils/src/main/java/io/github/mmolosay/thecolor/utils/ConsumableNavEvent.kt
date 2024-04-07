package io.github.mmolosay.thecolor.utils

/**
 * An interface for a navigation event that is executed by UI and should be reported
 * as consumed (executed, reduced) via [onConsumed].
 */
// TODO: move to :presentation:common?
interface ConsumableNavEvent {
    val onConsumed: () -> Unit
}