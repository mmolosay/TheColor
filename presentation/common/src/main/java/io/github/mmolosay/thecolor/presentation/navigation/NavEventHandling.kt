package io.github.mmolosay.thecolor.presentation.navigation

import androidx.navigation.NavController

/**
 * Performs navigation based on passed [navEvent] using receiver [NavController].
 */
infix fun NavController.handle(navEvent: NavEvent) {
    when (navEvent) {
        is NavEvent.Back -> popBackStack()
        is NavEvent.Destination -> navigate(route = navEvent.destination.route)
    }
}