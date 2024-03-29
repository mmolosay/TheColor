package io.github.mmolosay.thecolor.presentation.navigation

/**
 * Encapsulates a navigation event.
 */
sealed interface NavEvent {
    data object Back : NavEvent
    data class Destination(val destination: NavDest) : NavEvent
}