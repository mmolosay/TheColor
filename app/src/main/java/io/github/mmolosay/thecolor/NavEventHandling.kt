package io.github.mmolosay.thecolor

import androidx.navigation.NavController
import io.github.mmolosay.thecolor.presentation.navigation.NavDest
import io.github.mmolosay.thecolor.presentation.navigation.NavEvent

/**
 * Performs navigation based on receiver [NavEvent] using passed controllers.
 */
infix fun NavEvent.handle(
    mainNavController: NavController,
) {
    when (this) {
        is NavEvent.Back -> mainNavController.popBackStack()
        is NavEvent.Destination -> mainNavController.navigate(route = destination.route)
    }
}