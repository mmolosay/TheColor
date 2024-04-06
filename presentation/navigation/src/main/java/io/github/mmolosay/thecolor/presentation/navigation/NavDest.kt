package io.github.mmolosay.thecolor.presentation.navigation

/**
 * Enumeration of all top-level navigation destinations of the application.
 * The entities of this component specify solely "where to go" / "what to show".
 *
 * @param route a unique string identifier of this destination.
 */
enum class NavDest(val route: String) {
    Home("home"),
    Settings("settings"),
}