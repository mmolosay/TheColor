package io.github.mmolosay.thecolor

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Enumeration of top-level navigation destinations.
 */
/*
 * The 'route' property shouldn't be here:
 * If not for the Compose Navigation library that needs a String label for route, it wouldn't be here.
 * Having 'route' here is combining an API model ('AppNavDest') with implementation detail ('route').
 *
 * However, it doesn't harm the quality of code:
 * All navigation actions are passed to the destination screen Composables, so that there's no
 * `navController.navigate(route = AppNavDest.Home.route)` code in feature modules.
 * Thus, we don't need (and won't need) to extract a 'AppNavDest' to a separate shared module.
 * Therefore, there's no need to introduce additional logic and decouple API from implementation for this class.
 */
@Serializable
sealed interface NavDest : NavKey {

    @Serializable
    data object Home : NavDest

    @Serializable
    data object Settings : NavDest

    @Serializable
    data object DevOptions : NavDest

    @Serializable
    data object Acknowledgements : NavDest
}