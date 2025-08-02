package io.github.mmolosay.thecolor.presentation.home.viewmodel

/**
 * A family of navigation events that may occur in 'Home' ViewModel.
 */
sealed interface HomeNavEvent {

    data object GoToSettings : HomeNavEvent
}