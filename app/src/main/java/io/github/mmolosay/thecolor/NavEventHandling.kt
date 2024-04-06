package io.github.mmolosay.thecolor

import androidx.navigation.NavController
import io.github.mmolosay.thecolor.presentation.navigation.NavEvent

/**
 * Performs navigation based on receiver [NavEvent] using passed controllers.
 */
infix fun io.github.mmolosay.thecolor.presentation.navigation.NavEvent.handle(
    mainNavController: NavController,
) {
    when (this) {
        is io.github.mmolosay.thecolor.presentation.navigation.NavEvent.Back -> mainNavController.popBackStack()
        is io.github.mmolosay.thecolor.presentation.navigation.NavEvent.Destination -> mainNavController.navigate(route = destination.route)
    }
}