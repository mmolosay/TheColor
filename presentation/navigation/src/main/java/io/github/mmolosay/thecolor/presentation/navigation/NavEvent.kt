package io.github.mmolosay.thecolor.presentation.navigation

/**
 * Encapsulates a navigation event.
 *
 * You can think of this component as a wider, more functional [NavDest].
 * Just as [NavDest], it specifies "where to go" / "what to show". It also may
 * carry useful navigation-related (but not data-related) payload.
 */
sealed interface NavEvent {
    data object Back : NavEvent
    data class Destination(val destination: NavDest) : NavEvent
}